/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Michael Neonov <two.nelonn@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nelonn.propack.builder.impl.task;

import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.impl.BuilderUtil;
import me.nelonn.propack.builder.api.Project;
import me.nelonn.propack.builder.api.file.File;
import me.nelonn.propack.builder.api.file.JsonFile;
import me.nelonn.propack.builder.api.file.RealFile;
import me.nelonn.propack.builder.api.task.AbstractTask;
import me.nelonn.propack.builder.api.task.FileProcessingException;
import me.nelonn.propack.builder.api.task.TaskBootstrap;
import me.nelonn.propack.builder.api.task.TaskIO;
import me.nelonn.propack.core.asset.SoundAssetBuilder;
import me.nelonn.propack.builder.impl.json.sound.Sound;
import me.nelonn.propack.builder.impl.json.sound.SoundEntry;
import me.nelonn.propack.builder.impl.json.sound.SoundEntryDeserializer;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.PathUtil;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProcessSoundsTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = ProcessSoundsTask::new;

    public ProcessSoundsTask(@NotNull Project project) {
        super("processSounds", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        // Converting sounds
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".wav") && !filePath.endsWith(".mp3"))
                    continue;
                io.getFiles().removeFile(filePath);
                futureList.add(CompletableFuture.runAsync(() -> {
                    try {
                        java.io.File inputFile;
                        if (file instanceof RealFile) {
                            inputFile = ((RealFile) file).getFile();
                        } else {
                            inputFile = BuilderUtil.tempFile(io, filePath);
                            try (InputStream in = file.openInputStream();
                                 OutputStream out = Files.newOutputStream(inputFile.toPath())) {
                                IOUtil.transferTo(in, out);
                            }
                        }
                        String outputPath = Util.substringLast(filePath, 3) + "ogg";
                        java.io.File outputFile = BuilderUtil.tempFile(io, outputPath);
                        String ffmpeg = System.getProperty("propack.ffmpeg", "ffmpeg");
                        String[] commandLine = new String[]{ffmpeg, "-i", inputFile.getAbsolutePath(), "-c:a",
                                "libvorbis", "-q:a", "10", outputFile.getAbsolutePath()};
                        Runtime.getRuntime().exec(commandLine);
                    } catch (IOException e) {
                        LOGGER.error("Unable to convert '" + filePath + "' to ogg", e);
                    }
                }));
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        futureList.forEach(CompletableFuture::join);
        // *.sound.json to sounds.json
        Map<String, JsonObject> soundsJsons = new HashMap<>();
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".sound.json")) continue;
                io.getFiles().removeFile(filePath);
                if (!(file instanceof JsonFile)) {
                    LOGGER.error("{} :: sound file is not Json", filePath);
                    continue;
                }
                JsonObject jsonObject = ((JsonFile) file).getContent();
                Path resourcePath = PathUtil.resourcePath(filePath, ".sound.json");
                SoundEntry soundEntryIn = SoundEntryDeserializer.INSTANCE.deserialize(jsonObject, null, null);
                List<Sound> soundsOut = new ArrayList<>();
                for (Sound soundIn : soundEntryIn.getSounds()) {
                    String oggPathStr = soundIn.getName();
                    if (oggPathStr.endsWith(".ogg")) {
                        oggPathStr = Util.substringLast(oggPathStr, ".ogg");
                    }
                    Path oggPath = PathUtil.resolve(oggPathStr, resourcePath);
                    soundsOut.add(new Sound(oggPath.toString(), soundIn.getVolume(), soundIn.getPitch(),
                            soundIn.getWeight(), soundIn.getRegistrationType(), soundIn.isStreamed(), soundIn.isPreloaded(),
                            soundIn.getAttenuation()));
                }
                SoundEntry soundEntryOut = new SoundEntry(soundsOut, soundEntryIn.canReplace(), soundEntryIn.getSubtitle());
                io.getAssets().putSound(new SoundAssetBuilder(resourcePath).setSoundPath(resourcePath));
                JsonObject resultSounds = soundsJsons.computeIfAbsent(resourcePath.namespace(), key -> new JsonObject());
                resultSounds.add(resourcePath.value(), SoundEntryDeserializer.INSTANCE.serialize(soundEntryOut, null, null));
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (Map.Entry<String, JsonObject> soundsJson : soundsJsons.entrySet()) {
            io.getFiles().addFile(new JsonFile("assets/" + soundsJson.getKey() + "/sounds.json", soundsJson.getValue()));
        }
    }
}

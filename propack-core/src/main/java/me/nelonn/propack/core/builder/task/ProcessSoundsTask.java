/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Nelonn <two.nelonn@gmail.com>
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

package me.nelonn.propack.core.builder.task;

import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.file.JsonFile;
import me.nelonn.propack.builder.file.RealFile;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.core.builder.asset.SoundAssetBuilder;
import me.nelonn.propack.core.builder.json.sound.Sound;
import me.nelonn.propack.core.builder.json.sound.SoundEntry;
import me.nelonn.propack.core.builder.json.sound.SoundEntryDeserializer;
import me.nelonn.propack.core.util.PathUtil;
import me.nelonn.propack.core.util.WaveToVorbis;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessSoundsTask extends AbstractTask {
    public ProcessSoundsTask(@NotNull Project project) {
        super("processSounds", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        // WAV to OGG
        // TODO: now the transcoding is not working correctly, need to fix it
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".wav")) continue;
                io.getFiles().removeFile(filePath);
                String oggFilePath = filePath.substring(0, filePath.length() - "wav".length()) + "ogg";
                java.io.File tempFile = new java.io.File(io.getTempDirectory(), oggFilePath);
                tempFile.getParentFile().mkdirs();
                try (InputStream inputStream = file.openInputStream();
                     OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
                    WaveToVorbis.encode(inputStream, outputStream);
                    io.getFiles().addFile(new RealFile(oggFilePath, tempFile));
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        // *.sound.json to sounds.json
        Map<String, JsonObject> soundsJsons = new HashMap<>();
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".sound.json") || !(file instanceof JsonFile)) continue;
                io.getFiles().removeFile(filePath);
                JsonObject jsonObject = ((JsonFile) file).getContent();
                Path resourcePath = PathUtil.resourcePath(filePath, ".sound.json");
                SoundEntry soundEntryIn = SoundEntryDeserializer.INSTANCE.deserialize(jsonObject, null, null);
                List<Sound> soundsOut = new ArrayList<>();
                for (Sound soundIn : soundEntryIn.getSounds()) {
                    String oggPathStr = soundIn.getName();
                    if (oggPathStr.endsWith(".ogg")) {
                        oggPathStr = oggPathStr.substring(0, oggPathStr.length() - ".ogg".length());
                    }
                    Path oggPath = PathUtil.resolve(soundIn.getName(), resourcePath);
                    soundsOut.add(new Sound(oggPath.toString(), soundIn.getVolume(), soundIn.getPitch(),
                            soundIn.getWeight(), soundIn.getRegistrationType(), soundIn.isStreamed(), soundIn.isPreloaded(),
                            soundIn.getAttenuation()));
                }
                SoundEntry soundEntryOut = new SoundEntry(soundsOut, soundEntryIn.canReplace(), soundEntryIn.getSubtitle());
                io.getAssets().putSound(new SoundAssetBuilder(resourcePath).setSoundPath(resourcePath));
                JsonObject resultSounds = Util.getOrPut(soundsJsons, resourcePath.getNamespace(), JsonObject::new);
                resultSounds.add(resourcePath.getValue(), SoundEntryDeserializer.INSTANCE.serialize(soundEntryOut, null, null));
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (Map.Entry<String, JsonObject> soundsJson : soundsJsons.entrySet()) {
            io.getFiles().addFile(new JsonFile("assets/" + soundsJson.getKey() + "/sounds.json", soundsJson.getValue()));
        }
    }
}

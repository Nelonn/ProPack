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

package me.nelonn.propack.core.builder.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.file.JsonFile;
import me.nelonn.propack.builder.task.AbstractTask;
import me.nelonn.propack.builder.task.FileProcessingException;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.core.builder.ObfuscationConfiguration;
import me.nelonn.propack.core.builder.asset.FontBuilder;
import me.nelonn.propack.core.builder.asset.SoundAssetBuilder;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.PathUtil;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ObfuscateTask extends AbstractTask {
    public static final TaskBootstrap BOOTSTRAP = ObfuscateTask::new;

    public ObfuscateTask(@NotNull Project project) {
        super("obfuscate", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        ObfuscationConfiguration conf = getProject().getBuildConfiguration().getObfuscationConfiguration();
        ObfuscationNamer namer = new ObfuscationNamer();
        if (conf.isMeshes()) {
            obfuscateMeshes(io, namer, conf);
        }
        if (conf.isTextures()) {
            obfuscateTextures(io, namer, conf);
        }
        if (conf.isOgg()) {
            obfuscateOgg(io, namer, conf);
        }
        if (conf.isSounds()) {
            obfuscateSounds(io, namer, conf);
        }
        if (conf.isFonts()) {
            obfuscateFonts(io, namer, conf);
        }
    }

    private void obfuscateMeshes(@NotNull TaskIO io, @NotNull ObfuscationNamer namer, @NotNull ObfuscationConfiguration conf) {
        String obfuscatedNamespace = conf.getNamespace();
        Map<Path, Path> meshMapping = new HashMap<>();
        for (File file : files(io, conf)) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("assets/") || !filePath.endsWith(".json") || !(file instanceof JsonFile)) continue;
                String[] pathSplit = filePath.split("/", 4); // assets/example/models/file.json
                if (pathSplit.length < 4 || pathSplit[1].equalsIgnoreCase("minecraft") ||
                        !pathSplit[2].equalsIgnoreCase("models")) continue;
                Path contentPath = Path.of(pathSplit[1], Util.substringLast(pathSplit[3], ".json"));
                Path obfuscatedPath = Path.of(obfuscatedNamespace, namer.next());
                io.getFiles().addFile(file.copyAs(PathUtil.assetsPath(obfuscatedPath, "models") + ".json"));
                meshMapping.put(contentPath, obfuscatedPath);
                io.getFiles().removeFile(filePath);
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (File file : files(io, conf)) {
            try {
                String filePath = file.getPath();
                if (filePath.startsWith("include/")) {
                    filePath = filePath.substring("include/".length());
                }
                if (!filePath.startsWith("assets/") || !filePath.endsWith(".json") || !(file instanceof JsonFile)) continue;
                String[] pathSplit = filePath.split("/", 4); // assets/example/models/file.json
                if (pathSplit.length < 4) continue;
                JsonObject jsonObject = ((JsonFile) file).getContent();
                if (pathSplit[2].equalsIgnoreCase("models")) {
                    if (jsonObject.has("parent")) {
                        Path path = Path.of(GsonHelper.getString(jsonObject, "parent"));
                        Path obfuscatedPath = meshMapping.get(path);
                        if (obfuscatedPath != null) {
                            jsonObject.addProperty("parent", obfuscatedPath.toString());
                        }
                    }
                    if (jsonObject.has("overrides")) {
                        JsonArray overrides = GsonHelper.getArray(jsonObject, "overrides");
                        for (JsonElement overrideElement : overrides) {
                            JsonObject overrideObject = overrideElement.getAsJsonObject();
                            if (overrideObject.has("model")) {
                                Path path = Path.of(GsonHelper.getString(overrideObject, "model"));
                                Path obfuscatedPath = meshMapping.get(path);
                                if (obfuscatedPath != null) {
                                    overrideObject.addProperty("model", obfuscatedPath.toString());
                                }
                            }
                        }
                    }
                } else if (pathSplit[2].equalsIgnoreCase("blockstates")) {
                    if (GsonHelper.hasArray(jsonObject, "multipart")) {
                        JsonArray multipartArray = jsonObject.getAsJsonArray("multipart");
                        for (JsonElement jsonElement : multipartArray) {
                            if (!jsonElement.isJsonObject()) continue;
                            JsonObject elementObject = jsonElement.getAsJsonObject();
                            if (GsonHelper.hasJsonObject(elementObject, "apply")) {
                                JsonObject applyObject = elementObject.getAsJsonObject("apply");
                                if (GsonHelper.hasString(applyObject, "model")) {
                                    String model = applyObject.get("model").getAsString();
                                    try {
                                        Path path = Path.of(model);
                                        Path obfuscatedPath = meshMapping.get(path);
                                        if (obfuscatedPath != null) {
                                            applyObject.addProperty("model", obfuscatedPath.toString());
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                    if (GsonHelper.hasJsonObject(jsonObject, "variants")) {
                        JsonObject variantsObject = jsonObject.getAsJsonObject("variants");
                        for (Map.Entry<String, JsonElement> entry : variantsObject.entrySet()) {
                            if (!entry.getValue().isJsonArray()) continue;
                            JsonArray variant = entry.getValue().getAsJsonArray();
                            for (JsonElement jsonElement : variant) {
                                if (!jsonElement.isJsonObject()) continue;
                                JsonObject elementObject = jsonElement.getAsJsonObject();
                                if (GsonHelper.hasString(elementObject, "model")) {
                                    String model = elementObject.get("model").getAsString();
                                    try {
                                        Path path = Path.of(model);
                                        Path obfuscatedPath = meshMapping.get(path);
                                        if (obfuscatedPath != null) {
                                            elementObject.addProperty("model", obfuscatedPath.toString());
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
    }

    private void obfuscateTextures(@NotNull TaskIO io, @NotNull ObfuscationNamer namer, @NotNull ObfuscationConfiguration conf) {
        String obfuscatedNamespace = conf.getNamespace();
        String obfuscatedTexturesAtlasesFolder = conf.getTexturesAtlasesFolder();
        namer.reset();
        Map<Path, Path> pngMapping = new HashMap<>();
        for (File file : files(io, conf)) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".png")) continue;
                Path resourcePath = PathUtil.resourcePath(filePath, ".png");
                boolean trueResolution = false;
                try (InputStream inputStream = file.openInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    trueResolution = image.getHeight() >= 16 && image.getWidth() >= 16;
                }
                String obfuscatedName = namer.next();
                if (trueResolution) {
                    obfuscatedName = obfuscatedTexturesAtlasesFolder + '/' + obfuscatedName;
                }
                Path obfuscatedPath = Path.of(obfuscatedNamespace, obfuscatedName);
                io.getFiles().addFile(file.copyAs(PathUtil.assetsPath(obfuscatedPath, "textures") + ".png"));
                pngMapping.put(resourcePath, obfuscatedPath);
                io.getFiles().removeFile(filePath);
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (File file : files(io, conf)) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".png.mcmeta")) continue;
                Path resourcePath = PathUtil.resourcePath(filePath, ".png.mcmeta");
                Path obfuscatedPath = pngMapping.get(resourcePath);
                if (obfuscatedPath != null) {
                    io.getFiles().addFile(file.copyAs(PathUtil.assetsPath(obfuscatedPath, "textures") + ".png.mcmeta"));
                    io.getFiles().removeFile(filePath);
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (File file : files(io, conf)) {
            try {
                String filePath = file.getPath();
                if (filePath.startsWith("include/")) {
                    filePath = filePath.substring("include/".length());
                }
                if (!filePath.startsWith("assets/") || !filePath.endsWith(".json") || !(file instanceof JsonFile)) continue;
                String[] pathSplit = filePath.split("/", 4); // assets/example/models/file.json
                if (pathSplit.length < 4) continue;
                String type = pathSplit[2];
                JsonObject jsonObject = ((JsonFile) file).getContent();
                if (type.equalsIgnoreCase("models")) {
                    JsonObject texturesObject = jsonObject.getAsJsonObject("textures");
                    if (texturesObject != null) {
                        for (Map.Entry<String, JsonElement> textureEntry : texturesObject.entrySet()) {
                            String texture = textureEntry.getValue().getAsString();
                            if (texture.startsWith("#")) continue;
                            Path obfuscatedTexture = pngMapping.get(Path.of(texture));
                            if (obfuscatedTexture != null) {
                                textureEntry.setValue(new JsonPrimitive(obfuscatedTexture.toString()));
                            }
                        }
                    }
                } else if (type.equalsIgnoreCase("font")) {
                    JsonArray providersArray = jsonObject.getAsJsonArray("providers");
                    if (providersArray != null) {
                        for (JsonElement jsonElement : providersArray) {
                            JsonObject providerObject = jsonElement.getAsJsonObject();
                            String providerType = GsonHelper.getString(providerObject, "type");
                            if (providerType.equalsIgnoreCase("bitmap")) {
                                String path = GsonHelper.getString(providerObject, "file");
                                path = Util.substringLast(path, ".png");
                                Path obfuscatedPath = pngMapping.get(Path.of(path));
                                if (obfuscatedPath != null) {
                                    providerObject.addProperty("file", obfuscatedPath + ".png");
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
    }

    private void obfuscateOgg(@NotNull TaskIO io, @NotNull ObfuscationNamer namer, @NotNull ObfuscationConfiguration conf) {
        String obfuscatedNamespace = conf.getNamespace();
        namer.reset();
        Map<Path, Path> oggMapping = new HashMap<>();
        for (File file : files(io, conf)) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".ogg")) continue;
                Path resourcePath = PathUtil.resourcePath(filePath, ".ogg");
                Path obfuscatedPath = Path.of(obfuscatedNamespace, namer.next());
                io.getFiles().addFile(file.copyAs(PathUtil.assetsPath(obfuscatedPath, "sounds") + ".ogg"));
                oggMapping.put(resourcePath, obfuscatedPath);
                io.getFiles().removeFile(filePath);
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (File file : files(io, conf)) {
            try {
                String filePath = file.getPath();
                if (filePath.startsWith("include/")) {
                    filePath = filePath.substring("include/".length());
                }
                if (!filePath.startsWith("assets/") || !filePath.endsWith("/sounds.json") || !(file instanceof JsonFile)) continue;
                String[] pathSplit = filePath.split("/", 4); // assets/example/sounds.json
                if (pathSplit.length > 3) continue;
                JsonObject soundsObject = ((JsonFile) file).getContent();
                for (String soundName : soundsObject.keySet()) {
                    JsonObject soundObject = GsonHelper.getObject(soundsObject, soundName);
                    JsonArray soundsArray = GsonHelper.getArray(soundObject, "sounds");
                    for (int i = 0; i < soundsArray.size(); i++) {
                        JsonElement jsonElement = soundsArray.get(i);
                        Path friendlyPath;
                        if (GsonHelper.isString(jsonElement)) {
                            friendlyPath = Path.of(jsonElement.getAsString());
                        } else {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            friendlyPath = Path.of(jsonObject.get("name").getAsString());
                        }
                        Path obfuscatedPath = oggMapping.get(friendlyPath);
                        if (obfuscatedPath != null) {
                            if (GsonHelper.isString(jsonElement)) {
                                soundsArray.set(i, new JsonPrimitive(obfuscatedPath.toString()));
                            } else {
                                JsonObject jsonObject = jsonElement.getAsJsonObject();
                                jsonObject.addProperty("name", obfuscatedPath.toString());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
    }

    private void obfuscateSounds(@NotNull TaskIO io, @NotNull ObfuscationNamer namer, @NotNull ObfuscationConfiguration conf) {
        String obfuscatedNamespace = conf.getNamespace();
        namer.reset();
        JsonObject obfuscatedSounds = new JsonObject();
        List<String> filesToRemove = new ArrayList<>();
        for (SoundAssetBuilder soundAsset : io.getAssets().getSounds()) {
            String soundsFilePath = "assets/" + soundAsset.getPath().namespace() + "/sounds.json";
            File soundsFile = io.getFiles().getFile(soundsFilePath);
            if (!(soundsFile instanceof JsonFile)) {
                throw new IllegalStateException("sounds.json file not found for '" + soundAsset.getPath() + "'");
            }
            if (!filesToRemove.contains(soundsFilePath)) {
                filesToRemove.add(soundsFilePath);
            }
            JsonObject soundsObject = ((JsonFile) soundsFile).getContent();
            JsonObject soundObject = soundsObject.getAsJsonObject(soundAsset.getSoundPath().value());
            Path obfuscatedSoundPath = Path.of(obfuscatedNamespace, namer.next());
            obfuscatedSounds.add(obfuscatedSoundPath.value(), soundObject);
            soundAsset.setSoundPath(obfuscatedSoundPath);
        }
        for (String fileToRemove : filesToRemove) {
            io.getFiles().removeFile(fileToRemove);
        }
        io.getFiles().addFile(new JsonFile(PathUtil.join("assets", obfuscatedNamespace, "sounds.json"), obfuscatedSounds));
    }

    private void obfuscateFonts(@NotNull TaskIO io, @NotNull ObfuscationNamer namer, @NotNull ObfuscationConfiguration conf) {
        String obfuscatedNamespace = conf.getNamespace();
        namer.reset();
        for (FontBuilder font : io.getAssets().getFonts()) {
            String fontFilePath = "assets/" + font.getFontPath().namespace() + "/font/" + font.getFontPath().value() + ".json";
            File fontFile = io.getFiles().removeFile(fontFilePath);
            if (!(fontFile instanceof JsonFile)) {
                throw new IllegalStateException("font file not found for '" + font.getPath() + "'");
            }
            Path obfuscatedPath = Path.of(obfuscatedNamespace, namer.next());
            font.setFontPath(obfuscatedPath);
            io.getFiles().addFile(fontFile.copyAs(PathUtil.assetsPath(obfuscatedPath, "font") + ".json"));
        }
    }

    private Iterable<File> files(TaskIO io, ObfuscationConfiguration conf) {
        if (conf.isShuffleSequence()) {
            List<File> files = new ArrayList<>(io.getFiles().getFiles());
            Collections.shuffle(files);
            return files;
        } else {
            return io.getFiles().copy().getFiles();
        }
    }

    public static class ObfuscationNamer {
        private final AtomicInteger integer = new AtomicInteger(1);

        public ObfuscationNamer() {
        }

        public void reset() {
            integer.set(0);
        }

        public String next() {
            return Integer.toHexString(integer.getAndIncrement());
        }
    }
}

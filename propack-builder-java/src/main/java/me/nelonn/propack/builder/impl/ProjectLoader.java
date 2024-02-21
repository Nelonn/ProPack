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

package me.nelonn.propack.builder.impl;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import me.nelonn.flint.path.Key;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.builder.api.StrictMode;
import me.nelonn.propack.builder.api.file.ByteFile;
import me.nelonn.propack.builder.api.file.VirtualFile;
import me.nelonn.propack.builder.api.hosting.Hosting;
import me.nelonn.propack.core.loader.text.TextLoader;
import me.nelonn.propack.builder.api.util.Extra;
import me.nelonn.propack.core.loader.LoadedResourcePack;
import me.nelonn.propack.core.loader.ProPackFileLoader;
import me.nelonn.propack.core.loader.text.LegacyTextLoader;
import me.nelonn.propack.core.loader.text.MiniMessageTextLoader;
import me.nelonn.propack.core.loader.text.TextComponentLoader;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

public class ProjectLoader {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final Extra<File> EXTRA_CONFIG_DIR = new Extra<>(File.class, "propack.project_loader.config_dir");
    private final ProPackCore core;
    private final List<TextLoader> textLoaders;

    public ProjectLoader(@NotNull ProPackCore core,
                         @Nullable List<TextLoader> textLoaders) {
        this.core = core;
        if (textLoaders != null) {
            this.textLoaders = new ArrayList<>(textLoaders);
        } else {
            this.textLoaders = new ArrayList<>();
            this.textLoaders.add(LegacyTextLoader.INSTANCE);
            this.textLoaders.add(MiniMessageTextLoader.INSTANCE);
            this.textLoaders.add(TextComponentLoader.INSTANCE);
        }
    }

    public ProjectLoader(@NotNull ProPackCore core) {
        this(core, null);
    }

    public List<TextLoader> getTextLoaders() {
        return textLoaders;
    }

    public @NotNull InternalProject load(@NotNull File projectFile, boolean tryLoadBuilt) {
        String name;
        VirtualFile packMeta;
        VirtualFile packIcon;
        try {
            String projectFileContent = IOUtil.readString(projectFile);
            JsonObject projectFileObject = GsonHelper.deserialize(projectFileContent, true);

            int fileVersion = GsonHelper.getInt(projectFileObject, "FileVersion");
            if (fileVersion != 1) {
                throw new JsonSyntaxException("File version " + fileVersion + " is not supported");
            }

            name = GsonHelper.getString(projectFileObject, "Name").toLowerCase(Locale.ROOT);
            int packFormat = GsonHelper.getInt(projectFileObject, "PackFormat");

            if (projectFileObject.has("Icon")) {
                String iconPath = GsonHelper.getString(projectFileObject, "Icon");
                File iconFile = new File(projectFile.getParentFile(), iconPath + ".png");
                if (!iconFile.exists()) {
                    throw new IllegalArgumentException("File '" + iconFile.getName() + "' not found");
                } else if (iconFile.isDirectory()) {
                    throw new IllegalArgumentException("File '" + iconFile.getName() + "' is directory");
                }
                try (FileInputStream fileInputStream = new FileInputStream(iconFile)) {
                    packIcon = new ByteFile("pack.png", IOUtil.readAllBytes(fileInputStream));
                }
            } else {
                packIcon = null;
            }

            Component description;
            if (projectFileObject.has("Description")) {
                JsonObject descriptionObject = GsonHelper.getObject(projectFileObject, "Description");
                String type = GsonHelper.getString(descriptionObject, "Type");
                JsonElement text = descriptionObject.get("Text");
                if (text == null || text.isJsonNull()) {
                    throw new IllegalArgumentException("Missing 'Text' in Description");
                }
                List<TextLoader> results = textLoaders.stream().filter(loader -> loader.is(type)).collect(Collectors.toList());
                if (results.isEmpty()) {
                    throw new UnsupportedOperationException("No loader found for Description type '" + type + "'");
                } else if (results.size() > 1) {
                    LOGGER.warn("Found more than 1 loaders for the Description type '{}'", type);
                }
                description = results.get(0).load(descriptionObject);
            } else {
                description = Component.empty();
            }

            JsonObject packMetaObject = new JsonObject();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("pack_format", packFormat);
            jsonObject.add("description", GsonComponentSerializer.gson().serializeToTree(description));
            packMetaObject.add("pack", jsonObject);

            packMeta = new ByteFile("pack.mcmeta", packMetaObject.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading '" + projectFile.getName() + "'", e);
        }

        StrictMode strictMode;
        Pattern fileIgnore = null;
        Pattern dirIgnore = null;
        ObfuscationConfiguration obfuscationConfiguration;
        try {
            File buildConfigFile = new File(projectFile.getParentFile(), "config/build.json5");
            String buildConfigContent = IOUtil.readString(buildConfigFile);
            JsonObject buildConfigObject = GsonHelper.deserialize(buildConfigContent, true);

            if (buildConfigObject.has("Strict")) {
                JsonElement jsonElement = buildConfigObject.get("Strict");

                if (GsonHelper.isBoolean(jsonElement)) {
                    strictMode = jsonElement.getAsBoolean() ? StrictMode.ENABLED : StrictMode.DISABLED;
                } else if (GsonHelper.isString(jsonElement) && jsonElement.getAsString().equalsIgnoreCase("warn")) {
                    strictMode = StrictMode.WARN;
                } else {
                    throw new IllegalArgumentException("Strict mode '" + jsonElement + "' not found");
                }
            } else {
                strictMode = StrictMode.ENABLED;
            }

            if (buildConfigObject.has("DirIgnore")) {
                JsonArray dirIgnoreArray = GsonHelper.getArray(buildConfigObject, "DirIgnore");
                if (!dirIgnoreArray.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    Util.forEachStringArray(dirIgnoreArray, "DirIgnore", s -> {
                        if (s.isEmpty()) return;
                        if (sb.length() > 0) {
                            sb.append('|');
                        }
                        sb.append('(').append(s).append(')');
                    });
                    dirIgnore = Pattern.compile(sb.toString());
                }
            }

            if (buildConfigObject.has("FileIgnore")) {
                JsonArray fileIgnoreArray = GsonHelper.getArray(buildConfigObject, "FileIgnore");
                if (!fileIgnoreArray.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    Util.forEachStringArray(fileIgnoreArray, "FileIgnore", s -> {
                        if (s.isEmpty()) return;
                        if (sb.length() > 0) {
                            sb.append('|');
                        }
                        sb.append('(').append(s).append(')');
                    });
                    fileIgnore = Pattern.compile(sb.toString());
                }
            }

            JsonObject obfuscationObject = GsonHelper.getObject(buildConfigObject, "Obfuscation");

            boolean obfuscationEnabled = GsonHelper.getBoolean(obfuscationObject, "Enabled", false);
            String obfuscatedNamespace = GsonHelper.getString(obfuscationObject, "Namespace", "obfuscated");
            boolean obfuscateShuffleSequence = GsonHelper.getBoolean(obfuscationObject, "ShuffleSequence", false);
            boolean obfuscateMeshes = GsonHelper.getBoolean(obfuscationObject, "Meshes", false);
            boolean obfuscateTextures = GsonHelper.getBoolean(obfuscationObject, "Textures", false);
            String obfuscateTexturesBlocksAtlasFolder = GsonHelper.getString(obfuscationObject, "TexturesBlocksAtlasFolder", "1");
            String obfuscateTexturesGuiAtlasFolder = GsonHelper.getString(obfuscationObject, "TexturesGuiAtlasFolder", "2");
            boolean obfuscateOgg = GsonHelper.getBoolean(obfuscationObject, "Ogg", false);
            boolean obfuscateSounds = GsonHelper.getBoolean(obfuscationObject, "Sounds", false);
            boolean obfuscateFonts = GsonHelper.getBoolean(obfuscationObject, "Fonts", false);

            if (obfuscateTexturesBlocksAtlasFolder.startsWith("/")) {
                obfuscateTexturesBlocksAtlasFolder = obfuscateTexturesBlocksAtlasFolder.substring(1);
            }
            if (obfuscateTexturesBlocksAtlasFolder.endsWith("/")) {
                obfuscateTexturesBlocksAtlasFolder = obfuscateTexturesBlocksAtlasFolder.substring(0, obfuscateTexturesBlocksAtlasFolder.length() - 1);
            }

            if (obfuscateTexturesGuiAtlasFolder.startsWith("/")) {
                obfuscateTexturesGuiAtlasFolder = obfuscateTexturesGuiAtlasFolder.substring(1);
            }
            if (obfuscateTexturesGuiAtlasFolder.endsWith("/")) {
                obfuscateTexturesGuiAtlasFolder = obfuscateTexturesGuiAtlasFolder.substring(0, obfuscateTexturesGuiAtlasFolder.length() - 1);
            }

            obfuscationConfiguration = new ObfuscationConfiguration(
                    obfuscationEnabled,
                    obfuscatedNamespace,
                    obfuscateShuffleSequence,
                    obfuscateMeshes,
                    obfuscateTextures,
                    obfuscateTexturesBlocksAtlasFolder,
                    obfuscateTexturesGuiAtlasFolder,
                    obfuscateOgg,
                    obfuscateSounds,
                    obfuscateFonts
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading 'config/build.json5'", e);
        }

        Map<String, String> allLangTranslations = new HashMap<>();
        ImmutableSet.Builder<String> languagesBuilder = ImmutableSet.builder();
        File languagesConfigFile = new File(projectFile.getParentFile(), "config/languages.json5");
        if (languagesConfigFile.exists()) {
            try {
                String languagesConfigContent = IOUtil.readString(languagesConfigFile);
                JsonObject languagesConfigObject = GsonHelper.deserialize(languagesConfigContent, true);

                JsonObject translations = GsonHelper.getObject(languagesConfigObject, "AllLangTranslations");
                for (Map.Entry<String, JsonElement> entry : translations.entrySet()) {
                    allLangTranslations.put(entry.getKey(), GsonHelper.asString(entry.getValue(), entry.getKey()));
                }

                JsonArray languagesArray = GsonHelper.getArray(languagesConfigObject, "Languages");
                Util.forEachStringArray(languagesArray, "Languages", languagesBuilder::add);
            } catch (Exception e) {
                throw new IllegalArgumentException("Something went wrong when loading 'config/languages.json5'", e);
            }
        }
        Set<String> languages = languagesBuilder.build();

        PackageOptions packageOptions;
        try {
            File packageConfigFile = new File(projectFile.getParentFile(), "config/package.json5");
            String packageConfigContent = IOUtil.readString(packageConfigFile);
            JsonObject packageConfigObject = GsonHelper.deserialize(packageConfigContent, true);
            int compressionLevel;
            if (packageConfigObject.has("compression")) {
                JsonElement jsonElement = packageConfigObject.get("compression");
                if (GsonHelper.isString(jsonElement)) {
                    String string = jsonElement.getAsString();
                    try {
                        compressionLevel = Deflater.class.getDeclaredField(string).getInt(null);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Compression level with name '" + string + "' not found");
                    }
                } else if (GsonHelper.isNumber(jsonElement)) {
                    compressionLevel = GsonHelper.getInt(packageConfigObject, "compression");
                } else {
                    throw new IllegalArgumentException("Expected 'compression' to be a string or number");
                }
            } else {
                compressionLevel = Deflater.BEST_COMPRESSION;
            }
            boolean protection = GsonHelper.getBoolean(packageConfigObject, "protection", false);
            String comment = GsonHelper.getString(packageConfigObject, "comment", "");
            packageOptions = new PackageOptions(compressionLevel, protection, comment);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading 'config/package.json5'", e);
        }

        Hosting hosting;
        Map<String, Object> uploadOptions;
        try {
            File uploadConfigFile = new File(projectFile.getParentFile(), "config/upload.json5");
            String uploadConfigContent = IOUtil.readString(uploadConfigFile);
            JsonObject uploadConfigObject = GsonHelper.getGson().fromJson(uploadConfigContent, JsonObject.class);
            if (GsonHelper.getBoolean(uploadConfigObject, "Enabled")) {
                Key to = Key.withFallback(GsonHelper.getString(uploadConfigObject, "To"), "propack");
                hosting = core.getHostingMap().getHosting(to);
                JsonObject optionsObject = GsonHelper.getObject(uploadConfigObject, "Options");
                uploadOptions = toOptions(optionsObject);
            } else {
                hosting = null;
                uploadOptions = null;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading 'config/upload.json5'", e);
        }

        BuildConfiguration buildConfiguration = new BuildConfiguration(strictMode, dirIgnore, fileIgnore,
                obfuscationConfiguration, allLangTranslations, languages, packageOptions, hosting, uploadOptions);

        ResourcePack resourcePack = null;
        File builtResourcePack = new File(projectFile.getParentFile(), "build/" + name + ".propack");
        if (builtResourcePack.exists() && tryLoadBuilt) {
            try {
                ProPackFileLoader proPackFileLoader = new ProPackFileLoader();
                Resources resources = proPackFileLoader.load(builtResourcePack);
                resourcePack = new LoadedResourcePack(name, resources);
            } catch (Exception e) {
                LOGGER.error("Unable to load built resource pack", e);
            }
        }

        InternalProject project = new InternalProject(name, projectFile.getParentFile(), buildConfiguration,
                packMeta, packIcon, resourcePack);

        LOGGER.info("Project '{}' successfully loaded", name);

        return project;
    }

    private static Object toPrimitive(JsonElement element) {
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return element.getAsNumber();
        } else if (primitive.isBoolean()) {
            return element.getAsBoolean();
        } else if (primitive.isString()) {
            return element.getAsString();
        }
        return null;
    }

    private static Map<String, Object> toOptions(JsonObject optionsObject) {
        Map<String, Object> options = new HashMap<>();
        for (String key : optionsObject.keySet()) {
            JsonElement element = optionsObject.get(key);
            if (element.isJsonPrimitive()) {
                options.put(key, toPrimitive(element));
            } else if (element.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonElement arrayElement : element.getAsJsonArray()) {
                    if (arrayElement.isJsonPrimitive()) {
                        list.add(toPrimitive(arrayElement));
                    } else if (arrayElement.isJsonNull()) {
                        list.add(null);
                    }
                }
                options.put(key, list);
            } else if (element.isJsonNull()) {
                options.put(key, null);
            } else {
                throw new JsonSyntaxException("Objects is not allowed in options");
            }
        }
        return options;
    }
}

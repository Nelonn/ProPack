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

package me.nelonn.propack.core.loader;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.builder.Hosting;
import me.nelonn.propack.builder.StrictMode;
import me.nelonn.propack.builder.ZipPackager;
import me.nelonn.propack.builder.file.ByteFile;
import me.nelonn.propack.builder.file.VirtualFile;
import me.nelonn.propack.builder.loader.ItemDefinitionLoader;
import me.nelonn.propack.builder.loader.TextLoader;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.builder.util.Extras;
import me.nelonn.propack.core.builder.BuildConfiguration;
import me.nelonn.propack.core.builder.InternalProject;
import me.nelonn.propack.core.builder.ObfuscationConfiguration;
import me.nelonn.propack.core.loader.itemdefinition.JsonFileItemDefinitionLoader;
import me.nelonn.propack.core.loader.text.LegacyTextLoader;
import me.nelonn.propack.core.loader.text.MiniMessageTextLoader;
import me.nelonn.propack.core.loader.text.TextComponentLoader;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.Util;
import me.nelonn.propack.definition.ItemDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectLoader {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final Extra<File> EXTRA_CONFIG_DIR = new Extra<>(File.class, "propack.project_loader.config_dir");
    private final List<TextLoader> textLoaders;
    private final List<ItemDefinitionLoader> itemDefinitionLoaders;

    public ProjectLoader(@Nullable List<TextLoader> textLoaders,
                         @Nullable List<ItemDefinitionLoader> itemDefinitionLoaders) {
        if (textLoaders != null) {
            this.textLoaders = new ArrayList<>(textLoaders);
        } else {
            this.textLoaders = new ArrayList<>();
            this.textLoaders.add(LegacyTextLoader.INSTANCE);
            this.textLoaders.add(MiniMessageTextLoader.INSTANCE);
            this.textLoaders.add(TextComponentLoader.INSTANCE);
        }
        if (itemDefinitionLoaders != null) {
            this.itemDefinitionLoaders = new ArrayList<>(itemDefinitionLoaders);
        } else {
            this.itemDefinitionLoaders = new ArrayList<>();
            this.itemDefinitionLoaders.add(JsonFileItemDefinitionLoader.INSTANCE);
        }
    }

    public ProjectLoader() {
        this(null, null);
    }

    public @NotNull InternalProject load(@NotNull File projectFile, boolean loadBuilt) {
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

        ItemDefinition itemDefinition;
        StrictMode strictMode;
        ImmutableSet.Builder<String> ignoredExtensionsBuilder = ImmutableSet.builder();
        ObfuscationConfiguration obfuscationConfiguration;
        try {
            File buildConfigFile = new File(projectFile.getParentFile(), "config/build.json5");
            String buildConfigContent = IOUtil.readString(buildConfigFile);
            JsonObject buildConfigObject = GsonHelper.deserialize(buildConfigContent, true);

            JsonObject itemDefinitionObject = GsonHelper.getObject(buildConfigObject, "ItemDefinition");
            String type = GsonHelper.getString(itemDefinitionObject, "Type");

            List<ItemDefinitionLoader> results = itemDefinitionLoaders.stream().filter(loader -> loader.is(type)).collect(Collectors.toList());
            if (results.isEmpty()) {
                throw new UnsupportedOperationException("No loader found for ItemDefinition type '" + type + "'");
            } else if (results.size() > 1) {
                LOGGER.warn("Found more than 1 loaders for the ItemDefinition type '{}'", type);
            }
            Extras extras = new Extras();
            extras.put(EXTRA_CONFIG_DIR, new File(projectFile.getParentFile(), "config"));
            itemDefinition = results.get(0).load(itemDefinitionObject, extras);

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

            if (buildConfigObject.has("IgnoredExtensions")) {
                JsonArray ignoredExtensionsArray = GsonHelper.getArray(buildConfigObject, "IgnoredExtensions");
                Util.forEachStringArray(ignoredExtensionsArray, "IgnoredExtensions", ignoredExtensionsBuilder::add);
            }

            JsonObject obfuscationObject = GsonHelper.getObject(buildConfigObject, "Obfuscation");

            boolean obfuscationEnabled = GsonHelper.getBoolean(obfuscationObject, "Enabled", false);
            String obfuscatedNamespace = GsonHelper.getString(obfuscationObject, "Namespace", "obfuscated");
            boolean obfuscateShuffleSequence = GsonHelper.getBoolean(obfuscationObject, "ShuffleSequence", false);
            boolean obfuscateMeshes = GsonHelper.getBoolean(obfuscationObject, "Meshes", false);
            boolean obfuscateTextures = GsonHelper.getBoolean(obfuscationObject, "Textures", false);
            boolean obfuscateOgg = GsonHelper.getBoolean(obfuscationObject, "Ogg", false);
            boolean obfuscateSounds = GsonHelper.getBoolean(obfuscationObject, "Sounds", false);
            boolean obfuscateFonts = GsonHelper.getBoolean(obfuscationObject, "Fonts", false);

            obfuscationConfiguration = new ObfuscationConfiguration(obfuscationEnabled, obfuscatedNamespace,
                    obfuscateShuffleSequence, obfuscateMeshes, obfuscateTextures, obfuscateOgg, obfuscateSounds, obfuscateFonts);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading 'config/build.json5'", e);
        }
        Set<String> ignoredExtensions = ignoredExtensionsBuilder.build();

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

        ZipPackager zipPackager;
        Map<String, Object> packageOptions;
        try {
            File packageConfigFile = new File(projectFile.getParentFile(), "config/package.json5");
            String packageConfigContent = IOUtil.readString(packageConfigFile);
            JsonObject packageConfigObject = GsonHelper.deserialize(packageConfigContent, true);

            String className = GsonHelper.getString(packageConfigObject, "Class");
            Class<?> clazz = Class.forName(className);
            if (!ZipPackager.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class '" + className + "' must implement '" +
                        ZipPackager.class.getName() + "'");
            }
            Constructor<?> constructor;
            try {
                constructor = clazz.getDeclaredConstructor();
            } catch (Exception e) {
                throw new IllegalArgumentException("Class '" + className +
                        "' must have a constructor without parameters");
            }
            zipPackager = (ZipPackager) constructor.newInstance();

            JsonObject optionsObject = GsonHelper.getObject(packageConfigObject, "Options");
            packageOptions = toOptions(optionsObject);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading 'config/package.json5'", e);
        }

        Hosting hosting;
        try {
            File uploadConfigFile = new File(projectFile.getParentFile(), "config/upload.json5");
            String uploadConfigContent = IOUtil.readString(uploadConfigFile);
            JsonObject uploadConfigObject = GsonHelper.getGson().fromJson(uploadConfigContent, JsonObject.class);
            if (GsonHelper.getBoolean(uploadConfigObject, "Enabled")) {
                try {
                    String className = GsonHelper.getString(uploadConfigObject, "Class");
                    Class<?> clazz = Class.forName(className);
                    if (!Hosting.class.isAssignableFrom(clazz)) {
                        throw new IllegalArgumentException("Class '" + className + "' must implement '" +
                                Hosting.class.getName() + "'");
                    }
                    Constructor<?> constructor;
                    try {
                        constructor = clazz.getDeclaredConstructor();
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Class '" + className +
                                "' must have a constructor without parameters");
                    }
                    hosting = (Hosting) constructor.newInstance();
                    JsonObject sharedOptionsObject = GsonHelper.getObject(uploadConfigObject, "SharedOptions");
                    // TODO: separate SharedOptions and Options
                    JsonObject optionsObject = GsonHelper.getObject(uploadConfigObject, "Options");
                    hosting.enable(toOptions(sharedOptionsObject));
                } catch (Exception e) {
                    LOGGER.error("Unable to initialize upload system", e);
                    hosting = null;
                }
            } else {
                hosting = null;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading 'config/upload.json5'", e);
        }

        BuildConfiguration buildConfiguration = new BuildConfiguration(strictMode, ignoredExtensions,
                obfuscationConfiguration, allLangTranslations, languages, zipPackager, packageOptions, hosting);

        ResourcePack resourcePack;
        File builtResourcePack = new File(projectFile.getParentFile(), "build/" + name + ".propack");
        if (builtResourcePack.exists()) {
            ResourcePackLoader resourcePackLoader = new ResourcePackLoader();
            resourcePack = resourcePackLoader.load(builtResourcePack);
        } else {
            resourcePack = null;
        }

        InternalProject project = new InternalProject(name, projectFile.getParentFile(),
                itemDefinition, buildConfiguration, packMeta, packIcon, resourcePack);

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

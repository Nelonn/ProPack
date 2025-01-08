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

import me.nelonn.propack.builder.api.StrictMode;
import me.nelonn.propack.builder.api.hosting.Hosting;
import me.nelonn.propack.builder.api.task.TaskBootstrap;
import me.nelonn.propack.builder.impl.task.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class BuildConfiguration {
    private final StrictMode strictMode;
    private final Pattern dirIgnore;
    private final Pattern fileIgnore;
    private final int customModelDataStart;
    private final boolean generateItemModels;
    private final ObfuscationConfiguration obfuscationConfiguration;
    private final Map<String, String> allLangTranslations;
    private final Set<String> languages;
    private final PackageOptions packageOptions;
    private final Hosting hosting;
    private final Map<String, Object> uploadOptions;
    private final LinkedHashMap<String, TaskBootstrap> tasks;

    public BuildConfiguration(StrictMode strictMode,
                              @Nullable Pattern dirIgnore,
                              @Nullable Pattern fileIgnore,
                              int customModelDataStart,
                              boolean generateItemModels,
                              ObfuscationConfiguration obfuscationConfiguration,
                              Map<String, String> allLangTranslations,
                              Set<String> languages,
                              PackageOptions packageOptions,
                              @Nullable Hosting hosting,
                              @Nullable Map<String, Object> uploadOptions) {
        this.strictMode = strictMode;
        this.dirIgnore = dirIgnore;
        this.fileIgnore = fileIgnore;
        this.customModelDataStart = customModelDataStart;
        this.generateItemModels = generateItemModels;
        this.obfuscationConfiguration = obfuscationConfiguration;
        this.allLangTranslations = allLangTranslations;
        this.languages = languages;
        this.packageOptions = packageOptions;
        this.hosting = hosting;
        this.uploadOptions = uploadOptions == null ? null : new HashMap<>(uploadOptions);
        tasks = new LinkedHashMap<>();
        tasks.put("gatherSources", GatherSourcesTask.BOOTSTRAP);
        tasks.put("processModels", ProcessModelsTask.BOOTSTRAP);
        tasks.put("processSounds", ProcessSoundsTask.BOOTSTRAP);
        tasks.put("processArmorTextures", ProcessArmorTextures.BOOTSTRAP);
        tasks.put("processLanguages", ProcessLanguagesTask.BOOTSTRAP);
        tasks.put("processFonts", ProcessFontsTask.BOOTSTRAP);
        if (obfuscationConfiguration.isEnabled()) {
            tasks.put("obfuscate", ObfuscateTask.BOOTSTRAP);
        }
        tasks.put("sortAssets", SortAssetsTask.BOOTSTRAP);
        tasks.put("package", PackageTask.BOOTSTRAP);
        tasks.put("serialize", SerializeTask.BOOTSTRAP);
        if (hosting != null) {
            tasks.put("upload", UploadTask.BOOTSTRAP);
        }
    }

    public StrictMode getStrictMode() {
        return strictMode;
    }

    public @Nullable Pattern getDirIgnore() {
        return dirIgnore;
    }

    public @Nullable Pattern getFileIgnore() {
        return fileIgnore;
    }

    public int getCustomModelDataStart() {
        return customModelDataStart;
    }

    public boolean isGenerateItemModels() {
        return generateItemModels;
    }

    public ObfuscationConfiguration getObfuscationConfiguration() {
        return obfuscationConfiguration;
    }

    public Map<String, String> getAllLangTranslations() {
        return allLangTranslations;
    }

    public Set<String> getLanguages() {
        return languages;
    }

    public PackageOptions getPackageOptions() {
        return packageOptions;
    }

    public @Nullable Hosting getHosting() {
        return hosting;
    }

    public @Nullable Map<String, Object> getUploadOptions() {
        return uploadOptions == null ? null : new HashMap<>(uploadOptions);
    }

    public LinkedHashMap<String, TaskBootstrap> getTasks() {
        return tasks;
    }
}

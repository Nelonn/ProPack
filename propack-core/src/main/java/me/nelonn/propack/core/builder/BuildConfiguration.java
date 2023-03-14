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

package me.nelonn.propack.core.builder;

import me.nelonn.propack.builder.StrictMode;
import me.nelonn.propack.builder.hosting.Hosting;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.core.builder.task.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class BuildConfiguration {
    private final StrictMode strictMode;
    private final Pattern dirIgnore;
    private final Pattern fileIgnore;
    private final ObfuscationConfiguration obfuscationConfiguration;
    private final Map<String, String> allLangTranslations;
    private final Set<String> languages;
    private final PackageOptions packageOptions;
    private final Hosting hosting;
    private final Map<String, Object> uploadOptions;
    private final LinkedHashSet<TaskBootstrap> tasks;

    public BuildConfiguration(@NotNull StrictMode strictMode,
                              @Nullable Pattern dirIgnore,
                              @Nullable Pattern fileIgnore,
                              @NotNull ObfuscationConfiguration obfuscationConfiguration,
                              @NotNull Map<String, String> allLangTranslations,
                              @NotNull Set<String> languages,
                              @NotNull PackageOptions packageOptions,
                              @Nullable Hosting hosting,
                              @Nullable Map<String, Object> uploadOptions) {
        this.strictMode = strictMode;
        this.dirIgnore = dirIgnore;
        this.fileIgnore = fileIgnore;
        this.obfuscationConfiguration = obfuscationConfiguration;
        this.allLangTranslations = allLangTranslations;
        this.languages = languages;
        this.packageOptions = packageOptions;
        this.hosting = hosting;
        this.uploadOptions = uploadOptions == null ? null : new HashMap<>(uploadOptions);
        tasks = new LinkedHashSet<>();
        tasks.add(GatherSourcesTask.BOOTSTRAP);
        tasks.add(ProcessModelsTask.BOOTSTRAP);
        tasks.add(ProcessSoundsTask.BOOTSTRAP);
        tasks.add(ProcessArmorTextures.BOOTSTRAP);
        tasks.add(ProcessLanguagesTask.BOOTSTRAP);
        tasks.add(ProcessFontsTask.BOOTSTRAP);
        if (obfuscationConfiguration.isEnabled()) {
            tasks.add(ObfuscateTask.BOOTSTRAP);
        }
        tasks.add(SortAssetsTask.BOOTSTRAP);
        tasks.add(PackageTask.BOOTSTRAP);
        tasks.add(SerializeTask.BOOTSTRAP);
        if (hosting != null) {
            tasks.add(UploadTask.BOOTSTRAP);
        }
    }

    public @NotNull StrictMode getStrictMode() {
        return strictMode;
    }

    public @Nullable Pattern getDirIgnore() {
        return dirIgnore;
    }

    public @Nullable Pattern getFileIgnore() {
        return fileIgnore;
    }

    public @NotNull ObfuscationConfiguration getObfuscationConfiguration() {
        return obfuscationConfiguration;
    }

    public @NotNull Map<String, String> getAllLangTranslations() {
        return allLangTranslations;
    }

    public @NotNull Set<String> getLanguages() {
        return languages;
    }

    public @NotNull PackageOptions getPackageOptions() {
        return packageOptions;
    }

    public @Nullable Hosting getHosting() {
        return hosting;
    }

    public @Nullable Map<String, Object> getUploadOptions() {
        return uploadOptions == null ? null : new HashMap<>(uploadOptions);
    }

    public @NotNull Set<TaskBootstrap> getTasks() {
        return tasks;
    }
}

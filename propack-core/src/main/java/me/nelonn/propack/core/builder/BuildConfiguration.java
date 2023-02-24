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

import me.nelonn.propack.builder.Hosting;
import me.nelonn.propack.builder.StrictMode;
import me.nelonn.propack.builder.ZipPackager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class BuildConfiguration {
    private final StrictMode strictMode;
    private final Set<String> ignoredExtensions;
    private final ObfuscationConfiguration obfuscationConfiguration;
    private final Map<String, String> allLangTranslations;
    private final Set<String> languages;
    private final ZipPackager zipPackager;
    private final Map<String, Object> packageOptions;
    private final Hosting hosting;

    public BuildConfiguration(@NotNull StrictMode strictMode,
                              @NotNull Set<String> ignoredExtensions,
                              @NotNull ObfuscationConfiguration obfuscationConfiguration,
                              @NotNull Map<String, String> allLangTranslations,
                              @NotNull Set<String> languages,
                              @NotNull ZipPackager zipPackager,
                              @NotNull Map<String, Object> packageOptions,
                              @Nullable Hosting hosting) {
        this.strictMode = strictMode;
        this.ignoredExtensions = ignoredExtensions;
        this.obfuscationConfiguration = obfuscationConfiguration;
        this.allLangTranslations = allLangTranslations;
        this.languages = languages;
        this.zipPackager = zipPackager;
        this.packageOptions = packageOptions;
        this.hosting = hosting;
    }

    public @NotNull StrictMode getStrictMode() {
        return strictMode;
    }

    public @NotNull Set<String> getIgnoredExtensions() {
        return ignoredExtensions;
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

    public @NotNull ZipPackager getZipPackager() {
        return zipPackager;
    }

    public @NotNull Map<String, Object> getPackageOptions() {
        return packageOptions;
    }

    public @Nullable Hosting getHosting() {
        return hosting;
    }
}

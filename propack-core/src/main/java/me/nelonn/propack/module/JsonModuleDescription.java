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

package me.nelonn.propack.module;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <table border=1>
 * <caption>The description of the module.yml layout</caption>
 * <tr>
 *     <th>Node</th>
 *     <th>Method</th>
 *     <th>Summary</th>
 * </tr><tr>
 *     <td><code>name</code></td>
 *     <td>{@link #getName()}</td>
 *     <td>The unique name of plugin</td>
 * </tr><tr>
 *     <td><code>bootstrapper</code></td>
 *     <td>{@link #getBootstrapper()}</td>
 *     <td>The plugin's initial class file</td>
 * </tr><tr>
 *     <td><code>version</code></td>
 *     <td>{@link #getVersion()}</td>
 *     <td>A plugin revision identifier</td>
 * </tr><tr>
 *     <td><code>description</code></td>
 *     <td>{@link #getDescription()}</td>
 *     <td>Human readable plugin summary</td>
 * </tr><tr>
 *     <td><code>author</code><br><code>authors</code></td>
 *     <td>{@link #getAuthors()}</td>
 *     <td>The plugin authors</td>
 * </tr><tr>
 *     <td><code>contributors</code></td>
 *     <td>{@link #getContributors()}</td>
 *     <td>The plugin contributors</td>
 * </tr><tr>
 *     <td><code>website</code></td>
 *     <td>{@link #getWebsite()}</td>
 *     <td>The URL to the plugin's site</td>
 * </tr><tr>
 *     <td><code>logger-prefix</code></td>
 *     <td>{@link #getLoggerPrefix()}</td>
 *     <td>The token to prefix plugin log entries</td>
 * </tr>
 * </table>
 */
public class JsonModuleDescription implements ModuleDescription {
    private final String name;
    private final String bootstraper;
    private final String version;
    private final String description;
    private final List<String> authors;
    private final List<String> contributors;
    private final String website;
    private final String loggerPrefix;

    private JsonModuleDescription(String name, String bootstraper, String version, String description, List<String> authors, List<String> contributors, String website, String loggerPrefix) {
        this.name = name;
        this.bootstraper = bootstraper;
        this.version = version;
        this.description = description;
        this.authors = authors;
        this.contributors = contributors;
        this.website = website;
        this.loggerPrefix = loggerPrefix;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getBootstrapper() {
        return bootstraper;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public @NotNull List<String> getAuthors() {
        return authors;
    }

    @Override
    public @NotNull List<String> getContributors() {
        return contributors;
    }

    @Override
    public @Nullable String getWebsite() {
        return website;
    }

    @Override
    public @Nullable String getLoggerPrefix() {
        return loggerPrefix;
    }

    public static JsonModuleDescription deserialize(JsonObject jsonObject) throws InvalidDescriptionException {
        String name = GsonHelper.getString(jsonObject, "name");
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new InvalidDescriptionException("name '" + name + "' contains invalid characters.");
        }
        String bootstrapper = GsonHelper.getString(jsonObject, "bootstrapper");
        String version = GsonHelper.getString(jsonObject, "version");
        String description;
        if (jsonObject.has("description")) {
            description = GsonHelper.getString(jsonObject, "description");
        } else {
            description = null;
        }
        List<String> authors;
        if (jsonObject.has("authors")) {
            ImmutableList.Builder<String> authorsBuilder = ImmutableList.builder();
            if (jsonObject.get("author") != null) {
                authorsBuilder.add(GsonHelper.getString(jsonObject, "author"));
            }
            Util.forEachStringArray(GsonHelper.getArray(jsonObject, "authors"), "authors", authorsBuilder::add);
            authors = authorsBuilder.build();
        } else if (jsonObject.has("author")) {
            authors = ImmutableList.of(GsonHelper.getString(jsonObject, "author"));
        } else {
            authors = ImmutableList.of();
        }
        List<String> contributors;
        if (jsonObject.has("contributors")) {
            ImmutableList.Builder<String> contributorsBuilder = ImmutableList.<String>builder();
            Util.forEachStringArray(GsonHelper.getArray(jsonObject, "contributors"), "contributors", contributorsBuilder::add);
            contributors = contributorsBuilder.build();
        } else {
            contributors = ImmutableList.of();
        }
        String website;
        if (jsonObject.has("website")) {
            website = GsonHelper.getString(jsonObject, "website");
        } else {
            website = null;
        }
        String loggerPrefix;
        if (jsonObject.has("logger-prefix")) {
            loggerPrefix = GsonHelper.getString(jsonObject, "logger-prefix");
        } else {
            loggerPrefix = name;
        }
        return new JsonModuleDescription(name, bootstrapper, version, description, authors, contributors, website, loggerPrefix);
    }
}

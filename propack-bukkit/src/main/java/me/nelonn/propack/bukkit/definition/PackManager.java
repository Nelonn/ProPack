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

package me.nelonn.propack.bukkit.definition;

import com.google.gson.JsonObject;
import me.nelonn.flint.path.Key;
import me.nelonn.propack.bukkit.BukkitProPackCore;
import me.nelonn.propack.core.loader.ProjectLoader;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PackManager {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final Map<String, PackDefinition> definitions = new HashMap<>();
    private final BukkitProPackCore core;
    private final File directory;
    private final ProjectLoader projectLoader;

    public PackManager(@NotNull BukkitProPackCore core, @NotNull File directory) {
        this.core = core;
        this.directory = directory;
        projectLoader = core.getProjectLoader();
    }

    public void loadAll() {
        definitions.clear();
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) continue;
            String name = file.getName();
            if (!name.endsWith(".json")) continue;
            name = Util.substringLast(name, ".json");
            try {
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                JsonObject jsonObject = GsonHelper.deserialize(content);
                String type = GsonHelper.getString(jsonObject, "Type");
                if (type.equalsIgnoreCase("Project")) {
                    boolean buildAtStartup = GsonHelper.getBoolean(jsonObject, "BuildAtStartup", false);
                    File projectFile = new File(directory, name + File.separatorChar + "project.json5");
                    ProjectPack projectPack = new ProjectPack(projectFile, projectLoader, !buildAtStartup);
                    definitions.put(name, projectPack);
                } else if (type.equalsIgnoreCase("File")) {
                    throw new UnsupportedOperationException("Resource pack definition type 'File' currently not supported");
                } else {
                    Key id = Key.of(type);
                    DefinitionType definitionType = core.getDefinitionTypeMap().get(id);
                    if (definitionType == null) {
                        throw new NullPointerException("Pack type '" + id + "' not found");
                    }
                    definitions.put(name, definitionType.apply(jsonObject));
                }
            } catch (Exception e) {
                LOGGER.error("Unable to load '" + name + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        definitions.clear();
    }

    public @Nullable PackDefinition getDefinition(@NotNull String name) {
        return definitions.get(name);
    }

    public Collection<PackDefinition> getDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public @NotNull ProjectLoader getProjectLoader() {
        return projectLoader;
    }
}

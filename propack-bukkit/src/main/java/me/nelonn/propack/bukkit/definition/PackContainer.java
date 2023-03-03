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

package me.nelonn.propack.bukkit.definition;

import com.google.gson.JsonObject;
import me.nelonn.propack.core.ProPackCore;
import me.nelonn.propack.core.builder.InternalProject;
import me.nelonn.propack.core.loader.ProjectLoader;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.Util;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackContainer {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final Map<String, PackDefinition> definitions = new HashMap<>();
    private final ProjectLoader projectLoader;
    private final File directory;

    public PackContainer(@NotNull ProPackCore core, @NotNull File directory) {
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
                    InternalProject internalProject = projectLoader.load(projectFile, !buildAtStartup);
                    if (buildAtStartup || internalProject.getResourcePack().isEmpty()) {
                        internalProject.build(); // TODO: improve
                    }
                    ProjectDefinition resourcePackDefinition = new ProjectDefinition(internalProject);
                    definitions.put(name, resourcePackDefinition);
                } else if (type.equalsIgnoreCase("File")) {
                    throw new UnsupportedOperationException("Resource pack definition type 'File' currently not supported");
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

    public List<PackDefinition> getDefinitions() {
        return definitions.values().stream().toList();
    }

    public @NotNull ProjectLoader getProjectLoader() {
        return projectLoader;
    }
}

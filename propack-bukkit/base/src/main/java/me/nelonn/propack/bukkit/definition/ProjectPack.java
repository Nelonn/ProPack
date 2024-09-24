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

import com.google.gson.annotations.SerializedName;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.builder.ProPackBuilder;
import me.nelonn.propack.builder.impl.BuiltResourcePack;
import me.nelonn.propack.builder.impl.InternalProject;
import me.nelonn.propack.builder.impl.ProjectLoader;
import me.nelonn.propack.bukkit.SaveToFolderTask;
import me.nelonn.propack.bukkit.SimpleResourcePack;
import me.nelonn.propack.core.loader.ProPackFileLoader;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class ProjectPack implements PackDefinition {
    public static class Config {
        @SerializedName("BuildAtStartup")
        public boolean buildAtStartup = true;
        @SerializedName("ItemsAdderCompat")
        public boolean itemsAdderCompat = true;
    }

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final File file;
    private String name;
    private InternalProject project;
    private ProjectLoader projectLoader;
    private ProPackBuilder builder;
    private ResourcePack resourcePack;
    private final boolean itemsAdderCompat;

    public ProjectPack(File file, ProPackBuilder builder, ProjectLoader projectLoader, Config config) {
        this.file = file;
        this.projectLoader = projectLoader;
        this.builder = builder;
        this.itemsAdderCompat = config.itemsAdderCompat;
        if (config.buildAtStartup) {
            build();
        } else {
            loadOrBuild();
        }
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @Nullable ResourcePack getResourcePack() {
        return resourcePack;
    }

    public @NotNull File getFile() {
        return file;
    }

    public @NotNull InternalProject getProject() {
        return project;
    }

    public @NotNull ProjectLoader getProjectLoader() {
        return projectLoader;
    }

    public void setProjectLoader(@NotNull ProjectLoader projectLoader) {
        this.projectLoader = projectLoader;
    }

    public ProPackBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(ProPackBuilder builder) {
        this.builder = builder;
    }

    public void loadOrBuild() {
        project = projectLoader.load(file, true);
        name = project.name;
        if (project.getResourcePack() == null) {
            buildInternal();
        } else {
            resourcePack = project.getResourcePack$();
        }
    }

    public void build() {
        LOGGER.info("Running builder...");
        project = projectLoader.load(file, false);
        name = project.name;
        buildInternal();
    }

    private void buildInternal() {
        if (itemsAdderCompat) {
            Path iaDir = Bukkit.getServer().getPluginsFolder().toPath().resolve("ItemsAdder");
            Path destDir = iaDir.resolve("contents").resolve("propack").resolve("resourcepack");
            var tasks = project.getBuildConfiguration().getTasks();
            tasks.remove("package");
            tasks.remove("upload");
            tasks.put("copyToIA", (project) -> {
               var task = new SaveToFolderTask(project);
               task.destDir = destDir;
               return task;
            });
        }
        builder.build();
        project.build();
        LOGGER.info("Trying to load output file...");
        File builtResourcePack = new File(project.getBuildDir(), project.name + ".propack");
        if (!builtResourcePack.exists()) {
            throw new IllegalStateException("ProPack file not found, is the resource pack really built?");
        }
        ProPackFileLoader proPackFileLoader = new ProPackFileLoader();
        Resources resources = proPackFileLoader.load(builtResourcePack);
        BuiltResourcePack builtResourcePack1 = (BuiltResourcePack) project.getResourcePack();
        resourcePack = new SimpleResourcePack(project.name, resources, builtResourcePack, builtResourcePack1.getSha1(), builtResourcePack1.getUpload());
        LOGGER.info("Done");
    }
}

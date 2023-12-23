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

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.builder.impl.InternalProject;
import me.nelonn.propack.builder.impl.ProjectLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ProjectPack implements PackDefinition {
    private final File file;
    private InternalProject project;
    private ProjectLoader projectLoader;

    public ProjectPack(@NotNull File file, @NotNull ProjectLoader projectLoader, boolean tryLoadBuilt) {
        this.file = file;
        this.projectLoader = projectLoader;
        if (tryLoadBuilt) {
            loadOrBuild();
        } else {
            build();
        }
    }

    @Override
    public @NotNull String getName() {
        return project.getName();
    }

    @Override
    public @Nullable ResourcePack getResourcePack() {
        return project.getResourcePack();
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

    public void loadOrBuild() {
        project = projectLoader.load(file, true);
        if (project.getResourcePack() == null) {
            build0();
        }
    }

    public void build() {
        project = projectLoader.load(file, false);
        build0();
    }

    private void build0() {
        project.build(); // TODO: improve
    }
}

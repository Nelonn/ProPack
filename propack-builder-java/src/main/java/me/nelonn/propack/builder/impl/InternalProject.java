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

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.builder.api.Project;
import me.nelonn.propack.builder.api.file.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class InternalProject implements Project {
    public final String name;
    public final File projectDir;
    private File buildDir;
    public final BuildConfiguration buildConfiguration;
    public final VirtualFile metaFile;
    public final VirtualFile iconFile;
    private ResourcePack resourcePack;

    public InternalProject(@NotNull String name,
                           @NotNull File projectDir,
                           @NotNull BuildConfiguration buildConfiguration,
                           @NotNull VirtualFile metaFile,
                           @Nullable VirtualFile iconFile,
                           @Nullable ResourcePack resourcePack) {
        this.name = name;
        this.projectDir = projectDir;
        this.buildDir = new File(projectDir, "build");
        this.buildConfiguration = buildConfiguration;
        this.metaFile = metaFile;
        this.iconFile = iconFile;
        this.resourcePack = resourcePack;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public File getProjectDir() {
        return projectDir;
    }

    @Override
    public File getBuildDir() {
        return buildDir;
    }

    @Override
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    @Override
    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    @Override
    public VirtualFile getMetaFile() {
        return metaFile;
    }

    @Override
    public VirtualFile getIconFile() {
        return iconFile;
    }

    @Override
    public DefaultProjectBuilder createDefaultBuilder() {
        return new DefaultProjectBuilder(this);
    }

    @Override
    public @Nullable ResourcePack getResourcePack() {
        return resourcePack;
    }

    public void build() {
        resourcePack = createDefaultBuilder().build();
    }
}

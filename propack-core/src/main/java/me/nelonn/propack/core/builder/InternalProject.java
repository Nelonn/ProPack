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

import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.VirtualFile;
import me.nelonn.propack.definition.ItemDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class InternalProject implements Project {
    public final String name;
    public final File projectDir;
    private File buildDir;
    public final ItemDefinition itemDefinition;
    public final BuildConfiguration buildConfiguration;
    public final VirtualFile metaFile;
    public final VirtualFile iconFile;
    private LocalResourcePack resourcePack;

    public InternalProject(@NotNull String name,
                           @NotNull File projectDir,
                           @NotNull ItemDefinition itemDefinition,
                           @NotNull BuildConfiguration buildConfiguration,
                           @NotNull VirtualFile metaFile,
                           @Nullable VirtualFile iconFile) {
        this.name = name;
        this.projectDir = projectDir;
        this.buildDir = new File(projectDir, "build");
        this.itemDefinition = itemDefinition;
        this.buildConfiguration = buildConfiguration;
        this.metaFile = metaFile;
        this.iconFile = iconFile;
    }

    @Override
    public String getName() {
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
    public ItemDefinition getItemDefinition() {
        return itemDefinition;
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

    public void build() {
        resourcePack = createDefaultBuilder().build();
    }

    public LocalResourcePack getResourcePack() {
        return resourcePack;
    }
}

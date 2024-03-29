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

package me.nelonn.propack.builder.api;

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.builder.api.file.VirtualFile;
import me.nelonn.propack.builder.impl.BuildConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public interface Project {

    @NotNull String getName();

    File getProjectDir();

    File getBuildDir();

    void setBuildDir(File dir);

    BuildConfiguration getBuildConfiguration();

    VirtualFile getMetaFile();

    VirtualFile getIconFile();

    ProjectBuilder createDefaultBuilder();

    @Nullable ResourcePack getResourcePack();

    default @NotNull ResourcePack getResourcePack$() {
        return Objects.requireNonNull(this.getResourcePack());
    }

}

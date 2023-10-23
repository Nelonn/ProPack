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

package me.nelonn.propack.core.builder;

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.Sha1;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.builder.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;

public class BuiltResourcePack implements ResourcePack {
    private final Project project;
    private final Resources resources;
    private final File file;
    private final File zip;
    private final Sha1 sha1;
    private final UploadedPack uploadedPack;

    public BuiltResourcePack(@NotNull Project project,
                             @NotNull Resources resources,
                             @NotNull File file,
                             @NotNull File zip,
                             @NotNull Sha1 sha1,
                             @Nullable UploadedPack uploadedPack) {
        this.project = project;
        this.resources = resources;
        this.file = file;
        this.zip = zip;
        this.sha1 = sha1;
        this.uploadedPack = uploadedPack;
    }

    @Override
    public @NotNull String getName() {
        return project.getName();
    }

    @Override
    public @NotNull Resources resources() {
        return resources;
    }

    @Override
    public @NotNull Optional<UploadedPack> getUpload() {
        return Optional.ofNullable(uploadedPack);
    }

    public @NotNull Project getProject() {
        return project;
    }

    public @NotNull File getFile() {
        return file;
    }

    public @NotNull File getZip() {
        return zip;
    }

    public @NotNull Sha1 getSha1() {
        return sha1;
    }

}

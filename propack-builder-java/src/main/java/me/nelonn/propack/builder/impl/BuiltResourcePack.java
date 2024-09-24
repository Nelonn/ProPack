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
import me.nelonn.propack.Resources;
import me.nelonn.propack.Sha1;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.builder.api.Project;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BuiltResourcePack implements ResourcePack {
    private final Project project;
    private final Resources resources;
    private final File file;
    private final @Nullable File zip;
    private final @Nullable Sha1 sha1;
    private final @Nullable UploadedPack uploadedPack;

    public BuiltResourcePack(Project project,
                             Resources resources,
                             File file,
                             @Nullable File zip,
                             @Nullable Sha1 sha1,
                             @Nullable UploadedPack uploadedPack) {
        this.project = project;
        this.resources = resources;
        this.file = file;
        this.zip = zip;
        this.sha1 = sha1;
        this.uploadedPack = uploadedPack;
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public Resources resources() {
        return resources;
    }

    @Override
    public @Nullable UploadedPack getUpload() {
        return uploadedPack;
    }

    public Project getProject() {
        return project;
    }

    public @Nullable File getFile() {
        return file;
    }

    public @Nullable File getZip() {
        return zip;
    }

    public @Nullable Sha1 getSha1() {
        return sha1;
    }

}

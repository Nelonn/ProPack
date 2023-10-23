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

package me.nelonn.propack.core.builder.task;

import me.nelonn.propack.Sha1;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.hosting.Hosting;
import me.nelonn.propack.builder.task.AbstractTask;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

public class UploadTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = UploadTask::new;
    public static final Extra<UploadedPack> EXTRA_UPLOADED_PACK = new Extra<>(UploadedPack.class, "propack.upload.uploaded_pack");

    public UploadTask(@NotNull Project project) {
        super("upload", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        Hosting hosting = getProject().getBuildConfiguration().getHosting();
        if (hosting == null) {
            throw new IllegalArgumentException("Upload task is unavailable because hosting is not specified");
        }
        File zip = io.getExtras().get(PackageTask.EXTRA_ZIP);
        if (zip == null) {
            throw new NullPointerException("EXTRA_ZIP");
        }
        Sha1 sha1 = io.getExtras().get(PackageTask.EXTRA_SHA1);
        if (sha1 == null) {
            throw new NullPointerException("EXTRA_SHA1");
        }
        try {
            UploadedPack uploadedPack = hosting.upload(zip, sha1, getProject().getName(), getProject().getBuildConfiguration().getUploadOptions());
            io.getExtras().put(EXTRA_UPLOADED_PACK, uploadedPack);
        } catch (Exception e) {
            LOGGER.error("Unable to upload '" + getProject().getName() + "'", e);
        }
    }
}

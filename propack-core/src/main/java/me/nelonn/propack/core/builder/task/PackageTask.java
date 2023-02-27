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

package me.nelonn.propack.core.builder.task;

import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.core.builder.BuildConfiguration;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.Sha1;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PackageTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final Extra<File> EXTRA_ZIP = new Extra<>(File.class, "propack.package.zip");
    public static final Extra<File> EXTRA_SHA1_FILE = new Extra<>(File.class, "propack.package.sha1_file");
    public static final Extra<Sha1> EXTRA_SHA1 = new Extra<>(Sha1.class, "propack.package.sha1");

    public PackageTask(@NotNull Project project) {
        super("package", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        File buildDir = getProject().getBuildDir();
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }
        File zip = new File(buildDir, getProject().getName() + ".zip");
        if (zip.exists()) {
            try {
                Files.delete(zip.toPath());
            } catch (Exception e) {
                LOGGER.error("Unable to delete " + zip, e);
            }
        }
        File sha1File = new File(buildDir, getProject().getName() + ".sha1");
        if (sha1File.exists()) {
            try {
                Files.delete(sha1File.toPath());
            } catch (Exception e) {
                LOGGER.error("Unable to delete " + sha1File, e);
            }
        }
        BuildConfiguration buildConfiguration = getProject().getBuildConfiguration();
        buildConfiguration.getZipPackager().packageFiles(zip, io.getFiles(), buildConfiguration.getPackageOptions());
        io.getExtras().put(EXTRA_ZIP, zip);
        me.nelonn.propack.Sha1 sha1;
        try (InputStream inputStream = Files.newInputStream(zip.toPath())) {
            sha1 = Sha1.hash(inputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash sha1 file", e);
        }
        io.getExtras().put(EXTRA_SHA1, sha1);
        try (OutputStream outputStream = Files.newOutputStream(sha1File.toPath())) {
            outputStream.write(sha1.toString().getBytes(StandardCharsets.UTF_8));
            io.getExtras().put(EXTRA_SHA1_FILE, sha1File);
        } catch (Exception e) {
            LOGGER.error("Unable to write " + sha1File.getName(), e);
        }
    }
}

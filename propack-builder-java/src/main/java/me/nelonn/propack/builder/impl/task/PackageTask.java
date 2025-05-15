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

package me.nelonn.propack.builder.impl.task;

import me.nelonn.propack.Sha1;
import me.nelonn.propack.builder.api.Project;
import me.nelonn.propack.builder.api.task.AbstractTask;
import me.nelonn.propack.builder.api.task.FileCollection;
import me.nelonn.propack.builder.api.task.TaskBootstrap;
import me.nelonn.propack.builder.api.task.TaskIO;
import me.nelonn.propack.builder.api.util.Extra;
import me.nelonn.propack.builder.impl.PackageOptions;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackageTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = PackageTask::new;
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
        packageFiles(zip, io.getFiles(), getProject().getBuildConfiguration().getPackageOptions());
        io.getExtras().put(EXTRA_ZIP, zip);
        me.nelonn.propack.Sha1 sha1;
        try (InputStream inputStream = Files.newInputStream(zip.toPath())) {
            sha1 = Sha1.fromInputStream(inputStream);
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

    private final static FileTime ZERO_TIME = FileTime.fromMillis(0L);
    private final static Random RANDOM = new Random();

    private static int random(int min, int max) {
        return RANDOM.nextInt((max - min) + 1) + min;
    }

    private void packageFiles(@NotNull File output, @NotNull FileCollection input, @NotNull PackageOptions options) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(output);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream, StandardCharsets.UTF_8)) {
            zipOutputStream.setLevel(options.compressionLevel);
            zipOutputStream.setComment(options.comment);
            for (me.nelonn.propack.builder.api.file.File file : input) {
                final ZipEntry zipEntry = new ZipEntry(file.getPath());
                if (options.protection) {
                    zipEntry.setCrc(0L);
                    zipEntry.setLastAccessTime(ZERO_TIME);
                    zipEntry.setCreationTime(ZERO_TIME);
                    zipEntry.setLastModifiedTime(ZERO_TIME);
                    if (file.getPath().endsWith(".ogg")) {
                        zipEntry.setSize(random(9000, 15000));
                    } else {
                        zipEntry.setSize(1111L);
                    }
                }
                zipOutputStream.putNextEntry(zipEntry);
                try (InputStream inputStream = file.openInputStream()) {
                    final byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) >= 0) {
                        zipOutputStream.write(buffer, 0, read);
                    }
                    zipOutputStream.closeEntry();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot package files", e);
        }
    }
}

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

import me.nelonn.propack.Sha1;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.task.FileCollection;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.core.builder.PackageOptions;
import me.nelonn.propack.builder.task.AbstractTask;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackageTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = GatherSourcesTask::new;
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

    private void packageFiles(@NotNull File output, @NotNull FileCollection input, @NotNull PackageOptions options) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(output);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream, StandardCharsets.UTF_8)) {
            zipOutputStream.setLevel(options.compressionLevel);
            zipOutputStream.setComment(options.comment);
            for (me.nelonn.propack.builder.file.File file : input) {
                final ZipEntry zipEntry = new ZipEntry(file.getPath());
                zipEntry.setLastModifiedTime(FileTime.fromMillis(0L));
                zipOutputStream.putNextEntry(zipEntry);
                try (InputStream inputStream = file.openInputStream()) {
                    final byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) >= 0) {
                        zipOutputStream.write(buffer, 0, read);
                    }
                    zipOutputStream.closeEntry();
                    if (options.protection) {
                        zipEntry.setCrc(buffer.length);
                        zipEntry.setSize(new BigInteger(buffer).mod(BigInteger.valueOf(Long.MAX_VALUE)).longValue());
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot package files", e);
        }
    }
}

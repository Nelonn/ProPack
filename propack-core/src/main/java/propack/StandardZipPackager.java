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

package propack;

import me.nelonn.propack.builder.ZipPackager;
import me.nelonn.propack.builder.task.FileCollection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StandardZipPackager implements ZipPackager {

    @Override
    public void packageFiles(@NotNull File output, @NotNull FileCollection input, @NotNull Map<String, Object> options) {
        int compressionLevel;
        Object compressionRaw = options.getOrDefault("compression", "BEST_COMPRESSION");
        if (compressionRaw instanceof String) {
            String string = (String) compressionRaw;
            try {
                compressionLevel = Deflater.class.getDeclaredField(string).getInt(null);
            } catch (Exception e) {
                throw new IllegalArgumentException("Compression level with name '" + string + "' not found");
            }
        } else if (compressionRaw instanceof Number) {
            compressionLevel = ((Number) compressionRaw).intValue();
        } else {
            throw new IllegalArgumentException("Expected 'compression' to be a string or number");
        }

        Object protectionRaw = options.getOrDefault("protection", false);
        if (!(protectionRaw instanceof Boolean)) {
            throw new IllegalArgumentException("Expected 'compression' to be a boolean");
        }
        boolean protection = (boolean) protectionRaw;

        Object commentRaw = options.getOrDefault("comment", "");
        if (!(commentRaw instanceof String)) {
            throw new IllegalArgumentException("Expected 'comment' to be a string");
        }
        String comment = (String) commentRaw;

        try (
                final FileOutputStream fileOutputStream = new FileOutputStream(output);
                final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream, StandardCharsets.UTF_8)
        ) {
            zipOutputStream.setLevel(compressionLevel);
            zipOutputStream.setComment(comment);
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
                    if (protection) {
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

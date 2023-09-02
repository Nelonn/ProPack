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

package me.nelonn.propack.core.util;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class IOUtil {
    public static byte[] readAllBytes(@NotNull InputStream in) throws IOException {
        return readNBytes(in, Integer.MAX_VALUE);
    }

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    public static byte[] readNBytes(@NotNull InputStream in, int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, DEFAULT_BUFFER_SIZE)];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = in.read(buf, nread, Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;
            }

            if (nread > 0) {
                if (MAX_BUFFER_SIZE - total < nread) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                if (nread < buf.length) {
                    buf = Arrays.copyOfRange(buf, 0, nread);
                }
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ?
                    result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }

    public static long transferTo(@NotNull InputStream in, @NotNull OutputStream out) throws IOException {
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    public static String readString(@NotNull File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
        }
    }

    /**
     * Extract files from jar
     *
     * @param source the class from which to take jar
     * @param from   directory in jar, example: "example/"
     * @param to     output file
     */
    public static void extractResources(@NotNull Class<?> source, @NotNull String from, @NotNull File to) {
        try {
            ZipInputStream jar;
            try {
                jar = new JarInputStream(source.getProtectionDomain().getCodeSource().getLocation().openStream());
            } catch (Exception e) {
                throw new IllegalStateException("An error occurred browsing the zip", e);
            }

            ZipEntry entry = jar.getNextEntry();
            while (entry != null) {
                String absolutePath = entry.getName();
                if (!entry.isDirectory() && absolutePath.startsWith(from)) {
                    String relativePath = absolutePath.substring(from.length());
                    File outFile = new File(to, relativePath);
                    int lastIndex = relativePath.lastIndexOf('/');
                    File outDir = new File(to, relativePath.substring(0, Math.max(lastIndex, 0)));
                    if (!outDir.exists()) {
                        outDir.mkdirs();
                    }
                    extractResource(source, absolutePath, outFile);
                }
                entry = jar.getNextEntry();
            }
            jar.closeEntry();
            jar.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void extractResource(@NotNull Class<?> source, @NotNull String input, @NotNull File output) {
        try {
            URL url = source.getClassLoader().getResource(input);
            if (url == null) {
                throw new IllegalArgumentException("The embedded resource '" + input + "' cannot be found in jar");
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            try (InputStream in = connection.getInputStream()) {
                if (in == null) {
                    throw new IllegalArgumentException("The embedded resource '" + input + "' cannot be found in jar");
                }
                try {
                    if (output.exists()) return;
                    try (OutputStream out = Files.newOutputStream(output.toPath())) {
                        byte[] buffer = new byte[1024];
                        int bytes;
                        while ((bytes = in.read(buffer)) > 0) {
                            out.write(buffer, 0, bytes);
                        }
                    }
                } catch (IOException e) {
                    //LOGGER.error("Could not save " + outFile.getName() + " to " + outFile, e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private IOUtil() {
        throw new UnsupportedOperationException();
    }
}

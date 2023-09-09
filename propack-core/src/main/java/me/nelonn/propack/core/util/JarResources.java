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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JarResources {

    public static void extractDirectory(@NotNull Object source, @NotNull String input, @NotNull File output) {
        extractFile(source.getClass(), input, output);
    }

    /**
     * Extract files from jar
     *
     * @param source the class from which to take jar
     * @param from   directory in jar, example: "example/"
     * @param to     output file
     */
    public static void extractDirectory(@NotNull Class<?> source, @NotNull String from, @NotNull File to) {
        from = from.replaceAll("\\\\", "/");
        if (!from.endsWith("/")) {
            from += '/';
        }
        try (ZipInputStream jar = new JarInputStream(source.getProtectionDomain().getCodeSource().getLocation().openStream())) {
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
                    extractFile(source, absolutePath, outFile);
                }
                entry = jar.getNextEntry();
            }
            jar.closeEntry();
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred browsing the jar", e);
        }
    }

    public static void extractFile(@NotNull Object source, @NotNull String input, @NotNull File output) {
        extractFile(source.getClass(), input, output);
    }

    public static void extractFile(@NotNull Class<?> source, @NotNull String input, @NotNull File output) {
        extractFile(source.getClassLoader(), input, output);
    }

    public static void extractFile(@NotNull ClassLoader classLoader, @NotNull String input, @NotNull File output) {
        input = input.replaceAll("\\\\", "/");
        if (input.endsWith("/")) {
            throw new IllegalArgumentException("Directories cannot be extracted as file");
        }
        try {
            URL url = classLoader.getResource(input);
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

    private JarResources() {
        throw new UnsupportedOperationException();
    }
}
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

package me.nelonn.propack.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public final class SharedLoader {
    private static final String DOT_SHARED = ".shared";
    private final Plugin plugin;
    private final File sharedDir;

    public SharedLoader(@NotNull Plugin plugin) {
        this.plugin = plugin;
        File rootDir = Bukkit.getServer().getPluginsFolder().getParentFile();
        sharedDir = new File(rootDir, "libraries" + File.separatorChar + DOT_SHARED);
    }

    public void loadIfNotExists(@NotNull Library library) {
        try {
            Class.forName(library.getCheckClass());
            return;
        } catch (Exception ignored) {
        }
        try {
            File file = new File(sharedDir, library.getFileName());
            if (!file.exists()) {
                sharedDir.mkdirs();
                try (InputStream in = plugin.getResource(library.getFileName() + DOT_SHARED);
                     OutputStream out = Files.newOutputStream(file.toPath())) {
                    assert in != null;
                    in.transferTo(out);
                }
            }
            Bukkit.getPluginManager().loadPlugin(file);
            Class.forName(library.getCheckClass());
        } catch (Exception e) {
            throw new RuntimeException("Unable to load shared library '" + library.getFileName() + "'", e);
        }
    }

    public static class Library {
        private final String fileName;
        private final String checkClass;

        public Library(@NotNull String fileName, @NotNull String checkClass) {
            this.fileName = fileName;
            this.checkClass = checkClass;
        }

        public @NotNull String getFileName() {
            return fileName;
        }

        public @NotNull String getCheckClass() {
            return checkClass;
        }
    }
}

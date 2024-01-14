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

package me.nelonn.propack.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class DependencyManager {
    private static final String META_INF = "META-INF";
    private static final String LIBRARIES = "libraries";
    private static final String DOT_LIBRARY = ".library";
    private boolean valid = true;
    private final Plugin plugin;
    private URLClassLoader classLoader;
    private Method addURLMethod;
    private final File librariesDirectory;

    public DependencyManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        if (plugin.getClass().getClassLoader() instanceof URLClassLoader classLoader) {
            this.classLoader = classLoader;
        } else {
            valid = false;
        }
        // TODO: not working
        try {
            addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURLMethod.setAccessible(true);
        } catch (Throwable e) {
            addURLMethod = null;
            valid = false;
        }
        File rootDir = Bukkit.getServer().getUpdateFolderFile().getParentFile().getParentFile();
        librariesDirectory = new File(rootDir, LIBRARIES);
    }

    public void addLibrary(@NotNull JarLibrary library) {
        try {
            Class.forName(library.getCheckClass());
            return;
        } catch (Throwable ignored) {
        }
        try {
            File file = new File(librariesDirectory, library.getName());
            if (!file.exists()) {
                try (InputStream in = plugin.getResource(META_INF + '/' + library.getName() + DOT_LIBRARY)) {
                    if (in == null) {
                        throw new RuntimeException("Library '" + library.getName() + "' not found in plugin jar");
                    }
                    try (OutputStream out = Files.newOutputStream(file.toPath())) {
                        in.transferTo(out);
                    }
                }
            }
            //addToClasspath(file.toURI().toURL());
            Bukkit.getPluginManager().loadPlugin(file);
            Class.forName(library.getCheckClass());
        } catch (Throwable e) {
            throw new RuntimeException("Unable to load library '" + library.getName() + "'", e);
        }
    }

    public void addToClasspath(@NotNull URL url) {
        if (!valid) return;
        try {
            addURLMethod.invoke(classLoader, url);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static class JarLibrary {
        private final String name;
        private final String checkClass;

        public JarLibrary(@NotNull String name, @NotNull String checkClass) {
            this.name = name;
            this.checkClass = checkClass;
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull String getCheckClass() {
            return checkClass;
        }
    }
}

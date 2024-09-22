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

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("all")
public class LibrariesLoader implements PluginLoader {
    private static final String META_INF = "META-INF";
    private static final String LIBRARIES = "libraries";
    private static final String DOT_LIBRARY = ".library";
    private static final String LIBRARY_NAME = "flint-path-0.0.1.jar";

    public static void extractFile(@NotNull Path source, @NotNull String input, @NotNull Path output) {
        input = input.replaceAll("\\\\", "/");
        if (input.endsWith("/")) {
            throw new IllegalArgumentException("Directories cannot be extracted as file");
        }
        try (ZipFile zf = new ZipFile(source.toFile())) {
            ZipEntry entry = zf.getEntry(input);
            if (entry == null) {
                throw new IllegalArgumentException("The embedded resource '" + input + "' cannot be found in jar");
            }
            try (InputStream is = zf.getInputStream(entry)) {
                extractFile(is, output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void extractFile(@NotNull InputStream in, @NotNull Path output) {
        try {
            if (Files.exists(output)) return;
            try (OutputStream out = Files.newOutputStream(output)) {
                byte[] buffer = new byte[1024];
                int bytes;
                while ((bytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not save " + output.getFileName(), e);
        }
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        Path librariesDir = Path.of(LIBRARIES);
        Path libFile = librariesDir.resolve(LIBRARY_NAME);
        if (!Files.exists(libFile)) {
            extractFile(classpathBuilder.getContext().getPluginSource(), META_INF + "/" + LIBRARY_NAME + DOT_LIBRARY, libFile);
        }

        classpathBuilder.addLibrary(new JarLibrary(libFile));

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("net.kyori:adventure-platform-bukkit:4.3.3"), null));
        resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        classpathBuilder.addLibrary(resolver);
    }
}

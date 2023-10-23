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

package me.nelonn.propack.module;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.nelonn.propack.core.ProPackCore;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JavaModuleManager implements ModuleManager {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final Map<String, Module> modules = new HashMap<>();
    private final ProPackCore core;
    private final File modulesDir;

    public JavaModuleManager(@NotNull ProPackCore core, @NotNull File modulesDir) {
        this.core = core;
        this.modulesDir = modulesDir;
    }

    @Override
    public @Nullable Module getModule(@NotNull String name) {
        return null;
    }

    @Override
    public @NotNull Module[] getModules() {
        return modules.values().toArray(new Module[0]);
    }

    @Override
    public void loadAll() throws IOException {
        disableAll();
        DirectoryStream<Path> stream = Files.newDirectoryStream(modulesDir.toPath(), p -> {
            return p.toFile().isFile() && p.toString().endsWith(".jar");
        });
        for (Path path : stream) {
            try {
                loadModule(path.toFile());
            } catch (Exception e) {
                LOGGER.error("Unable to load module '" + path.getFileName() + "'", e);
            }
        }
        stream.close();
    }

    public @NotNull Module loadModule(@NotNull File file) {
        file = file.getAbsoluteFile();
        JsonModuleDescription description;
        try {
            description = getModuleDescription(file);
        } catch (InvalidDescriptionException e) {
            throw new IllegalArgumentException("Invalid module", e);
        }
        File dataFolder = new File(modulesDir, description.getName());
        try {
            URLClassLoader child = new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());
            Class<?> bootstrapper = Class.forName(description.getBootstrapper(), true, child);
            if (!ModuleBootstrap.class.isAssignableFrom(bootstrapper)) {
                throw new IllegalArgumentException("Bootstrapper class must implement '" + ModuleBootstrap.class.getName() + "'");
            }
            ModuleBootstrap moduleBootstrap = (ModuleBootstrap) bootstrapper.getDeclaredConstructor().newInstance();
            ModuleProviderContext context = new ModuleProviderContextImpl(description, dataFolder.toPath());
            moduleBootstrap.bootstrap(context);
            JavaModule module = moduleBootstrap.createModule(context);
            module.init(description, new File(modulesDir, description.getName()), file);
            module.enable();
            modules.put(description.getName(), module);
            return module;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull JsonModuleDescription getModuleDescription(@NotNull File file) throws InvalidDescriptionException {
        JarFile jar = null;
        InputStream stream = null;
        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("propack_module.json");
            if (entry == null) {
                 entry = jar.getJarEntry("module.json");
                if (entry == null) {
                    throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain propack_module.json"));
                }
            }

            stream = jar.getInputStream(entry);

            String moduleDescriptionString = IOUtils.toString(stream, StandardCharsets.UTF_8);
            JsonObject jsonObject = GsonHelper.deserialize(moduleDescriptionString);
            return JsonModuleDescription.deserialize(jsonObject);
        } catch (IOException | JsonParseException ex) {
            throw new InvalidDescriptionException(ex);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignored) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void disableAll() {
        for (Module module : modules.values()) {
            module.disable();
        }
        modules.clear();
    }
}

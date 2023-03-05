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

package me.nelonn.propack.module;

import com.google.gson.JsonObject;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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

public class JavaModuleManager implements ModuleManager {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final Map<String, Module> modules = new HashMap<>();
    private final File modulesDir;

    public JavaModuleManager(@NotNull File modulesDir) {
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
                URLClassLoader child = new URLClassLoader(new URL[]{path.toUri().toURL()}, getClass().getClassLoader());
                InputStream inputStream = child.getResourceAsStream("module.json");
                if (inputStream == null) continue;
                String moduleDescriptionString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                JsonObject jsonObject = GsonHelper.deserialize(moduleDescriptionString);
                JsonModuleDescription moduleMeta = JsonModuleDescription.deserialize(jsonObject);
                try {
                    Class<?> bootstrapper = Class.forName(moduleMeta.getBootstrapper(), true, child);
                    if (!ModuleBootstrap.class.isAssignableFrom(bootstrapper)) {
                        throw new IllegalArgumentException("Bootstrapper class must implement '" + ModuleBootstrap.class.getName() + "'");
                    }
                    ModuleBootstrap moduleBootstrap = (ModuleBootstrap) bootstrapper.getDeclaredConstructor().newInstance();
                    File dataDir = new File(path.getParent().toFile(), moduleMeta.getName());
                    ModuleProviderContext context = new ModuleProviderContextImpl(moduleMeta, dataDir.toPath());
                    moduleBootstrap.bootstrap(context);
                    JavaModule module = moduleBootstrap.createModule(context);
                    module.enable();
                    modules.put(moduleMeta.getName(), module);
                } catch (Exception e) {
                    LOGGER.error("Error occurred while enabling " + moduleMeta.getDisplayName() + " (Is it up to date?)", e);
                }
            } catch (Exception e) {
                LOGGER.error("Unable to load module '" + path.getFileName() + "'", e);
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

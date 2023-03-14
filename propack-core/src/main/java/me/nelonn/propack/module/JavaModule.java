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

import me.nelonn.propack.core.ProPackCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public abstract class JavaModule implements Module {
    private boolean enabled = false;
    private ProPackCore core = null;
    private ModuleDescription meta = null;
    private File dataFolder = null;
    private File file = null;
    private Logger logger = null;

    @NotNull
    @Override
    public ModuleDescription getDescription() {
        return meta;
    }

    @NotNull
    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public @Nullable InputStream getResource(@NotNull String file) {
        try {
            URL url = getClass().getClassLoader().getResource(file);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public final void enable() {
        if (enabled) return;
        enabled = true;
        onEnable();
    }

    protected void onEnable() {
    }

    @Override
    public final void disable() {
        if (!enabled) return;
        enabled = false;
        onDisable();
    }

    protected void onDisable() {
    }

    @Override
    public final void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull ProPackCore getCore() {
        return core;
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    protected final @Nullable Reader getTextResource(@NotNull String file) {
        final InputStream in = getResource(file);
        return in == null ? null : new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    @NotNull
    protected final File getFile() {
        return file;
    }

    public final void init(@NotNull ProPackCore core, @NotNull ModuleDescription meta, @NotNull File dataFolder, @NotNull File file) {
        this.core = core;
        this.meta = meta;
        this.dataFolder = dataFolder;
        this.file = file;
        this.logger = LogManager.getLogger(meta.getLoggerPrefix());
    }
}

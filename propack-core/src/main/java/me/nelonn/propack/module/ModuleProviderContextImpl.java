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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ModuleProviderContextImpl implements ModuleProviderContext {
    private final ModuleDescription description;
    private final Path dataDirectory;
    private final Logger logger;

    public ModuleProviderContextImpl(@NotNull ModuleDescription description, @NotNull Path dataDirectory) {
        this.description = description;
        this.dataDirectory = dataDirectory;
        this.logger = LogManager.getLogger(description.getLoggerPrefix());
    }

    @Override
    public @NotNull ModuleDescription getDescription() {
        return description;
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }
}

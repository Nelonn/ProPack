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

package me.nelonn.propack.core;

import me.nelonn.propack.builder.hosting.HostingMap;
import me.nelonn.propack.core.loader.ProjectLoader;
import me.nelonn.propack.module.JavaModuleManager;
import me.nelonn.propack.module.ModuleManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ProPackCore {
    private final HostingMap hostingMap;
    private final ProjectLoader projectLoader;
    private final ModuleManager moduleManager;

    public ProPackCore(@NotNull File directory) {
        this.hostingMap = new SimpleHostingMap();
        this.projectLoader = new ProjectLoader(this);
        this.moduleManager = new JavaModuleManager(new File(directory, "modules"));
    }

    public HostingMap getHostingMap() {
        return hostingMap;
    }

    public ProjectLoader getProjectLoader() {
        return projectLoader;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}

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

import me.nelonn.propack.builder.JarBinarySource;
import me.nelonn.propack.builder.ProPackBuilder;
import me.nelonn.propack.builder.impl.ProPackCore;
import me.nelonn.propack.bukkit.definition.DefinitionTypeMap;
import me.nelonn.propack.bukkit.definition.PackManager;
import me.nelonn.propack.bukkit.dispatcher.ActivePackStoreMap;
import me.nelonn.propack.bukkit.dispatcher.Dispatcher;
import me.nelonn.propack.bukkit.dispatcher.MemoryActivePackStore;
import org.jetbrains.annotations.NotNull;

public class BukkitProPackCore extends ProPackCore {
    private final DefinitionTypeMap definitionTypeMap;
    private final PackManager packManager;
    private final ActivePackStoreMap activePackStoreMap;
    private final Dispatcher dispatcher;
    private final ProPackBuilder builder;

    public BukkitProPackCore(@NotNull ProPackPlugin plugin) {
        super(plugin.getDataFolder());
        definitionTypeMap = new DefinitionTypeMap();
        packManager = new PackManager(this, plugin.getDataFolder());
        activePackStoreMap = new ActivePackStoreMap();
        MemoryActivePackStore memoryStore = new MemoryActivePackStore(plugin);
        activePackStoreMap.register("memory_store", memoryStore);
        dispatcher = new Dispatcher(plugin, memoryStore);
        try {
            JarBinarySource jarBinarySource = new JarBinarySource(plugin.getFile().toPath());
            builder = new ProPackBuilder(jarBinarySource.getBinaryPath());
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize builder", e);
        }
    }

    public DefinitionTypeMap getDefinitionTypeMap() {
        return definitionTypeMap;
    }

    public PackManager getPackManager() {
        return packManager;
    }

    public ActivePackStoreMap getActivePackStoreMap() {
        return activePackStoreMap;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public ProPackBuilder getBuilder() {
        return builder;
    }
}

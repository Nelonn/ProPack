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

import me.nelonn.propack.bukkit.definition.PackManager;
import me.nelonn.propack.bukkit.dispatcher.Dispatcher;
import me.nelonn.propack.bukkit.dispatcher.MemoryStore;
import me.nelonn.propack.bukkit.dispatcher.StoreMap;
import me.nelonn.propack.core.ProPackCore;
import org.jetbrains.annotations.NotNull;

public class BukkitProPackCore extends ProPackCore {
    private final PackManager packManager;
    private final StoreMap storeMap;
    private final Dispatcher dispatcher;

    public BukkitProPackCore(@NotNull ProPackPlugin plugin) {
        super(plugin.getDataFolder());
        packManager = new PackManager(this, plugin.getDataFolder());
        storeMap = new StoreMap();
        storeMap.register("memory_store", new MemoryStore(plugin));
        dispatcher = new Dispatcher(plugin);
    }

    public PackManager getPackManager() {
        return packManager;
    }

    public StoreMap getStoreMap() {
        return storeMap;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}

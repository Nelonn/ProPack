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

import com.google.gson.JsonObject;
import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.bukkit.definition.DefinitionTypeMap;
import me.nelonn.propack.bukkit.definition.PackDefinition;
import me.nelonn.propack.bukkit.definition.PackManager;
import me.nelonn.propack.bukkit.dispatcher.ActivePackStoreMap;
import me.nelonn.propack.bukkit.dispatcher.Dispatcher;
import me.nelonn.propack.bukkit.dispatcher.MemoryActivePackStore;
import me.nelonn.propack.core.ProPackCore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BukkitProPackCore extends ProPackCore {
    private final DefinitionTypeMap definitionTypeMap;
    private final PackManager packManager;
    private final ActivePackStoreMap activePackStoreMap;
    private final Dispatcher dispatcher;
    public Map<Identifier, Function<JsonObject, PackDefinition>> tempDefs = new HashMap<>();

    public BukkitProPackCore(@NotNull ProPackPlugin plugin) {
        super(plugin.getDataFolder());
        definitionTypeMap = new DefinitionTypeMap();
        packManager = new PackManager(this, plugin.getDataFolder());
        activePackStoreMap = new ActivePackStoreMap();
        MemoryActivePackStore memoryStore = new MemoryActivePackStore(plugin);
        activePackStoreMap.register("memory_store", memoryStore);
        dispatcher = new Dispatcher(plugin, memoryStore);
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
}

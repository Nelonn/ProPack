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
import me.nelonn.propack.builder.loader.ItemDefinitionLoader;
import me.nelonn.propack.builder.util.Extras;
import me.nelonn.propack.definition.Item;
import me.nelonn.propack.definition.ItemDefinition;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BukkitItemDefinitionLoader implements ItemDefinitionLoader {
    public static final BukkitItemDefinitionLoader INSTANCE = new BukkitItemDefinitionLoader();
    private final ItemDefinition itemDefinition;

    @SuppressWarnings("deprecation")
    private BukkitItemDefinitionLoader() {
        Map<Identifier, Item> itemMap = new HashMap<>();
        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;
            Identifier id = Identifier.of(material.getKey().getKey(), material.getKey().getNamespace());
            itemMap.put(id, new Item(id, material.isBlock()));
        }
        itemDefinition = new ItemDefinition(itemMap);
    }

    @Override
    public @NotNull String getType() {
        return "Bukkit";
    }

    @Override
    public @NotNull ItemDefinition load(@NotNull JsonObject input, @NotNull Extras extras) {
        return itemDefinition;
    }
}

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
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BukkitItemDefinitionLoader implements ItemDefinitionLoader {
    public static final BukkitItemDefinitionLoader INSTANCE = new BukkitItemDefinitionLoader();
    private final ItemDefinition itemDefinition;

    private BukkitItemDefinitionLoader() {
        itemDefinition = BukkitItemDefinition.INSTANCE;
    }

    @Override
    public @NotNull String getType() {
        return "Bukkit";
    }

    @Override
    public @NotNull ItemDefinition load(@NotNull JsonObject input, @NotNull Extras extras) {
        return itemDefinition;
    }

    @SuppressWarnings("deprecation")
    public static class BukkitItemDefinition implements ItemDefinition {
        public static final BukkitItemDefinition INSTANCE = new BukkitItemDefinition();

        private BukkitItemDefinition() {
        }

        @Override
        public @Nullable Item getItem(@NotNull Identifier identifier) {
            Material material = Material.matchMaterial(identifier.asString());
            return material == null ? null : transform(material);
        }

        @Override
        public @NotNull Collection<Item> getItems() {
            return Arrays.stream(Material.values()).collect(HashSet::new, (items, material) -> {
                if (!material.isLegacy()) {
                    items.add(transform(material));
                }
            }, HashSet::addAll);
        }

        public @NotNull Item transform(@NotNull Material material) {
            if (material.isLegacy()) {
                throw new IllegalArgumentException("Legacy material is not allowed");
            }
            NamespacedKey key = material.getKey();
            return new Item(Identifier.of(key.getKey(), key.getNamespace()), material.isBlock());
        }
    }
}

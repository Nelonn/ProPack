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

package me.nelonn.propack.builder.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.nelonn.flint.path.Key;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.MeshesMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MeshesMapBuilder {
    private final Map<Key, ItemEntry> mappers = new HashMap<>();

    public @NotNull MeshesMapBuilder.ItemEntry getMapper(@NotNull Key itemId) {
        return mappers.computeIfAbsent(itemId, ItemEntry::new);
    }

    public @NotNull Collection<ItemEntry> getMappers() {
        return mappers.values();
    }

    public @NotNull MeshesMap build() {
        Map<Key, Map<Path, Integer>> map = new HashMap<>();
        for (ItemEntry itemEntry : getMappers()) {
            map.put(itemEntry.getItemId(), new HashMap<>(itemEntry.getMap().inverse()));
        }
        return new MeshesMap(map);
    }

    public static class ItemEntry {
        private final Key itemId;
        private final BiMap<Integer, Path> map = HashBiMap.create();
        private final AtomicInteger integer = new AtomicInteger(1);

        public ItemEntry(@NotNull Key itemId) {
            this.itemId = itemId;
        }

        @NotNull
        public Key getItemId() {
            return itemId;
        }

        @Nullable
        public Integer get(@NotNull Path path) {
            return map.inverse().get(path);
        }

        public void add(@NotNull Path path) {
            map.put(integer.getAndIncrement(), path);
        }

        public @NotNull BiMap<Integer, Path> getMap() {
            return map;
        }
    }
}
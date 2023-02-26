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

package me.nelonn.propack.core.builder;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.MapMeshMapping;
import me.nelonn.propack.definition.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MappingsBuilder {
    private final Map<Item, Mapper> mappers = new HashMap<>();

    public @NotNull  Mapper getMapper(@NotNull Item item) {
        return mappers.computeIfAbsent(item, Mapper::new);
    }

    public @NotNull Collection<Mapper> getMappers() {
        return mappers.values();
    }

    public @NotNull MapMeshMapping build() {
        Map<Item, Map<Path, Integer>> map = new HashMap<>();
        for (Mapper mapper : getMappers()) {
            map.put(mapper.getItem(), new HashMap<>(mapper.getMap().inverse()));
        }
        return new MapMeshMapping(map);
    }

    public static class Mapper {
        private final Item item;
        private final BiMap<Integer, Path> map = HashBiMap.create();
        private final AtomicInteger integer = new AtomicInteger(1);

        public Mapper(@NotNull Item item) {
            this.item = item;
        }

        @NotNull
        public Item getItem() {
            return item;
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
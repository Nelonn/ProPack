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
import me.nelonn.propack.definition.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Mapper implements Cloneable {
    private final Item item;
    private final BiMap<Integer, Path> mappings = HashBiMap.create();
    private AtomicInteger integer = new AtomicInteger(1);

    public Mapper(@NotNull Item item) {
        this.item = item;
    }

    @NotNull
    public Item getItem() {
        return item;
    }

    @Nullable
    public Integer get(@NotNull Path path) {
        return mappings.inverse().get(path);
    }

    public void set(@NotNull Path path, int value) {
        if (value < 1) {
            throw new IllegalArgumentException("CustomModelData must be more than 0");
        }
        mappings.inverse().put(path, value);
    }

    public void map(@NotNull Path path) {
        mappings.put(integer.getAndIncrement(), path);
    }

    @NotNull
    public Set<Map.Entry<Integer, Path>> entrySet() {
        return mappings.entrySet();
    }

    public void clear() {
        mappings.clear();
    }

    @Override
    public Mapper clone() {
        try {
            return (Mapper) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
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

package me.nelonn.propack;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.definition.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MapMeshMapping implements MeshMapping {
    private final Map<Item, Map<Path, Integer>> map;

    public MapMeshMapping(Map<Item, Map<Path, Integer>> map) {
        this.map = new HashMap<>(map);
        this.map.replaceAll((k, v) -> new HashMap<>(v)); // deep copy
    }

    @Override
    public @Nullable Integer getCustomModelData(@NotNull Path mesh, @NotNull Item item) {
        Map<Path, Integer> itemMap = map.get(item);
        if (itemMap == null) return null;
        return itemMap.get(mesh);
    }
}

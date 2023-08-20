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

package me.nelonn.propack.bukkit.dispatcher;

import me.nelonn.flint.path.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ActivePackStoreMap {
    private final Map<Identifier, ActivePackStore> knownStores = new HashMap<>();

    public boolean register(@NotNull Identifier id, @NotNull ActivePackStore activePackStore) {
        if (knownStores.containsKey(id)) return false;
        knownStores.put(id, activePackStore);
        return true;
    }

    public boolean register(@NotNull String id, @NotNull ActivePackStore activePackStore) {
        return register(Identifier.ofWithFallback(id, "propack"), activePackStore);
    }

    public boolean unregister(@NotNull Identifier id) {
        return knownStores.remove(id) != null;
    }

    public boolean unregister(@NotNull String id) {
        return unregister(Identifier.ofWithFallback(id, "propack"));
    }

    public @Nullable ActivePackStore get(@NotNull Identifier id) {
        return knownStores.get(id);
    }

    public @Nullable ActivePackStore get(@NotNull String id) {
        return get(Identifier.ofWithFallback(id, "propack"));
    }
}

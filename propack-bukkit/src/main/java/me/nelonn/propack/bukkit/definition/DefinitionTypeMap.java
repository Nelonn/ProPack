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

package me.nelonn.propack.bukkit.definition;

import me.nelonn.flint.path.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DefinitionTypeMap {
    private final Map<Identifier, DefinitionType> knownDefTypes = new HashMap<>();

    public boolean register(@NotNull Identifier id, @NotNull DefinitionType definitionType) {
        if (knownDefTypes.containsKey(id)) return false;
        knownDefTypes.put(id, definitionType);
        return true;
    }

    public boolean register(@NotNull String id, @NotNull DefinitionType definitionType) {
        return register(Identifier.ofWithFallback(id, "propack"), definitionType);
    }

    public boolean unregister(@NotNull Identifier id) {
        return knownDefTypes.remove(id) != null;
    }

    public boolean unregister(@NotNull String id) {
        return unregister(Identifier.ofWithFallback(id, "propack"));
    }

    public @Nullable DefinitionType get(@NotNull Identifier id) {
        return knownDefTypes.get(id);
    }

    public @Nullable DefinitionType get(@NotNull String id) {
        return get(Identifier.ofWithFallback(id, "propack"));
    }
}

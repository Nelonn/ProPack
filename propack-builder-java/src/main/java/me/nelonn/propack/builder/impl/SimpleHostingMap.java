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

import me.nelonn.flint.path.Key;
import me.nelonn.propack.builder.api.hosting.Hosting;
import me.nelonn.propack.builder.api.hosting.HostingMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SimpleHostingMap implements HostingMap {
    protected final Map<Key, Hosting> knownHostings = new HashMap<>();

    public SimpleHostingMap() {
    }

    @Override
    public boolean register(@NotNull Key id, @NotNull Hosting hosting) {
        if (hosting.register(this, id)) {
            knownHostings.put(id, hosting);
            return true;
        }
        return false;
    }

    @Override
    public boolean unregister(@NotNull Key id) {
        Hosting hosting = knownHostings.get(id);
        if (hosting == null || !hosting.unregister(this)) return false;
        knownHostings.remove(id);
        return true;
    }

    @Override
    public @NotNull Hosting getHosting(@NotNull Key id) {
        return knownHostings.get(id);
    }

    @Override
    public @NotNull Map<Key, Hosting> getKnownHostings() {
        return knownHostings;
    }
}

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

import com.google.common.base.Preconditions;
import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.bukkit.dispatcher.Dispatcher;
import me.nelonn.propack.core.ProPackCore;
import me.nelonn.propack.definition.Item;
import me.nelonn.propack.bukkit.resourcepack.PackContainer;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public final class ProPack {
    private static ProPackPlugin plugin;

    public static ProPackPlugin getPlugin() {
        Preconditions.checkNotNull(plugin, "ProPack is not enabled, unable to access the api.");
        return plugin;
    }

    public static void setPlugin(ProPackPlugin plugin) {
        Preconditions.checkNotNull(plugin);
        ProPack.plugin = plugin;
    }

    public static @NotNull ProPackCore getCore() {
        return plugin.getProPackCore();
    }

    public static @NotNull PackContainer getPackContainer() {
        return plugin.getPackContainer();
    }

    public static @NotNull Dispatcher getDispatcher() {
        return plugin.getDispatcher();
    }

    public static @NotNull Item adapt(@NotNull Material material) {
        return new Item(Identifier.of(material.getKey().toString()), material.isBlock());
    }

    private ProPack() {
        throw new UnsupportedOperationException();
    }
}

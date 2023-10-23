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

package me.nelonn.propack.bukkit;

import com.google.common.base.Preconditions;
import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.ResourcePack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProPack {
    public static final String CUSTOM_MODEL = "CustomModel";

    private static BukkitProPackCore core;

    public static BukkitProPackCore getCore() {
        Preconditions.checkNotNull(core, "ProPack is not enabled, unable to access the api.");
        return core;
    }

    public static void setCore(@NotNull BukkitProPackCore core) {
        Preconditions.checkNotNull(core);
        ProPack.core = core;
    }

    public static @NotNull Identifier adapt(@NotNull Material material) {
        return Identifier.of(material.getKey().toString());
    }

    public static @Nullable ResourcePack getAppliedResourcePack(@NotNull Player player) {
        return getCore().getDispatcher().getAppliedResourcePack(player);
    }

    private ProPack() {
        throw new UnsupportedOperationException();
    }
}

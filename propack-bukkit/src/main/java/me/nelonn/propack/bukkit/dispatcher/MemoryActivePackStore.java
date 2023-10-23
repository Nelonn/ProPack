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

package me.nelonn.propack.bukkit.dispatcher;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryActivePackStore implements ActivePackStore, Listener {
    private final Map<UUID, ActivePack> map = new ConcurrentHashMap<>();

    public MemoryActivePackStore(@NotNull Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @Nullable ActivePack getActiveResourcePack(@NotNull UUID uuid) {
        return map.get(uuid);
    }

    @Override
    public void setActiveResourcePack(@NotNull UUID uuid, @Nullable ActivePack activePack) {
        map.put(uuid, activePack);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        map.remove(event.getPlayer().getUniqueId());
    }
}

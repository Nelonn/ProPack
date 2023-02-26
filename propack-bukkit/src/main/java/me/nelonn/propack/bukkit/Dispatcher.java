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

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.bukkit.compatibility.CompatibilitiesManager;
import me.nelonn.propack.bukkit.sender.BukkitPackSender;
import me.nelonn.propack.bukkit.sender.PackSender;
import me.nelonn.propack.bukkit.sender.ProtocolPackSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Dispatcher implements Listener {
    private final Plugin plugin;
    private final PackSender packSender;
    private final Map<Player, ResourcePack> sent = new HashMap<>();

    public Dispatcher(@NotNull Plugin plugin) {
        this.plugin = plugin;
        packSender = /*Util.isPaper() ? new PaperPackSender() :*/
                CompatibilitiesManager.hasPlugin("ProtocolLib") ? new ProtocolPackSender() :
                        new BukkitPackSender();
    }

    public void sendPack(Player player, ResourcePack resourcePack) {
        if (!resourcePack.isUploaded()) {
            throw new IllegalArgumentException("Resource pack '" + resourcePack.getName() + "' not upload");
        }
        packSender.sendPack(player, resourcePack.getUpload().get());
        sent.put(player, resourcePack);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!Settings.DISPATCH_ENABLED.asBoolean()) return;
        ResourcePack resourcePack = ProPack.getResourcePackContainer().getDefinition(Settings.DISPATCH_PACK.asString()).getResourcePack();
        int delay = (int) Settings.DISPATCH_DELAY.getValue();
        if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendPack(event.getPlayer(), resourcePack), delay * 20L);
        } else {
            sendPack(event.getPlayer(), resourcePack);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sent.remove(event.getPlayer());
    }

    public @NotNull Optional<ResourcePack> getResourcePack(@NotNull Player player) {
        return Optional.ofNullable(sent.get(player));
    }
}

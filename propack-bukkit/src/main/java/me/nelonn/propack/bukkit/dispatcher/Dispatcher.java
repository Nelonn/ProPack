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

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.bukkit.Config;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.compatibility.CompatibilitiesManager;
import me.nelonn.propack.bukkit.resourcepack.PackDefinition;
import me.nelonn.propack.bukkit.dispatcher.sender.BukkitPackSender;
import me.nelonn.propack.bukkit.dispatcher.sender.PackSender;
import me.nelonn.propack.bukkit.dispatcher.sender.ProtocolPackSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Dispatcher implements Listener {
    private final ProPackPlugin plugin;
    private final PackSender packSender;
    private final Store store;

    public Dispatcher(@NotNull ProPackPlugin plugin) {
        this.plugin = plugin;
        packSender = /*Util.isPaper() ? new PaperPackSender() :*/
                CompatibilitiesManager.hasPlugin("ProtocolLib") ? new ProtocolPackSender() :
                        new BukkitPackSender();
        store = new MemoryStore();
        Bukkit.getPluginManager().registerEvents((Listener) store, plugin);
    }

    public void sendPack(Player player, ResourcePack resourcePack) {
        Optional<UploadedPack> uploadedPack = resourcePack.getUpload();
        if (uploadedPack.isEmpty()) {
            throw new IllegalArgumentException("Resource pack '" + resourcePack.getName() + "' not upload");
        }
        packSender.send(player, uploadedPack.get());
        store.setActiveResourcePack(player.getUniqueId(), resourcePack.getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!Config.DISPATCHER_ENABLED.asBoolean()) return;
        PackDefinition definition = ProPack.getPackContainer().getDefinition(Config.DISPATCHER_PACK.asString());
        if (definition == null) return;
        Optional<ResourcePack> resourcePack = definition.getResourcePack();
        if (resourcePack.isEmpty()) return;
        int delay = (int) Config.DISPATCHER_DELAY.getValue();
        if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendPack(event.getPlayer(), resourcePack.get()), delay * 20L);
        } else {
            sendPack(event.getPlayer(), resourcePack.get());
        }
    }

    public @NotNull Optional<ResourcePack> getResourcePack(@NotNull Player player) {
        String rpName = store.getActiveResourcePack(player.getUniqueId());
        if (rpName == null) return Optional.empty();
        PackDefinition definition = plugin.getPackContainer().getDefinition(rpName);
        if (definition == null) return Optional.empty();
        return definition.getResourcePack();
    }
}

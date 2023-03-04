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
import me.nelonn.propack.bukkit.definition.PackDefinition;
import me.nelonn.propack.bukkit.dispatcher.sender.BukkitPackSender;
import me.nelonn.propack.bukkit.dispatcher.sender.PackSender;
import me.nelonn.propack.bukkit.dispatcher.sender.ProtocolPackSender;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Dispatcher implements Listener {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final ProPackPlugin plugin;
    private final PackSender packSender;
    private final Store store;

    public Dispatcher(@NotNull ProPackPlugin plugin) {
        this.plugin = plugin;
        packSender = /*Util.isPaper() ? new PaperPackSender() :*/
                CompatibilitiesManager.hasPlugin("ProtocolLib") ? new ProtocolPackSender() :
                        new BukkitPackSender();
        store = plugin.getCore().getStoreMap().get(Config.DISPATCHER_STORE.asString());
        if (store == null) {
            throw new IllegalArgumentException("Store '" + Config.DISPATCHER_STORE.asString() + "' not found");
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void sendPack(@NotNull Player player, @NotNull UploadedPack uploadedPack) {
        packSender.send(player, uploadedPack);
        // TODO: set only when player downloaded pack
        store.setActiveResourcePack(player.getUniqueId(), new SentPack(uploadedPack.getName(), uploadedPack.getSha1String()));
    }

    public void sendPack(@NotNull Player player, @NotNull ResourcePack resourcePack) {
        Optional<UploadedPack> uploadedPack = resourcePack.getUpload();
        if (uploadedPack.isEmpty()) {
            throw new IllegalArgumentException("Resource pack '" + resourcePack.getName() + "' not upload");
        }
        sendPack(player, uploadedPack.get());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!Config.DISPATCHER_ENABLED.asBoolean()) return;
        PackDefinition definition = ProPack.getCore().getPackManager().getDefinition(Config.DISPATCHER_PACK.asString());
        if (definition == null) {
            LOGGER.warn("Resource pack '" + Config.DISPATCHER_PACK.asString() + "' not found");
            return;
        }
        if (definition.getResourcePack().isEmpty()) {
            LOGGER.warn("Resource pack '" + Config.DISPATCHER_PACK.asString() + "' not built");
            return;
        }
        ResourcePack resourcePack = definition.getResourcePack().get();
        if (resourcePack.getUpload().isEmpty()) {
            throw new IllegalArgumentException("Resource pack '" + resourcePack.getName() + "' not uploaded");
        }
        UploadedPack uploadedPack = resourcePack.getUpload().get();
        Player player = event.getPlayer();
        SentPack active = store.getActiveResourcePack(player.getUniqueId());
        if (active != null) {
            if (Config.DISPATCHER_REPLACE.asBoolean()) {
                if (active.name.equals(resourcePack.getName()) && active.sha1.equals(uploadedPack.getSha1String())) return;
            } else return;
        }

        int delay = (int) Config.DISPATCHER_DELAY.getValue();
        if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendPack(player, uploadedPack), delay * 20L);
        } else {
            sendPack(player, uploadedPack);
        }
    }

    public @NotNull Optional<ResourcePack> getResourcePack(@NotNull Player player) {
        SentPack sentPack = store.getActiveResourcePack(player.getUniqueId());
        if (sentPack == null) return Optional.empty();
        PackDefinition definition = plugin.getCore().getPackManager().getDefinition(sentPack.name);
        if (definition == null) return Optional.empty();
        return definition.getResourcePack();
    }
}

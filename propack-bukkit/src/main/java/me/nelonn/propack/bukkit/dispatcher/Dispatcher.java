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
import me.nelonn.propack.bukkit.ResourcePackInfo;
import me.nelonn.propack.bukkit.compatibility.CompatibilitiesManager;
import me.nelonn.propack.bukkit.definition.PackDefinition;
import me.nelonn.propack.bukkit.dispatcher.sender.BukkitPackSender;
import me.nelonn.propack.bukkit.dispatcher.sender.PackSender;
import me.nelonn.propack.bukkit.dispatcher.sender.ProtocolPackSender;
import me.nelonn.propack.core.util.LogManagerCompat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Dispatcher implements Listener {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final ProPackPlugin plugin;
    private final PackSender packSender;
    private final Map<UUID, SentPack> pending = new HashMap<>();
    private Store store;

    public Dispatcher(@NotNull ProPackPlugin plugin) {
        this.plugin = plugin;
        packSender = /*Util.isPaper() ? new PaperPackSender() :*/
                CompatibilitiesManager.hasPlugin("ProtocolLib") ? new ProtocolPackSender() :
                        new BukkitPackSender();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        store = new MemoryStore(plugin);
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
        if (this.store == null) {
            this.store = new MemoryStore(plugin);
        }
    }

    public void sendOffer(@NotNull Player player, @NotNull ResourcePackInfo packInfo) {
        packSender.send(player, packInfo);
        pending.put(player.getUniqueId(), new SentPack(packInfo.getUpload().getName(), packInfo.getUpload().getSha1String()));
    }

    /**
     * This method uses dispatcher configuration as ResourcePackInfo
     * @param player receiver
     * @param uploadedPack uploaded resource pack
     */
    public void sendOfferAsDefault(@NotNull Player player, @NotNull UploadedPack uploadedPack) {
        Component prompt = MiniMessage.miniMessage().deserialize(Config.DISPATCHER_PROMPT.asString(),
                Placeholder.component("player", Component.text(player.getName())),
                Placeholder.component("pack_name", Component.text(uploadedPack.getName())));
        ResourcePackInfo packInfo = new ResourcePackInfo(uploadedPack, prompt, Config.DISPATCHER_REQUIRED.asBoolean());
        sendOffer(player, packInfo);
    }

    /**
     * This method uses dispatcher configuration as ResourcePackInfo
     * @param player receiver
     * @param resourcePack resource pack
     */
    public void sendOfferAsDefault(@NotNull Player player, @NotNull ResourcePack resourcePack) {
        Optional<UploadedPack> uploadedPack = resourcePack.getUpload();
        if (uploadedPack.isEmpty()) {
            throw new IllegalArgumentException("Resource pack '" + resourcePack.getName() + "' not upload");
        }
        sendOfferAsDefault(player, uploadedPack.get());
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
        int delay = Config.DISPATCHER_DELAY.asInt();
        if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendOfferAsDefault(player, uploadedPack), delay * 20L);
        } else {
            sendOfferAsDefault(player, uploadedPack);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            store.setActiveResourcePack(event.getPlayer().getUniqueId(), pending.remove(event.getPlayer().getUniqueId()));
        }
    }

    public @Nullable SentPack getPendingResourcePack(@NotNull Player player) {
        return pending.get(player.getUniqueId());
    }

    public @NotNull Optional<ResourcePack> getAppliedResourcePack(@NotNull Player player) {
        SentPack sentPack = store.getActiveResourcePack(player.getUniqueId());
        if (sentPack == null) return Optional.empty();
        PackDefinition definition = plugin.getCore().getPackManager().getDefinition(sentPack.name);
        if (definition == null) return Optional.empty();
        return definition.getResourcePack();
    }
}

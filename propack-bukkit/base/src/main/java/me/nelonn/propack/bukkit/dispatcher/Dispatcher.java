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

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.bukkit.Config;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.ResourcePackOffer;
import me.nelonn.propack.bukkit.definition.PackDefinition;
import me.nelonn.propack.core.util.LogManagerCompat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class Dispatcher implements Listener {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final ProPackPlugin plugin;
    private final PackSender packSender;
    private final Map<UUID, ActivePack> pending = new HashMap<>();
    private final ActivePackStore fallbackActivePackStore;
    private ActivePackStore activePackStore;

    public Dispatcher(@NotNull ProPackPlugin plugin, @NotNull MemoryActivePackStore memoryStore) {
        this.plugin = plugin;
        packSender = Objects.requireNonNull(PaperPackSender.INSTANCE, "INSTALL PAPER PLEASE!!!");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        fallbackActivePackStore = memoryStore;
        activePackStore = fallbackActivePackStore;
    }

    public @NotNull ActivePackStore getStore() {
        return activePackStore;
    }

    public void setStore(@Nullable ActivePackStore activePackStore) {
        this.activePackStore = activePackStore != null ? activePackStore : fallbackActivePackStore;
    }

    public void sendOffer(@NotNull Player player, @NotNull ResourcePackOffer packOffer) {
        packSender.send(player, packOffer);
        pending.put(player.getUniqueId(), new ActivePack(packOffer.getUpload().getName(), packOffer.getUpload().getSha1String()));
    }

    /**
     * This method uses dispatcher configuration as ResourcePackOffer
     * @param player receiver
     * @param uploadedPack uploaded resource pack
     */
    public void sendOfferAsDefault(@NotNull Player player, @NotNull UploadedPack uploadedPack) {
        Component prompt = plugin.config().get(Config.dispatcherPrompt).accept(
                Placeholder.component("player", Component.text(player.getName())),
                Placeholder.component("pack_name", Component.text(uploadedPack.getName())));
        ResourcePackOffer packOffer = new ResourcePackOffer(uploadedPack, prompt, plugin.config().get(Config.dispatcherRequired));
        sendOffer(player, packOffer);
    }

    /**
     * This method uses dispatcher configuration as ResourcePackInfo
     * @param player receiver
     * @param resourcePack resource pack
     */
    public void sendOfferAsDefault(@NotNull Player player, @NotNull ResourcePack resourcePack) {
        if (!resourcePack.isUploaded()) {
            throw new IllegalArgumentException("Resource pack '" + resourcePack.getName() + "' not uploaded");
        }
        sendOfferAsDefault(player, resourcePack.getUpload());
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (!plugin.config().get(Config.dispatcherEnabled)) return;
        String packName = plugin.config().get(Config.dispatcherPack);
        PackDefinition definition = ProPack.getCore().getPackManager().getDefinition(packName);
        if (definition == null) {
            LOGGER.warn("Resource pack '" + packName + "' not found");
            return;
        }
        ResourcePack resourcePack = definition.getResourcePack();
        if (resourcePack == null) {
            LOGGER.warn("Resource pack '" + packName + "' not built");
            return;
        }
        Player player = event.getPlayer();
        if (plugin.config().get(Config.itemsAdderCompat) && Bukkit.getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
            activePackStore.setActiveResourcePack(player.getUniqueId(), new ActivePack(resourcePack.getName(), null));
            return;
        }
        if (!resourcePack.isUploaded()) {
            LOGGER.error("Resource pack '" + resourcePack.getName() + "' not uploaded");
            return;
        }
        UploadedPack uploadedPack = resourcePack.getUpload();
        ActivePack active = activePackStore.getActiveResourcePack(player.getUniqueId());
        if (active != null) {
            if (plugin.config().get(Config.dispatcherReplace)) {
                if (active.name.equals(resourcePack.getName()) && active.sha1 != null && active.sha1.equals(uploadedPack.getSha1String())) return;
            } else return;
        }
        int delay = plugin.config().get(Config.dispatcherDelay);
        if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> sendOfferAsDefault(player, uploadedPack), delay * 20L);
        } else {
            sendOfferAsDefault(player, uploadedPack);
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            Player player = event.getPlayer();
            activePackStore.setActiveResourcePack(player.getUniqueId(), pending.remove(player.getUniqueId()));
            if (plugin.config().get(Config.patchPacketItems)) {
                player.updateInventory();
                // TODO: resend entities
            }
        }
    }

    public @Nullable ActivePack getPendingResourcePack(@NotNull Player player) {
        return pending.get(player.getUniqueId());
    }

    public @Nullable ResourcePack getAppliedResourcePack(@NotNull Player player) {
        return getAppliedResourcePack(player.getUniqueId());
    }

    public @Nullable ResourcePack getAppliedResourcePack(@NotNull UUID playerID) {
        ActivePack activePack = activePackStore.getActiveResourcePack(playerID);
        if (activePack == null) return null;
        PackDefinition definition = plugin.getCore().getPackManager().getDefinition(activePack.name);
        if (definition == null) return null;
        return definition.getResourcePack();
    }
}

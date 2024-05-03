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

package me.nelonn.propack.bukkit.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.SoundAsset;
import me.nelonn.propack.bukkit.Config;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.adapter.Adapter;
import me.nelonn.propack.bukkit.adapter.AdapterLoader;
import me.nelonn.propack.bukkit.adapter.IPacketListener;
import me.nelonn.propack.bukkit.adapter.MItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PacketListener implements IPacketListener, Listener {
    private final ProPackPlugin plugin;
    private final Adapter adapter;
    private final ItemPatcher packetPatcher;
    private final boolean thirdPartyInjector;

    public PacketListener(@NotNull ProPackPlugin plugin) {
        this.plugin = plugin;
        this.adapter = Objects.requireNonNull(AdapterLoader.ADAPTER, "Adapter not loaded");
        this.packetPatcher = plugin.getItemPatcher();
        if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            plugin.getLogger().info("Using third-party packet injector: ProtocolLib");
            ProtocolLibrary.getProtocolManager().addPacketListener(new com.comphenix.protocol.events.PacketListener() {
                private final ListeningWhitelist sending = buildWhitelist(PacketRegistry.getServerPacketTypes());
                private final ListeningWhitelist receiving = buildWhitelist(PacketRegistry.getClientPacketTypes());

                private static ListeningWhitelist buildWhitelist(Collection<PacketType> packetTypes) {
                    return ListeningWhitelist.newBuilder()
                            .normal()
                            .gamePhase(GamePhase.PLAYING)
                            .types(packetTypes
                                    .stream()
                                    .filter(packetType -> packetType.getProtocol() == PacketType.Protocol.PLAY)
                                    .toList())
                            .build();
                }

                @Override
                public void onPacketSending(PacketEvent packetEvent) {
                    if (packetEvent.isReadOnly()) return;
                    Object patchedPacket = PacketListener.this.onPacketSend(packetEvent.getPlayer(), packetEvent.getPacket().getHandle());
                    if (patchedPacket != null) {
                        packetEvent.setPacket(PacketContainer.fromPacket(patchedPacket));
                    } else {
                        packetEvent.setCancelled(true);
                    }
                }

                @Override
                public void onPacketReceiving(PacketEvent packetEvent) {
                    if (packetEvent.isReadOnly()) return;
                    Object patchedPacket = PacketListener.this.onPacketReceive(packetEvent.getPlayer(), packetEvent.getPacket().getHandle());
                    if (patchedPacket != null) {
                        packetEvent.setPacket(PacketContainer.fromPacket(patchedPacket));
                    } else {
                        packetEvent.setCancelled(true);
                    }
                }

                @Override
                public ListeningWhitelist getSendingWhitelist() {
                    return sending;
                }

                @Override
                public ListeningWhitelist getReceivingWhitelist() {
                    return receiving;
                }

                @Override
                public Plugin getPlugin() {
                    return plugin;
                }
            });
            thirdPartyInjector = true;
        } else {
            plugin.getLogger().warning("Builtin packet injector is not recommended! Install ProtocolLib");
            thirdPartyInjector = false;
        }
    }

    public static void register(@NotNull ProPackPlugin plugin) {
        PacketListener packetListener = new PacketListener(plugin);
        if (!packetListener.thirdPartyInjector) {
            plugin.getServer().getPluginManager().registerEvents(packetListener, plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void on(PlayerJoinEvent event) {
        adapter.inject(event.getPlayer(), this);
    }

    private @NotNull String patchSound(@NotNull Resources resources, @NotNull String original) {
        Path path = Path.tryOrNull(original);
        if (path == null) return original;
        SoundAsset soundAsset = resources.sound(path);
        if (soundAsset == null) return original;
        return soundAsset.realPath().toString();
    }

    @Override
    public @Nullable Object onPacketSend(@NotNull Player player, @NotNull Object packet) {
        ResourcePack resourcePack = ProPack.getCore().getDispatcher().getAppliedResourcePack(player);
        if (resourcePack == null) return packet;
        final Resources resources = resourcePack.resources();
        Class<?> type = packet.getClass();
        if (plugin.config().get(Config.patchPacketItems)) {
            BiFunction<Object, Consumer<MItemStack>, Object> method = null;
            if (type == adapter.getClientboundContainerSetSlotPacket()) {
                method = adapter::patchClientboundContainerSetSlotPacket;
            } else if (type == adapter.getClientboundContainerSetContentPacket()) {
                method = adapter::patchClientboundContainerSetContentPacket;
            } else if (type == adapter.getClientboundSetEntityEquipmentPacket()) {
                method = adapter::patchClientboundSetEntityEquipmentPacket;
            } else if (type == adapter.getClientboundSetEntityDataPacket()) {
                method = adapter::patchClientboundSetEntityDataPacket;
            }
            if (method != null) {
                return method.apply(packet, stack -> packetPatcher.patchClientboundItem(stack, resources));
            }
        }
        if (plugin.config().get(Config.patchPacketSounds)) {
            if (type == adapter.getClientboundSoundPacket() || type == adapter.getClientboundCustomSoundPacket()) {
                return adapter.patchClientboundSoundPacket(packet, original -> patchSound(resources, original));
            } else if (type == adapter.getClientboundSoundEntityPacket()) {
                return adapter.patchClientboundSoundEntityPacket(packet, original -> patchSound(resources, original));
            }
        }
        return packet;
    }

    @Override
    public @Nullable Object onPacketReceive(@NotNull Player player, @NotNull Object packet) {
        if (packet.getClass() == adapter.getServerboundSetCreativeModeSlotPacket()) {
            return adapter.patchServerboundSetCreativeModeSlotPacket(packet, packetPatcher::patchServerboundItem);
        }
        return packet;
    }
}
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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PacketListener implements IPacketListener, Listener {
    private final ProPackPlugin plugin;
    private final Adapter adapter;
    private final ItemPatcher packetPatcher;

    public PacketListener(@NotNull ProPackPlugin plugin) {
        this.plugin = plugin;
        this.adapter = Objects.requireNonNull(AdapterLoader.ADAPTER, "Adapter not loaded");
        this.packetPatcher = plugin.getItemPatcher();
    }

    public static void register(@NotNull ProPackPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new PacketListener(plugin), plugin);
    }

    @EventHandler
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
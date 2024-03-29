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

package me.nelonn.propack.bukkit.compatibility.provided.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.bukkit.Config;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.adapter.Adapter;
import me.nelonn.propack.bukkit.adapter.AdapterLoader;
import me.nelonn.propack.bukkit.adapter.MItemStack;
import me.nelonn.propack.bukkit.packet.ItemPatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketListener extends PacketAdapter {
    public static final PacketType[] PACKET_TYPES;

    static {
        List<PacketType> packetTypes = new ArrayList<>(List.of(new PacketType[]{
                PacketType.Play.Client.SET_CREATIVE_SLOT,

                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS,
                PacketType.Play.Server.ENTITY_EQUIPMENT,
                PacketType.Play.Server.ENTITY_METADATA,
        }));
        PACKET_TYPES = packetTypes.toArray(PacketType[]::new);
    }

    private final ProPackPlugin plugin;
    private final Adapter adapter;
    private final ItemPatcher packetPatcher;

    public PacketListener(@NotNull ProPackPlugin plugin) {
        super(plugin,
                ListenerPriority.HIGHEST, // priority is inverted
                PACKET_TYPES);
        this.plugin = plugin;
        this.adapter = Objects.requireNonNull(AdapterLoader.ADAPTER, "Adapter not loaded");
        this.packetPatcher = plugin.getItemPatcher();
    }

    public static void register(@NotNull ProPackPlugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(plugin));
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketType type = event.getPacketType();
        Object packet = event.getPacket().getHandle();
        if (type == PacketType.Play.Client.SET_CREATIVE_SLOT && plugin.config().get(Config.patchPacketItems)) {
            adapter.patchServerboundSetCreativeModeSlotPacket(packet, packetPatcher::patchServerboundItem);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketType type = event.getPacketType();
        Object packet = event.getPacket().getHandle();
        ResourcePack resourcePack = ProPack.getCore().getDispatcher().getAppliedResourcePack(event.getPlayer());
        if (resourcePack == null) return;
        final Resources resources = resourcePack.resources();
        if (plugin.config().get(Config.patchPacketItems)) {
            BiConsumer<Object, Consumer<MItemStack>> method = null;
            if (type == PacketType.Play.Server.SET_SLOT) {
                method = adapter::patchClientboundContainerSetSlotPacket;
            } else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                method = adapter::patchClientboundContainerSetContentPacket;
            } else if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                method = adapter::patchClientboundSetEntityEquipmentPacket;
            } else if (type == PacketType.Play.Server.ENTITY_METADATA) {
                method = adapter::patchClientboundSetEntityDataPacket;
            }
            if (method != null) {
                method.accept(packet, stack -> packetPatcher.patchClientboundItem(stack, resources));
                return;
            }
        }
    }
}
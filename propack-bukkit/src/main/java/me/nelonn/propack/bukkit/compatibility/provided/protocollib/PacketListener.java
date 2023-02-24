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

package me.nelonn.propack.bukkit.compatibility.provided.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.asset.CombinedItemModel;
import me.nelonn.propack.asset.DefaultItemModel;
import me.nelonn.propack.asset.ItemModel;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.adapter.Adapter;
import me.nelonn.propack.bukkit.adapter.WrappedCompoundTag;
import me.nelonn.propack.bukkit.adapter.WrappedItemStack;
import me.nelonn.propack.bukkit.adapter.WrappedListTag;
import me.nelonn.propack.core.builder.LocalResourcePack;
import me.nelonn.propack.definition.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketListener extends PacketAdapter {
    private static final String CUSTOM_MODEL_FIELD = "CustomModel";
    private static final String CUSTOM_MODEL_DATA_FIELD = "CustomModelData";
    private final Adapter adapter;

    public PacketListener(@NotNull Plugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, // priority is inverted
                PacketType.Play.Client.SET_CREATIVE_SLOT,

                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS,
                PacketType.Play.Server.ENTITY_EQUIPMENT);
        try {
            String craftBukkit = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            String minecraft = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String nmsVersion = craftBukkit;
            if (minecraft.equalsIgnoreCase("1.17.1")) {
                nmsVersion += "_2";
            }
            Class<?> clazz = Class.forName("me.nelonn.propack.bukkit.adapter.impl." + nmsVersion + ".PaperweightAdapter");
            if (Adapter.class.isAssignableFrom(clazz)) {
                adapter = (Adapter) clazz.getDeclaredConstructor().newInstance();
            } else {
                throw new IllegalArgumentException("Class '" + clazz.getName() + "' must implement '" + Adapter.class.getName() + "'");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load bukkit adapter", e);
        }
    }

    public static void register(@NotNull Plugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(plugin));
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketType type = event.getPacketType();
        Object packet = event.getPacket().getHandle();
        if (type == PacketType.Play.Client.SET_CREATIVE_SLOT) {
            adapter.patchSetCreativeSlot(packet, this::patchIn);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketType type = event.getPacketType();
        Object packet = event.getPacket().getHandle();
        final ResourcePack resourcePack = ProPack.getDispatcher().getResourcePack(event.getPlayer());
        if (resourcePack == null) return;
        BiConsumer<Object, Consumer<WrappedItemStack>> method;
        if (type == PacketType.Play.Server.SET_SLOT) {
            method = adapter::patchSetSlot;
        } else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
            method = adapter::patchWindowItems;
        } else if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            method = adapter::patchEntityEquipment;
        } else {
            return;
        }
        method.accept(packet, stack -> patchOut(stack, resourcePack));
    }

    private void patchIn(WrappedItemStack itemStack) {
        WrappedCompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains(CUSTOM_MODEL_DATA_FIELD, NbtType.TAG_INT.getRawID()) ||
                !tag.contains(CUSTOM_MODEL_FIELD, NbtType.TAG_STRING.getRawID())) return;
        tag.remove(CUSTOM_MODEL_DATA_FIELD);
    }

    private void patchOut(WrappedItemStack itemStack, ResourcePack resourcePack) {
        try {
            WrappedCompoundTag tag = itemStack.getTag();
            if (tag == null || !tag.contains(CUSTOM_MODEL_FIELD, NbtType.TAG_STRING.getRawID())) return;
            String customModel = tag.getString(CUSTOM_MODEL_FIELD);
            if (customModel.isEmpty()) return;
            Path path = Path.of(customModel);
            ItemModel itemModel = resourcePack.getItemModel(path);
            if (itemModel == null) return;
            Path mesh;
            if (itemModel instanceof DefaultItemModel defaultItemModel) {
                mesh = defaultItemModel.getMesh();
            } else if (itemModel instanceof CombinedItemModel combinedItemModel) {
                WrappedListTag listTag = tag.getList("ModelElements", NbtType.TAG_STRING.getRawID());
                mesh = combinedItemModel.getMesh(listTag.asStringCollection().toArray(new String[0]));
            } else if (itemModel instanceof SlotItemModel slotItemModel) {
                WrappedCompoundTag slotsTag = tag.getCompound("ModelSlots");
                Map<String, String> slots = new HashMap<>();
                for (SlotItemModel.Slot slot : slotItemModel.getSlots()) {
                    String element = slotsTag.getString(slot.getName());
                    if (!element.isEmpty()) {
                        slots.put(slot.getName(), element);
                    }
                }
                mesh = slotItemModel.getMesh(slots);
            } else {
                return;
            }
            Material material = Material.matchMaterial(itemStack.getItemId().toString());
            assert material != null;
            Item itemType = ProPack.adapt(material);
            if (!itemModel.getTargetItems().contains(itemType)) return;
            Integer cmd = ((LocalResourcePack) resourcePack).getMappings().getMapper(itemType).get(mesh);
            if (cmd == null) return;
            tag.putInt(CUSTOM_MODEL_DATA_FIELD, cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
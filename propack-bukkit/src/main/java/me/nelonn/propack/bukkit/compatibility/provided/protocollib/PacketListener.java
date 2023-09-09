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
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import me.nelonn.flint.path.Identifier;
import me.nelonn.flint.path.InvalidPathException;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.*;
import me.nelonn.propack.bukkit.Config;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.adapter.Adapter;
import me.nelonn.propack.bukkit.adapter.MCompoundTag;
import me.nelonn.propack.bukkit.adapter.MItemStack;
import me.nelonn.propack.bukkit.adapter.MListTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class PacketListener extends PacketAdapter {
    private static final String CUSTOM_MODEL_DATA_FIELD = "CustomModelData";
    private final Adapter adapter;
    private final ProPackPlugin plugin;

    public PacketListener(@NotNull ProPackPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, // priority is inverted
                PacketType.Play.Client.SET_CREATIVE_SLOT,

                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS,
                PacketType.Play.Server.ENTITY_EQUIPMENT,
                PacketType.Play.Server.ENTITY_METADATA,

                PacketType.Play.Server.CUSTOM_SOUND_EFFECT, // Removed in 1.19.3
                PacketType.Play.Server.NAMED_SOUND_EFFECT);
        this.plugin = plugin;
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

    public static void register(@NotNull ProPackPlugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(plugin));
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketType type = event.getPacketType();
        Object packet = event.getPacket().getHandle();
        if (type == PacketType.Play.Client.SET_CREATIVE_SLOT && plugin.config().get(Config.patchPacketItems)) {
            patchInItems(adapter.adaptPacket1(packet).getItem());
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketType type = event.getPacketType();
        Object packet = event.getPacket().getHandle();
        Optional<ResourcePack> resourcePack = ProPack.getCore().getDispatcher().getAppliedResourcePack(event.getPlayer());
        if (resourcePack.isEmpty()) return;
        final Resources resources = resourcePack.get().resources();
        if (plugin.config().get(Config.patchPacketItems)) {
            BiConsumer<Object, Consumer<MItemStack>> method;
            if (type == PacketType.Play.Server.SET_SLOT) {
                method = (rawPacket, patcher) -> { // TEMP
                    patcher.accept(adapter.adaptPacket2(rawPacket).getItem());
                };
            } else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                method = adapter::patchSetContent;
            } else if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                method = adapter::patchEntityEquipment;
            } else if (type == PacketType.Play.Server.ENTITY_METADATA) {
                method = adapter::patchSetEntityData;
            } else {
                return;
            }
            method.accept(packet, stack -> patchOutItems(stack, resources));
        }
        if (plugin.config().get(Config.patchPacketSounds) &&
                (type == PacketType.Play.Server.CUSTOM_SOUND_EFFECT ||
                        type == PacketType.Play.Server.NAMED_SOUND_EFFECT)) {
            MinecraftKey minecraftKey = event.getPacket().getMinecraftKeys().read(0);
            Path path = Path.of(minecraftKey.getPrefix(), minecraftKey.getKey());
            SoundAsset sound = resources.soundNullable(path);
            if (sound != null) {
                path = sound.realPath();
                minecraftKey = new MinecraftKey(path.getNamespace(), path.getValue());
                event.getPacket().getMinecraftKeys().write(0, minecraftKey);
            }
        }
    }

    private void patchInItems(MItemStack itemStack) {
        MCompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains(CUSTOM_MODEL_DATA_FIELD, NbtType.TAG_INT.getRawID()) ||
                !tag.contains(ProPack.CUSTOM_MODEL, NbtType.TAG_STRING.getRawID())) return;
        tag.remove(CUSTOM_MODEL_DATA_FIELD);
    }

    private void patchOutItems(@NotNull MItemStack itemStack, @NotNull Resources resources) {
        try {
            MCompoundTag tag = itemStack.getTag();
            if (tag == null || !tag.contains(ProPack.CUSTOM_MODEL, NbtType.TAG_STRING.getRawID())) return;
            String customModel = tag.getString(ProPack.CUSTOM_MODEL);
            if (customModel.isEmpty()) return;
            Path path;
            try {
                path = Path.of(customModel);
            } catch (InvalidPathException ignored) {
                return;
            }
            ItemModel itemModel = resources.itemModelNullable(path);
            if (itemModel == null) return;
            Path mesh;
            if (itemModel instanceof DefaultItemModel defaultItemModel) {
                mesh = defaultItemModel.getMesh();
            } else if (itemModel instanceof CombinedItemModel combinedItemModel) {
                MListTag listTag = tag.getList("ModelElements", NbtType.TAG_STRING.getRawID());
                mesh = combinedItemModel.getMesh(listTag.asStringCollection().toArray(new String[0]));
            } else if (itemModel instanceof SlotItemModel slotItemModel) {
                MCompoundTag slotsTag = tag.getCompound("ModelSlots");
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
            Identifier itemType = ProPack.adapt(material);
            if (!itemModel.getTargetItems().contains(itemType)) return;
            Integer cmd = resources.getMeshes().getCustomModelData(mesh, itemType);
            if (cmd == null) return;
            tag.putInt(CUSTOM_MODEL_DATA_FIELD, cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
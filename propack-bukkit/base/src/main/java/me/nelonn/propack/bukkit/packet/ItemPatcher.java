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

import me.nelonn.configlib.PluginConfig;
import me.nelonn.flint.path.Key;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.CombinedItemModel;
import me.nelonn.propack.asset.DefaultItemModel;
import me.nelonn.propack.asset.ItemModel;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.bukkit.Config;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.adapter.MCompoundTag;
import me.nelonn.propack.bukkit.adapter.MItemStack;
import me.nelonn.propack.bukkit.adapter.MListTag;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.InvalidPathException;
import java.util.HashMap;
import java.util.Map;

public class ItemPatcher {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private static final int TAG_STRING = 8;

    private final PluginConfig config;

    public ItemPatcher(PluginConfig config) {
        this.config = config;
    }

    public boolean isDebug() {
        return config.get(Config.patchPacketDebugMode);
    }

    public void patchServerboundItem(@NotNull MItemStack itemStack) {
        MCompoundTag tag = itemStack.getCustomData();
        if (tag == null || !tag.contains(ProPack.CUSTOM_MODEL, TAG_STRING)) return;
        itemStack.removeCustomModelData();
    }

    public void patchClientboundItem(@NotNull MItemStack itemStack, @NotNull Resources resources) {
        try {
            MCompoundTag rootTag = itemStack.getCustomData();
            if (rootTag == null || !rootTag.contains(ProPack.CUSTOM_MODEL, TAG_STRING)) return;
            String customModel = rootTag.getString(ProPack.CUSTOM_MODEL);
            if (customModel.isEmpty()) return;
            Path path;
            try {
                path = Path.of(customModel);
            } catch (InvalidPathException e) {
                if (isDebug()) {
                    LOGGER.error("[ItemPatcherDebug] Invalid custom model path {}", customModel, e);
                }
                return;
            }
            ItemModel itemModel = resources.itemModel(path);
            if (itemModel == null) {
                if (isDebug()) {
                    LOGGER.error("[ItemPatcherDebug] Model not found {}", path);
                }
                return;
            }
            Path mesh;
            if (itemModel instanceof DefaultItemModel defaultItemModel) {
                mesh = defaultItemModel.getMesh();
            } else if (itemModel instanceof CombinedItemModel combinedItemModel) {
                MListTag listTag = rootTag.getList("CombinedItemModel", TAG_STRING);
                mesh = combinedItemModel.getMesh(listTag.asStringCollection().toArray(String[]::new));
            } else if (itemModel instanceof SlotItemModel slotItemModel) {
                MCompoundTag compoundTag = rootTag.getCompound("SlotItemModel");
                Map<String, String> slots = new HashMap<>();
                for (SlotItemModel.Slot slot : slotItemModel.getSlots()) {
                    String element = compoundTag.getString(slot.getName());
                    if (element.isEmpty()) continue;
                    slots.put(slot.getName(), element);
                }
                mesh = slotItemModel.getMesh(slots);
            } else {
                return;
            }
            Material material = Material.matchMaterial(itemStack.getItemId().toString());
            assert material != null;
            Key itemType = ProPack.adapt(material);
            if (!itemModel.getTargetItems().contains(itemType)) {
                if (isDebug()) {
                    LOGGER.error("[ItemPatcherDebug] Not target item {}", itemType);
                }
                return;
            }
            Integer cmd = resources.getMeshes().getCustomModelData(mesh, itemType);
            if (cmd == null) {
                if (isDebug()) {
                    LOGGER.error("[ItemPatcherDebug] {} custom model data not found for {}", itemType, mesh);
                }
                return;
            }
            if (itemStack.setNewItemModel(Path.of("propack", itemType.value() + "." + Integer.toHexString(cmd)))) {
                return;
            }
            itemStack.setCustomModelData(cmd);
        } catch (Exception e) {
            LOGGER.error("Failed to patch item", e);
        }
    }

}

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

import me.nelonn.flint.path.Identifier;
import me.nelonn.flint.path.InvalidPathException;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.CombinedItemModel;
import me.nelonn.propack.asset.DefaultItemModel;
import me.nelonn.propack.asset.ItemModel;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.adapter.MCompoundTag;
import me.nelonn.propack.bukkit.adapter.MItemStack;
import me.nelonn.propack.bukkit.adapter.MListTag;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PacketPatcher {
    private static final String CUSTOM_MODEL_DATA = "CustomModelData";
    private static final int TAG_INT = 3;
    private static final int TAG_STRING = 8;

    public void patchServerboundItems(MItemStack itemStack) {
        MCompoundTag tag = itemStack.getTag();
        if (tag == null ||
                !tag.contains(CUSTOM_MODEL_DATA, TAG_INT) ||
                !tag.contains(ProPack.CUSTOM_MODEL, TAG_STRING)) return;
        tag.remove(CUSTOM_MODEL_DATA);
    }

    public void patchClientboundItems(@NotNull MItemStack itemStack, @NotNull Resources resources) {
        try {
            MCompoundTag tag = itemStack.getTag();
            if (tag == null || !tag.contains(ProPack.CUSTOM_MODEL, TAG_STRING)) return;
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
                MListTag listTag = tag.getList("ModelElements", TAG_STRING);
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
            tag.putInt(CUSTOM_MODEL_DATA, cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

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

package me.nelonn.propack.bukkit.adapter.impl.v1_18_R2;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.bukkit.adapter.Adapter;
import me.nelonn.propack.bukkit.adapter.WrappedCompoundTag;
import me.nelonn.propack.bukkit.adapter.WrappedItemStack;
import me.nelonn.propack.bukkit.adapter.WrappedListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PaperweightAdapter implements Adapter {
    @Override
    public void patchSetCreativeSlot(@NotNull Object packet, @NotNull Consumer<WrappedItemStack> patcher) {
        ServerboundSetCreativeModeSlotPacket nms = (ServerboundSetCreativeModeSlotPacket) packet;
        patcher.accept(new CraftItemStack(nms.getItem()));
    }

    @Override
    public void patchSetSlot(@NotNull Object packet, @NotNull Consumer<WrappedItemStack> patcher) {
        ClientboundContainerSetSlotPacket nms = (ClientboundContainerSetSlotPacket) packet;
        patcher.accept(new CraftItemStack(nms.getItem()));
    }

    @Override
    public void patchSetContent(@NotNull Object packet, @NotNull Consumer<WrappedItemStack> patcher) {
        ClientboundContainerSetContentPacket nms = (ClientboundContainerSetContentPacket) packet;
        for (ItemStack itemStack : nms.getItems()) {
            patcher.accept(new CraftItemStack(itemStack));
        }
    }

    @Override
    public void patchEntityEquipment(@NotNull Object packet, @NotNull Consumer<WrappedItemStack> patcher) {
        ClientboundSetEquipmentPacket nms = (ClientboundSetEquipmentPacket) packet;
        for (Pair<EquipmentSlot, ItemStack> slot : nms.getSlots()) {
            patcher.accept(new CraftItemStack(slot.getSecond()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void patchSetEntityData(@NotNull Object packet, @NotNull Consumer<WrappedItemStack> patcher) {
        ClientboundSetEntityDataPacket nms = (ClientboundSetEntityDataPacket) packet;
        List<SynchedEntityData.DataItem<?>> unpackedData = nms.getUnpackedData();
        if (unpackedData == null) return;
        List<SynchedEntityData.DataItem<?>> list = unpackedData.stream()
                .filter(dataItem -> dataItem.getAccessor().getSerializer().equals(EntityDataSerializers.ITEM_STACK))
                .toList();
        for (SynchedEntityData.DataItem<?> entry : list) {
            unpackedData.remove(entry);
            SynchedEntityData.DataItem<ItemStack> newItem = (SynchedEntityData.DataItem<ItemStack>) entry.copy();
            patcher.accept(new CraftItemStack(newItem.getValue()));
            unpackedData.add(newItem);
        }
    }

    private static class CraftItemStack implements WrappedItemStack {
        private final ItemStack handle;

        public CraftItemStack(final @NotNull ItemStack handle) {
            this.handle = handle;
        }

        @Override
        public @NotNull Identifier getItemId() {
            return Identifier.of(handle.getItem().toString());
        }

        @Override
        public @Nullable CraftCompoundTag getTag() {
            return handle.hasTag() ? new CraftCompoundTag(handle.getTag()) : null;
        }
    }

    private static class CraftCompoundTag implements WrappedCompoundTag {
        private final CompoundTag handle;

        public CraftCompoundTag(final @NotNull CompoundTag handle) {
            this.handle = handle;
        }

        @Override
        public boolean contains(@NotNull String key, int type) {
            return handle.contains(key, type);
        }

        @Override
        public void putInt(@NotNull String key, int value) {
            handle.putInt(key, value);
        }

        @Override
        public @NotNull String getString(@NotNull String key) {
            return handle.getString(key);
        }

        @Override
        public @NotNull CraftCompoundTag getCompound(@NotNull String key) {
            return new CraftCompoundTag(handle.getCompound(key));
        }

        @Override
        public @NotNull CraftListTag getList(@NotNull String key, int type) {
            return new CraftListTag(handle.getList(key, type));
        }

        @Override
        public void remove(@NotNull String key) {
            handle.remove(key);
        }
    }

    public static class CraftListTag implements WrappedListTag {
        private final ListTag handle;

        public CraftListTag(final @NotNull ListTag handle) {
            this.handle = handle;
        }

        @Override
        public @NotNull Collection<String> asStringCollection() {
            return Lists.transform(handle, Tag::getAsString);
        }
    }
}

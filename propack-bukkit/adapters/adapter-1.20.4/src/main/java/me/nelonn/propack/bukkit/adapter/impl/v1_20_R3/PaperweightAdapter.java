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

package me.nelonn.propack.bukkit.adapter.impl.v1_20_R3;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import me.nelonn.flint.path.Key;
import me.nelonn.propack.bukkit.adapter.*;
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
    public void patchServerboundSetCreativeModeSlotPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        patcher.accept(ItemStackWrapper.of(((ServerboundSetCreativeModeSlotPacket) packet).getItem()));
    }

    @Override
    public void patchClientboundContainerSetSlotPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        patcher.accept(ItemStackWrapper.of(((ClientboundContainerSetSlotPacket) packet).getItem()));
    }

    @Override
    public void patchClientboundContainerSetContentPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundContainerSetContentPacket nms = (ClientboundContainerSetContentPacket) packet;
        for (ItemStack itemStack : nms.getItems()) {
            patcher.accept(ItemStackWrapper.of(itemStack));
        }
        patcher.accept(ItemStackWrapper.of(nms.getCarriedItem()));
    }

    @Override
    public void patchClientboundSetEntityEquipmentPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundSetEquipmentPacket nms = (ClientboundSetEquipmentPacket) packet;
        for (Pair<EquipmentSlot, ItemStack> slot : nms.getSlots()) {
            patcher.accept(ItemStackWrapper.of(slot.getSecond()));
        }
    }

    @Override
    public void patchClientboundSetEntityDataPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundSetEntityDataPacket nms = (ClientboundSetEntityDataPacket) packet;
        List<SynchedEntityData.DataValue<?>> dataValueList = nms.packedItems();
        for (SynchedEntityData.DataValue<?> dataValue : dataValueList) {
            if (!dataValue.serializer().equals(EntityDataSerializers.ITEM_STACK)) continue;
            patcher.accept(ItemStackWrapper.of((ItemStack) dataValue.value()));
        }
    }

    private static class ItemStackWrapper implements MItemStack {
        public static @Nullable ItemStackWrapper of(final @Nullable ItemStack handle) {
            return handle == null ? null : new ItemStackWrapper(handle);
        }

        private final ItemStack handle;

        public ItemStackWrapper(final @NotNull ItemStack handle) {
            this.handle = handle;
        }

        @Override
        public @NotNull Key getItemId() {
            return Key.of(handle.getItem().toString());
        }

        @Override
        public @Nullable CompoundTagWrapper getTag() {
            return CompoundTagWrapper.of(handle.getTag());
        }
    }

    private static class CompoundTagWrapper implements MCompoundTag {
        public static @Nullable CompoundTagWrapper of(final @Nullable CompoundTag handle) {
            return handle == null ? null : new CompoundTagWrapper(handle);
        }

        private final CompoundTag handle;

        public CompoundTagWrapper(final @NotNull CompoundTag handle) {
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
        public @NotNull CompoundTagWrapper getCompound(@NotNull String key) {
            return CompoundTagWrapper.of(handle.getCompound(key));
        }

        @Override
        public @NotNull PaperweightAdapter.ListTagWrapper getList(@NotNull String key, int type) {
            return ListTagWrapper.of(handle.getList(key, type));
        }

        @Override
        public void remove(@NotNull String key) {
            handle.remove(key);
        }
    }

    public static class ListTagWrapper implements MListTag {
        public static @Nullable ListTagWrapper of(final @Nullable ListTag handle) {
            return handle == null ? null : new ListTagWrapper(handle);
        }
        
        private final ListTag handle;

        public ListTagWrapper(final @NotNull ListTag handle) {
            this.handle = handle;
        }

        @Override
        public @NotNull Collection<String> asStringCollection() {
            return Lists.transform(handle, Tag::getAsString);
        }
    }
}

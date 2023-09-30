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

package me.nelonn.propack.bukkit.adapter.impl.v1_19_R3;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.bukkit.adapter.*;
import me.nelonn.propack.bukkit.adapter.packet.MClientboundContainerSetSlotPacket;
import me.nelonn.propack.bukkit.adapter.packet.MServerboundSetCreativeModeSlotPacket;
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

    private static class CraftServerboundSetCreativeModeSlotPacket implements MServerboundSetCreativeModeSlotPacket {
        public static @Nullable CraftServerboundSetCreativeModeSlotPacket of(final @Nullable ServerboundSetCreativeModeSlotPacket handle) {
            return handle == null ? null : new CraftServerboundSetCreativeModeSlotPacket(handle);
        }

        private final ServerboundSetCreativeModeSlotPacket handle;

        private CraftServerboundSetCreativeModeSlotPacket(final @NotNull ServerboundSetCreativeModeSlotPacket handle) {
            this.handle = handle;
        }

        @Override
        public int getSlotNum() {
            return handle.getSlotNum();
        }

        @Override
        public MItemStack getItem() {
            return CraftItemStack.of(handle.getItem());
        }
    }

    @Override
    public @NotNull MServerboundSetCreativeModeSlotPacket adaptPacket1(@NotNull Object packet) {
        return CraftServerboundSetCreativeModeSlotPacket.of((ServerboundSetCreativeModeSlotPacket) packet);
    }

    private static class CraftClientboundContainerSetSlotPacket implements MClientboundContainerSetSlotPacket {
        public static @Nullable CraftClientboundContainerSetSlotPacket of(final @Nullable ClientboundContainerSetSlotPacket handle) {
            return handle == null ? null : new CraftClientboundContainerSetSlotPacket(handle);
        }

        private final ClientboundContainerSetSlotPacket handle;

        private CraftClientboundContainerSetSlotPacket(final @NotNull ClientboundContainerSetSlotPacket handle) {
            this.handle = handle;
        }

        @Override
        public int getContainerId() {
            return handle.getContainerId();
        }

        @Override
        public int getSlot() {
            return handle.getSlot();
        }

        @Override
        public MItemStack getItem() {
            return CraftItemStack.of(handle.getItem());
        }

        @Override
        public int getStateId() {
            return handle.getStateId();
        }
    }

    @Override
    public @NotNull MClientboundContainerSetSlotPacket adaptPacket2(@NotNull Object packet) {
        return CraftClientboundContainerSetSlotPacket.of((ClientboundContainerSetSlotPacket) packet);
    }

    @Override
    public void patchSetContent(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundContainerSetContentPacket nms = (ClientboundContainerSetContentPacket) packet;
        for (ItemStack itemStack : nms.getItems()) {
            patcher.accept(CraftItemStack.of(itemStack));
        }
    }

    @Override
    public void patchEntityEquipment(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundSetEquipmentPacket nms = (ClientboundSetEquipmentPacket) packet;
        for (Pair<EquipmentSlot, ItemStack> slot : nms.getSlots()) {
            patcher.accept(CraftItemStack.of(slot.getSecond()));
        }
    }

    @Override
    public void patchSetEntityData(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundSetEntityDataPacket nms = (ClientboundSetEntityDataPacket) packet;
        List<SynchedEntityData.DataValue<?>> dataValueList = nms.packedItems();
        for (SynchedEntityData.DataValue<?> dataValue : dataValueList) {
            if (!dataValue.serializer().equals(EntityDataSerializers.ITEM_STACK)) continue;
            patcher.accept(CraftItemStack.of((ItemStack) dataValue.value()));
        }
    }

    private static class CraftItemStack implements MItemStack {
        public static @Nullable CraftItemStack of(final @Nullable ItemStack handle) {
            return handle == null ? null : new CraftItemStack(handle);
        }

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
            return CraftCompoundTag.of(handle.getTag());
        }
    }

    private static class CraftCompoundTag implements MCompoundTag {
        public static @Nullable CraftCompoundTag of(final @Nullable CompoundTag handle) {
            return handle == null ? null : new CraftCompoundTag(handle);
        }

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
            return CraftCompoundTag.of(handle.getCompound(key));
        }

        @Override
        public @NotNull CraftListTag getList(@NotNull String key, int type) {
            return CraftListTag.of(handle.getList(key, type));
        }

        @Override
        public void remove(@NotNull String key) {
            handle.remove(key);
        }
    }

    public static class CraftListTag implements MListTag {
        public static @Nullable CraftListTag of(final @Nullable ListTag handle) {
            return handle == null ? null : new CraftListTag(handle);
        }
        
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

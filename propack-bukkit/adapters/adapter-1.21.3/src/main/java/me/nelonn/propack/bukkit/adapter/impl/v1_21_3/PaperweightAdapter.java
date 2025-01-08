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

package me.nelonn.propack.bukkit.adapter.impl.v1_21_3;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import me.nelonn.flint.path.Key;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.bukkit.Util;
import me.nelonn.propack.bukkit.adapter.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PaperweightAdapter implements Adapter {

    @Override
    public @NotNull Class<?> getServerboundSetCreativeModeSlotPacket() {
        return ServerboundSetCreativeModeSlotPacket.class;
    }

    @Override
    public @NotNull Class<?> getClientboundContainerSetSlotPacket() {
        return ClientboundContainerSetSlotPacket.class;
    }

    @Override
    public @NotNull Class<?> getClientboundContainerSetContentPacket() {
        return ClientboundContainerSetContentPacket.class;
    }

    @Override
    public @NotNull Class<?> getClientboundSetEntityEquipmentPacket() {
        return ClientboundSetEquipmentPacket.class;
    }

    @Override
    public @NotNull Class<?> getClientboundSetEntityDataPacket() {
        return ClientboundSetEntityDataPacket.class;
    }

    @Override
    public @NotNull Class<?> getClientboundSoundPacket() {
        return ClientboundSoundPacket.class;
    }

    @Override
    public @Nullable Class<?> getClientboundCustomSoundPacket() {
        return null;
    }

    @Override
    public @NotNull Class<?> getClientboundSoundEntityPacket() {
        return ClientboundSoundEntityPacket.class;
    }

    static class PacketInjector extends MessageToMessageCodec<Packet<?>, Packet<?>> {
        private final Player player;
        private final IPacketListener listener;

        public PacketInjector(@NotNull Player player, @NotNull IPacketListener listener) {
            this.player = player;
            this.listener = listener;
        }

        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) {
            if (listener.onPacketSend(player, packet) instanceof Packet<?> next) {
                list.add(next);
            }
        }

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) {
            if (listener.onPacketReceive(player, packet) instanceof Packet<?> next) {
                list.add(next);
            }
        }
    }

    @Override
    public void inject(@NotNull Player player, @NotNull IPacketListener listener) {
        ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline().addBefore("packet_handler", "propack_injector", new PacketInjector(player, listener));
    }

    @Override
    public @NotNull Object patchServerboundSetCreativeModeSlotPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        patcher.accept(ItemStackWrapper.of(((ServerboundSetCreativeModeSlotPacket) packet).itemStack()));
        return packet;
    }

    @Override
    public @NotNull Object patchClientboundContainerSetSlotPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        patcher.accept(ItemStackWrapper.of(((ClientboundContainerSetSlotPacket) packet).getItem()));
        return packet;
    }

    @Override
    public @NotNull Object patchClientboundContainerSetContentPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundContainerSetContentPacket nms = (ClientboundContainerSetContentPacket) packet;
        for (ItemStack itemStack : nms.getItems()) {
            patcher.accept(ItemStackWrapper.of(itemStack));
        }
        patcher.accept(ItemStackWrapper.of(nms.getCarriedItem()));
        return packet;
    }

    @Override
    public @NotNull Object patchClientboundSetEntityEquipmentPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundSetEquipmentPacket nms = (ClientboundSetEquipmentPacket) packet;
        for (Pair<EquipmentSlot, ItemStack> slot : nms.getSlots()) {
            patcher.accept(ItemStackWrapper.of(slot.getSecond()));
        }
        return packet;
    }

    @Override
    public @NotNull Object patchClientboundSetEntityDataPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher) {
        ClientboundSetEntityDataPacket nms = (ClientboundSetEntityDataPacket) packet;
        List<SynchedEntityData.DataValue<?>> dataValueList = nms.packedItems();
        for (SynchedEntityData.DataValue<?> dataValue : dataValueList) {
            if (!dataValue.serializer().equals(EntityDataSerializers.ITEM_STACK)) continue;
            patcher.accept(ItemStackWrapper.of((ItemStack) dataValue.value()));
        }
        return packet;
    }

    private SoundEvent recreateSound(SoundEvent original, ResourceLocation name) {
        float zeroRange = original.getRange(2.0F);
        float twoRange = original.getRange(3.0F);
        if (zeroRange != twoRange) {
            return SoundEvent.createVariableRangeEvent(name);
        } else {
            return SoundEvent.createFixedRangeEvent(name, zeroRange);
        }
    }

    @Override
    public @NotNull Object patchClientboundSoundPacket(@NotNull Object packet, @NotNull Function<String, String> patcher) {
        ClientboundSoundPacket nms = (ClientboundSoundPacket) packet;
        if (nms.getSound().kind() != Holder.Kind.DIRECT) return packet;
        SoundEvent soundEvent = nms.getSound().value();
        String originalSound = soundEvent.location().toString();
        String patchedSound = patcher.apply(originalSound);
        if (originalSound.equals(patchedSound)) return packet;
        return new ClientboundSoundPacket(
                Holder.direct(recreateSound(soundEvent, ResourceLocation.parse(patchedSound))),
                nms.getSource(),
                nms.getX(),
                nms.getY(),
                nms.getZ(),
                nms.getVolume(),
                nms.getPitch(),
                nms.getSeed()
        );
    }

    @Override
    public @NotNull Object patchClientboundSoundEntityPacket(@NotNull Object packet, @NotNull Function<String, String> patcher) {
        ClientboundSoundEntityPacket nms = (ClientboundSoundEntityPacket) packet;
        if (nms.getSound().kind() != Holder.Kind.DIRECT) return packet;
        SoundEvent soundEvent = nms.getSound().value();
        String originalSound = soundEvent.location().toString();
        String patchedSound = patcher.apply(originalSound);
        if (originalSound.equals(patchedSound)) return packet;
        Entity entity = new Marker(EntityType.MARKER, null);
        entity.setId(nms.getId());
        return new ClientboundSoundEntityPacket(
                Holder.direct(recreateSound(soundEvent, ResourceLocation.parse(patchedSound))),
                nms.getSource(),
                entity,
                nms.getVolume(),
                nms.getPitch(),
                nms.getSeed()
        );
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

        @SuppressWarnings("deprecation")
        @Override
        public @Nullable CompoundTagWrapper getCustomData() {
            return Util.map(handle.get(DataComponents.CUSTOM_DATA), customData -> CompoundTagWrapper.of(customData.getUnsafe()));
        }

        @Override
        public void setCustomModelData(int customModelData) {
        }

        @Override
        public boolean setNewItemModel(Path itemModel) {
            handle.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(itemModel.namespace(), itemModel.value()));
            return true;
        }

        @Override
        public void removeCustomModelData() {
            handle.remove(DataComponents.CUSTOM_MODEL_DATA);
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

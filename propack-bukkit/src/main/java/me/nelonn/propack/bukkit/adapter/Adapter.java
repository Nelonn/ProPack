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

package me.nelonn.propack.bukkit.adapter;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Adapter {

    @NotNull Class<?> getServerboundSetCreativeModeSlotPacket();

    @NotNull Class<?> getClientboundContainerSetSlotPacket();

    @NotNull Class<?> getClientboundContainerSetContentPacket();

    @NotNull Class<?> getClientboundSetEntityEquipmentPacket();

    @NotNull Class<?> getClientboundSetEntityDataPacket();

    @NotNull Class<?> getClientboundSoundPacket();

    @Nullable Class<?> getClientboundCustomSoundPacket(); // 1.17-1.18

    @NotNull Class<?> getClientboundSoundEntityPacket();

    void inject(@NotNull Player player, @NotNull IPacketListener packetListener);

    @NotNull Object patchServerboundSetCreativeModeSlotPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);

    @NotNull Object patchClientboundContainerSetSlotPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);

    @NotNull Object patchClientboundContainerSetContentPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);

    @NotNull Object patchClientboundSetEntityEquipmentPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);

    @NotNull Object patchClientboundSetEntityDataPacket(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);

    @NotNull Object patchClientboundSoundPacket(@NotNull Object packet, @NotNull Function<String, String> patcher);

    @NotNull Object patchClientboundSoundEntityPacket(@NotNull Object packet, @NotNull Function<String, String> patcher);
}

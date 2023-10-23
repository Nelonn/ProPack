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

import me.nelonn.propack.bukkit.adapter.packet.MClientboundContainerSetSlotPacket;
import me.nelonn.propack.bukkit.adapter.packet.MServerboundSetCreativeModeSlotPacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Adapter {
    @NotNull MServerboundSetCreativeModeSlotPacket wrap_ServerboundSetCreativeModeSlotPacket(@NotNull Object packet);

    @NotNull MClientboundContainerSetSlotPacket wrap_ClientboundContainerSetSlotPacket(@NotNull Object packet);

    void patchSetContent(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);

    void patchEntityEquipment(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);

    void patchSetEntityData(@NotNull Object packet, @NotNull Consumer<MItemStack> patcher);
}

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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.nelonn.propack.bukkit.ProPackPlugin;
import org.bukkit.entity.Player;

public class DuplexPacketHandler extends ChannelDuplexHandler {
    private final Player player;
    private final IPacketListener packetListener;

    public DuplexPacketHandler(Player player, IPacketListener packetListener) {
        this.player = player;
        this.packetListener = packetListener;
    }

    // On Server: Serverbound
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        packet = packetListener.onPacketReceive(player, packet);
        if (packet == null) return;
        try {
            super.channelRead(ctx, packet);
        } catch (Throwable e) {
            ProPackPlugin.getInstance().getSLF4JLogger().error("DuplexPacketHandler: {}", e.getMessage());
        }
    }

    // On Server: Clientbound
    @Override
    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
        packet = packetListener.onPacketSend(player, packet);
        if (packet == null) return;
        try {
            super.write(ctx, packet, promise);
        } catch (Throwable e) {
            ProPackPlugin.getInstance().getSLF4JLogger().error("DuplexPacketHandler: {}", e.getMessage());
        }
    }

}

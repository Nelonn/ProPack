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

package me.nelonn.propack.bukkit.dispatcher;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.nelonn.propack.bukkit.ResourcePackOffer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProtocolPackSender {
    private final ProtocolManager protocolManager;

    public ProtocolPackSender() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void send(@NotNull Player player, @NotNull ResourcePackOffer packOffer) {
        //player.setResourcePack(packOffer.getUpload().getUrl(), packOffer.getUpload().getSha1String(), packOffer.getShouldForce(), packOffer.getPrompt());
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.RESOURCE_PACK_SEND);
        packet.getStrings().write(0, packOffer.getUpload().getUrl());
        packet.getStrings().write(1, packOffer.getUpload().getSha1String());
        packet.getBooleans().write(0, packOffer.getShouldForce());
        packet.getChatComponents().write(0, toProtocolLike(packOffer.getPrompt()));
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static WrappedChatComponent toProtocolLike(Component component) {
        return WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component));
    }

}

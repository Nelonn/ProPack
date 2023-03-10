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

package me.nelonn.propack.bukkit.dispatcher.sender;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.bukkit.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProtocolPackSender implements PackSender {
    private final ProtocolManager protocolManager;

    public ProtocolPackSender() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void send(@NotNull Player player, @NotNull UploadedPack uploadedPack) {
        Component component = MiniMessage.miniMessage().deserialize(Config.DISPATCHER_PROMPT.asString(),
                Placeholder.component("player", Component.text(player.getName())),
                Placeholder.component("pack_name", Component.text(uploadedPack.getName())));
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.RESOURCE_PACK_SEND);
        packet.getStrings().write(0, uploadedPack.getUrl());
        packet.getStrings().write(1, uploadedPack.getSha1String());
        packet.getBooleans().write(0, Config.DISPATCHER_REQUIRED.asBoolean());
        packet.getChatComponents().write(0, toProtocolLike(component));
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WrappedChatComponent toProtocolLike(Component component) {
        return WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component));
    }

}

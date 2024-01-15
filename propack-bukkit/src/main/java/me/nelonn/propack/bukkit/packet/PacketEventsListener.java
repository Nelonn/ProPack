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

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.SoundAsset;
import me.nelonn.propack.bukkit.Config;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketEventsListener extends PacketListenerAbstract {
    private final ProPackPlugin plugin;

    public PacketEventsListener(@NotNull ProPackPlugin plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!plugin.config().get(Config.patchPacketSounds)) return;

        Player player = (Player) event.getPlayer();
        if (player == null) return;
        ResourcePack resourcePack = ProPack.getCore().getDispatcher().getAppliedResourcePack(player);
        if (resourcePack == null) return;
        final Resources resources = resourcePack.resources();

        SoundPacket soundPacket = event.getPacketType() == PacketType.Play.Server.SOUND_EFFECT ? new WrapperPlayServerSoundEffect(event) :
                                    event.getPacketType() == PacketType.Play.Server.ENTITY_SOUND_EFFECT ? new WrapperPlayServerEntitySoundEffect(event) :
                                            null;
        if (soundPacket == null) return;
        if (soundPacket.getSoundId() != 0) return;
        String soundName = soundPacket.getSoundName();
        if (soundName == null) return;
        Path path = Path.tryOrNull(soundName);
        if (path == null) return;
        SoundAsset soundAsset = resources.sound(path);
        if (soundAsset == null) return;
        soundPacket.setSoundName(soundAsset.realPath().toString());
    }
}

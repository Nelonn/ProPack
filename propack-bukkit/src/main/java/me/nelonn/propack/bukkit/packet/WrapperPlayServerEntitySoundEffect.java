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

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerEntitySoundEffect extends PacketWrapper<WrapperPlayServerEntitySoundEffect> implements SoundPacket {
    private int soundId;
    private String soundName;
    private SoundCategory soundCategory;
    private int entityId;
    private float volume;
    private float pitch;

    public WrapperPlayServerEntitySoundEffect(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerEntitySoundEffect(int soundId, SoundCategory soundCategory, int entityId, float volume, float pitch) {
        super(PacketType.Play.Server.ENTITY_SOUND_EFFECT);
        this.soundId = soundId;
        this.soundCategory = soundCategory;
        this.entityId = entityId;
        this.volume = volume;
        this.pitch = pitch;
    }

    public WrapperPlayServerEntitySoundEffect(String soundName, SoundCategory soundCategory, int entityId, float volume, float pitch) {
        super(PacketType.Play.Server.ENTITY_SOUND_EFFECT);
        this.soundName = soundName;
        this.soundCategory = soundCategory;
        this.entityId = entityId;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void read() {
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20)) {
            soundId = readVarInt();
            if (soundId == 0) {
                soundName = readIdentifier().toString();
                readOptional(PacketWrapper::readFloat);
            }
            soundCategory = SoundCategory.fromId(readVarInt());
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            soundId = readVarInt();
            soundCategory = SoundCategory.fromId(readVarInt());
        } else {
            soundName = readString();
        }
        this.soundCategory = SoundCategory.fromId(readVarInt());
        this.entityId = readVarInt();
        this.volume = readFloat();
        this.pitch = readFloat();
    }

    @Override
    public void write() {
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20)) {
            writeVarInt(soundId);
            if (soundId == 0) {
                writeIdentifier(new ResourceLocation(soundName));
                writeOptional(16.0F, PacketWrapper::writeFloat);
            }
            writeVarInt(soundCategory.ordinal());
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            writeVarInt(soundId);
            writeVarInt(soundCategory.ordinal());
        } else {
            writeString(soundName);
        }
        writeVarInt(this.soundCategory.ordinal());
        writeVarInt(this.entityId);
        writeFloat(this.volume);
        writeFloat(this.pitch);
    }

    @Override
    public void copy(WrapperPlayServerEntitySoundEffect wrapper) {
        this.soundId = wrapper.soundId;
        this.soundCategory = wrapper.soundCategory;
        this.entityId = wrapper.entityId;
        this.volume = wrapper.volume;
        this.pitch = wrapper.pitch;
    }

    @Override
    public int getSoundId() {
        return soundId;
    }

    @Override
    public void setSoundId(int soundID) {
        this.soundId = soundID;
    }

    @Override
    public @Nullable String getSoundName() {
        return soundName;
    }

    @Override
    public void setSoundName(@Nullable String soundName) {
        this.soundName = soundName;
    }

    public SoundCategory getSoundCategory() {
        return soundCategory;
    }

    public void setSoundCategory(SoundCategory soundCategory) {
        this.soundCategory = soundCategory;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
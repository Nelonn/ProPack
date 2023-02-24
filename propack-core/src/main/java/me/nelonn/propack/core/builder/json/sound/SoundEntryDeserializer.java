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

package me.nelonn.propack.core.builder.json.sound;

import com.google.common.collect.Lists;
import com.google.gson.*;
import me.nelonn.propack.core.util.GsonHelper;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

public class SoundEntryDeserializer implements JsonSerializer<SoundEntry>, JsonDeserializer<SoundEntry> {
    public static final SoundEntryDeserializer INSTANCE = new SoundEntryDeserializer();
    private static final float DEFAULT_FLOAT = 1.0F;

    protected SoundEntryDeserializer() {
    }

    @Override
    public JsonElement serialize(SoundEntry src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        if (src.canReplace()) {
            result.addProperty("replace", true);
        }
        if (src.getSubtitle() != null) {
            result.addProperty("subtitle", src.getSubtitle());
        }
        if (src.getSounds().size() > 0) {
            JsonArray sounds = new JsonArray();
            result.add("sounds", sounds);
            for (Sound sound : src.getSounds()) {
                if (sound.getVolume() == 1 &&
                        sound.getPitch() == 1 &&
                        sound.getWeight() == 1 &&
                        sound.getRegistrationType() == Sound.RegistrationType.FILE &&
                        !sound.isPreloaded() &&
                        !sound.isStreamed() &&
                        sound.getAttenuation() == 16) {
                    sounds.add(sound.getName());
                } else {
                    JsonObject soundObject = new JsonObject();
                    sounds.add(soundObject);
                    soundObject.addProperty("name", sound.getName());
                    if (sound.getVolume() != 1) {
                        soundObject.addProperty("volume", sound.getVolume());
                    }
                    if (sound.getPitch() != 1) {
                        soundObject.addProperty("pitch", sound.getPitch());
                    }
                    if (sound.getWeight() != 1) {
                        soundObject.addProperty("weight", sound.getWeight());
                    }
                    if (sound.getRegistrationType() != Sound.RegistrationType.FILE) {
                        soundObject.addProperty("type", Sound.RegistrationType.SOUND_EVENT.getName());
                    }
                    if (sound.isPreloaded()) {
                        soundObject.addProperty("preload", true);
                    }
                    if (sound.isStreamed()) {
                        soundObject.addProperty("stream", true);
                    }
                    if (sound.getAttenuation() != 16) {
                        soundObject.addProperty("attenuation_distance", sound.getAttenuation());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public SoundEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = GsonHelper.asObject(json, "entry");
        boolean bl = GsonHelper.getBoolean(jsonObject, "replace", false);
        String string = GsonHelper.getString(jsonObject, "subtitle", null);
        List<Sound> list = this.deserializeSounds(jsonObject);
        return new SoundEntry(list, bl, string);
    }

    private @NotNull List<Sound> deserializeSounds(@NotNull JsonObject json) {
        List<Sound> list = Lists.newArrayList();
        if (json.has("sounds")) {
            JsonArray jsonArray = GsonHelper.getArray(json, "sounds");
            for (JsonElement jsonElement : jsonArray) {
                if (GsonHelper.isString(jsonElement)) {
                    String name = GsonHelper.asString(jsonElement, "sound");
                    list.add(new Sound(name, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.RegistrationType.FILE, false, false, 16));
                } else {
                    list.add(this.deserializeSound(GsonHelper.asObject(jsonElement, "sound")));
                }
            }
        }
        return list;
    }

    private @NotNull Sound deserializeSound(@NotNull JsonObject json) {
        String name = GsonHelper.getString(json, "name");
        Sound.RegistrationType registrationType = this.deserializeType(json);
        float volume = GsonHelper.getFloat(json, "volume", 1.0F);
        Validate.isTrue(volume > 0.0F, "Invalid volume");
        float pitch = GsonHelper.getFloat(json, "pitch", 1.0F);
        Validate.isTrue(pitch > 0.0F, "Invalid pitch");
        int weight = GsonHelper.getInt(json, "weight", 1);
        Validate.isTrue(weight > 0, "Invalid weight");
        boolean preload = GsonHelper.getBoolean(json, "preload", false);
        boolean stream = GsonHelper.getBoolean(json, "stream", false);
        int attenuationDistance = GsonHelper.getInt(json, "attenuation_distance", 16);
        return new Sound(name, volume, pitch, weight, registrationType, stream, preload, attenuationDistance);
    }

    private Sound.@NotNull RegistrationType deserializeType(JsonObject json) {
        Sound.RegistrationType registrationType = Sound.RegistrationType.FILE;
        if (json.has("type")) {
            registrationType = Sound.RegistrationType.getByName(GsonHelper.getString(json, "type"));
            Validate.notNull(registrationType, "Invalid type");
        }
        return registrationType;
    }
}
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

package me.nelonn.propack.core.builder.json.sound;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Sound {
    private final String name;
    private final float volume;
    private final float pitch;
    private final int weight;
    private final RegistrationType registrationType;
    private final boolean stream;
    private final boolean preload;
    private final int attenuation;

    public Sound(String name, float volume, float pitch, int weight, RegistrationType registrationType, boolean stream, boolean preload, int attenuation) {
        this.name = name;
        this.volume = volume;
        this.pitch = pitch;
        this.weight = weight;
        this.registrationType = registrationType;
        this.stream = stream;
        this.preload = preload;
        this.attenuation = attenuation;
    }

    public String getName() {
        return this.name;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getWeight() {
        return this.weight;
    }

    public RegistrationType getRegistrationType() {
        return this.registrationType;
    }

    public boolean isStreamed() {
        return this.stream;
    }

    public boolean isPreloaded() {
        return this.preload;
    }

    public int getAttenuation() {
        return this.attenuation;
    }

    public String toString() {
        return "Sound[" + name + "]";
    }

    public enum RegistrationType {
        FILE("file"),
        SOUND_EVENT("event");

        private final String name;

        RegistrationType(@NotNull String name) {
            this.name = name;
        }

        public @NotNull String getName() {
            return name;
        }

        @Nullable
        public static RegistrationType getByName(String name) {
            for (RegistrationType registrationType : values()) {
                if (registrationType.name.equalsIgnoreCase(name)) {
                    return registrationType;
                }
            }
            return null;
        }
    }
}
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

package me.nelonn.propack.core.builder;

import me.nelonn.flint.path.PathImpl;
import org.jetbrains.annotations.NotNull;

public class ObfuscationConfiguration {
    private final boolean enabled;
    private final String namespace;
    private final boolean shuffleSequence;
    private final boolean meshes;
    private final boolean textures;
    private final boolean ogg;
    private final boolean sounds;
    private final boolean fonts;

    public ObfuscationConfiguration(boolean enabled, @NotNull String namespace, boolean shuffleSequence, boolean meshes,
                                    boolean textures, boolean ogg, boolean sounds, boolean fonts) {
        if (!PathImpl.isValidNamespace(namespace)) {
            throw new IllegalArgumentException("Illegal namespace: " + namespace);
        }
        this.enabled = enabled;
        this.namespace = namespace;
        this.shuffleSequence = shuffleSequence;
        this.meshes = meshes;
        this.textures = textures;
        this.ogg = ogg;
        this.sounds = sounds;
        this.fonts = fonts;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isShuffleSequence() {
        return shuffleSequence;
    }

    public boolean isMeshes() {
        return meshes;
    }

    public boolean isTextures() {
        return textures;
    }

    public boolean isOgg() {
        return ogg;
    }

    public boolean isSounds() {
        return sounds;
    }

    public boolean isFonts() {
        return fonts;
    }
}

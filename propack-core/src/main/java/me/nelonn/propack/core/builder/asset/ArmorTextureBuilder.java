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

package me.nelonn.propack.core.builder.asset;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.asset.ArmorTexture;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ArmorTextureBuilder extends AbstractAssetBuilder<ArmorTexture> {
    private Color color;
    private boolean hasLayer1;
    private boolean hasLayer2;

    public ArmorTextureBuilder(@NotNull Path path) {
        super(path);
    }

    public Color getColor() {
        return color;
    }

    public ArmorTextureBuilder setColor(Color color) {
        this.color = color;
        return this;
    }

    public boolean hasLayer1() {
        return hasLayer1;
    }

    public ArmorTextureBuilder setHasLayer1(boolean hasLayer1) {
        this.hasLayer1 = hasLayer1;
        return this;
    }

    public boolean hasLayer2() {
        return hasLayer2;
    }

    public ArmorTextureBuilder setHasLayer2(boolean hasLayer2) {
        this.hasLayer2 = hasLayer2;
        return this;
    }

    public @NotNull ArmorTexture build(@NotNull ResourcePack resourcePack) {
        return new ArmorTexture(resourcePack, path, color, hasLayer1, hasLayer2);
    }
}

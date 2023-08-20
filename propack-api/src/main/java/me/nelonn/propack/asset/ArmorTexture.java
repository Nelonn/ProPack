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

package me.nelonn.propack.asset;

import me.nelonn.flint.path.Path;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ArmorTexture extends AbstractAsset {
    private final Color color;
    private final boolean hasLayer1;
    private final boolean hasLayer2;

    public ArmorTexture(@NotNull Path path, @NotNull Color color, boolean hasLayer1, boolean hasLayer2) {
        super(path);
        this.color = color;
        this.hasLayer1 = hasLayer1;
        this.hasLayer2 = hasLayer2;
    }

    public @NotNull Color getColor() {
        return color;
    }

    public boolean hasLayer1() {
        return hasLayer1;
    }

    public boolean hasLayer2() {
        return hasLayer2;
    }
}

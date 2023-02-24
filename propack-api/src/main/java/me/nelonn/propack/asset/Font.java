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
import me.nelonn.propack.ResourcePack;
import org.jetbrains.annotations.NotNull;

public class Font extends AbstractAsset {
    private final Path fontPath;

    public Font(@NotNull ResourcePack resourcePack, @NotNull Path path, @NotNull Path fontPath) {
        super(resourcePack, path);
        this.fontPath = fontPath;
    }

    /**
     * Assets can be obfuscated, so this method will return an obfuscated path (if enabled)
     * @return Real path of asset
     */
    public @NotNull Path getFontPath() {
        return fontPath;
    }
}

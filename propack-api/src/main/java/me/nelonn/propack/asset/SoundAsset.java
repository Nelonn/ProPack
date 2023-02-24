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
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public class SoundAsset extends AbstractAsset {
    private final Path soundPath;

    public SoundAsset(@NotNull ResourcePack resourcePack, @NotNull Path path, @NotNull Path soundPath) {
        super(resourcePack, path);
        this.soundPath = soundPath;
    }

    /**
     * Assets can be obfuscated, so this method will return an obfuscated path (if enabled)
     * @return Real path of asset
     */
    public @NotNull Path getSoundPath() {
        return soundPath;
    }

    /**
     * Adventure api implementation. This method uses output from getAssetPath()
     * @return Sound.Type of this Sound Asset
     */
    public @NotNull Sound.Type asType() {
        return this::getSoundPath;
    }
}

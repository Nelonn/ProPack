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
import me.nelonn.propack.asset.SoundAsset;
import org.jetbrains.annotations.NotNull;

public class SoundAssetBuilder extends AbstractAssetBuilder<SoundAsset> {
    private Path soundPath;

    public SoundAssetBuilder(@NotNull Path path) {
        super(path);
    }

    public Path getSoundPath() {
        return soundPath;
    }

    public SoundAssetBuilder setSoundPath(Path soundPath) {
        this.soundPath = soundPath;
        return this;
    }

    public @NotNull SoundAsset build() {
        return new SoundAsset(path, soundPath);
    }
}

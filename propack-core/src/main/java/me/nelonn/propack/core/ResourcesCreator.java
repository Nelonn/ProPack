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

package me.nelonn.propack.core;

import me.nelonn.propack.Meshes;
import me.nelonn.propack.Resources;
import me.nelonn.propack.core.asset.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class ResourcesCreator {
    public static Resources create(@NotNull Collection<ItemModelBuilder> itemModels,
                                   @NotNull Collection<SoundAssetBuilder> soundAssets,
                                   @NotNull Collection<ArmorTextureBuilder> armorTextures,
                                   @NotNull Collection<FontBuilder> fonts,
                                   @NotNull Meshes meshes) {
        return new Resources(
                itemModels.stream().map(AssetBuilder::build).collect(Collectors.toList()),
                soundAssets.stream().map(AssetBuilder::build).collect(Collectors.toList()),
                armorTextures.stream().map(AssetBuilder::build).collect(Collectors.toList()),
                fonts.stream().map(AssetBuilder::build).collect(Collectors.toList()),
                meshes
        );
    }
}

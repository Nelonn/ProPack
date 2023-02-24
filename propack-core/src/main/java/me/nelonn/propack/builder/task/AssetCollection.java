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

package me.nelonn.propack.builder.task;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.core.builder.asset.ArmorTextureBuilder;
import me.nelonn.propack.core.builder.asset.FontBuilder;
import me.nelonn.propack.core.builder.asset.ItemModelBuilder;
import me.nelonn.propack.core.builder.asset.SoundAssetBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface AssetCollection {

    void putItemModel(@NotNull ItemModelBuilder itemModel);

    @Nullable ItemModelBuilder getItemModel(@NotNull Path path);

    @Nullable ItemModelBuilder removeItemModel(@NotNull Path path);

    @NotNull Collection<ItemModelBuilder> getItemModels();

    void putSound(@NotNull SoundAssetBuilder soundAsset);

    @Nullable SoundAssetBuilder getSound(@NotNull Path path);

    @Nullable SoundAssetBuilder removeSound(@NotNull Path path);

    @NotNull Collection<SoundAssetBuilder> getSounds();

    void putArmorTexture(@NotNull ArmorTextureBuilder armorTexture);

    @Nullable ArmorTextureBuilder getArmorTexture(@NotNull Path path);

    @Nullable ArmorTextureBuilder removeArmorTexture(@NotNull Path path);

    @NotNull Collection<ArmorTextureBuilder> getArmorTextures();

    void putFont(@NotNull FontBuilder font);

    @Nullable FontBuilder getFont(@NotNull Path path);

    @Nullable FontBuilder removeFont(@NotNull Path path);

    @NotNull Collection<FontBuilder> getFonts();

}

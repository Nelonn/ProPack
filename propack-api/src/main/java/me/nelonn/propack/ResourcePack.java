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

package me.nelonn.propack;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.ArmorTexture;
import me.nelonn.propack.asset.Font;
import me.nelonn.propack.asset.ItemModel;
import me.nelonn.propack.asset.SoundAsset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public interface ResourcePack {

    @NotNull String getName();

    @Nullable ItemModel getItemModel(@NotNull Path path);

    @NotNull Collection<ItemModel> getItemModels();

    @Nullable SoundAsset getSound(@NotNull Path path);

    @NotNull Collection<SoundAsset> getSounds();

    @Nullable ArmorTexture getArmorTexture(@NotNull Path path);

    @NotNull Collection<ArmorTexture> getArmorTextures();

    @Nullable Font getFont(@NotNull Path path);

    @NotNull Collection<Font> getFonts();

    @NotNull MeshMapping getMeshMapping();

    @NotNull Optional<UploadedPack> getUpload();

    default boolean isUploaded() {
        return getUpload().isPresent();
    }

}

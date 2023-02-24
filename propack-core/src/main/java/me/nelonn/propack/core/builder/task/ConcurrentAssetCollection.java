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

package me.nelonn.propack.core.builder.task;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.task.AssetCollection;
import me.nelonn.propack.core.builder.asset.ArmorTextureBuilder;
import me.nelonn.propack.core.builder.asset.FontBuilder;
import me.nelonn.propack.core.builder.asset.ItemModelBuilder;
import me.nelonn.propack.core.builder.asset.SoundAssetBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentAssetCollection implements AssetCollection {
    private final Map<Path, ItemModelBuilder> itemModels;
    private final Map<Path, SoundAssetBuilder> soundAssets;
    private final Map<Path, ArmorTextureBuilder> armorTextures;
    private final Map<Path, FontBuilder> fonts;

    public ConcurrentAssetCollection() {
        itemModels = new ConcurrentHashMap<>();
        soundAssets = new ConcurrentHashMap<>();
        armorTextures = new ConcurrentHashMap<>();
        fonts = new ConcurrentHashMap<>();
    }

    @Override
    public void putItemModel(@NotNull ItemModelBuilder itemModel) {
        itemModels.put(itemModel.getPath(), itemModel);
    }

    @Override
    public @Nullable ItemModelBuilder getItemModel(@NotNull Path path) {
        return itemModels.get(path);
    }

    @Override
    public @Nullable ItemModelBuilder removeItemModel(@NotNull Path path) {
        return itemModels.remove(path);
    }

    @Override
    public @NotNull Collection<ItemModelBuilder> getItemModels() {
        return itemModels.values();
    }

    @Override
    public void putSound(@NotNull SoundAssetBuilder soundAsset) {
        soundAssets.put(soundAsset.getPath(), soundAsset);
    }

    @Override
    public @Nullable SoundAssetBuilder getSound(@NotNull Path path) {
        return soundAssets.get(path);
    }

    @Override
    public @Nullable SoundAssetBuilder removeSound(@NotNull Path path) {
        return soundAssets.remove(path);
    }

    @Override
    public @NotNull Collection<SoundAssetBuilder> getSounds() {
        return soundAssets.values();
    }

    @Override
    public void putArmorTexture(@NotNull ArmorTextureBuilder armorTexture) {
        armorTextures.put(armorTexture.getPath(), armorTexture);
    }

    @Override
    public @Nullable ArmorTextureBuilder getArmorTexture(@NotNull Path path) {
        return armorTextures.get(path);
    }

    @Override
    public @Nullable ArmorTextureBuilder removeArmorTexture(@NotNull Path path) {
        return armorTextures.remove(path);
    }

    @Override
    public @NotNull Collection<ArmorTextureBuilder> getArmorTextures() {
        return armorTextures.values();
    }

    @Override
    public void putFont(@NotNull FontBuilder font) {
        fonts.put(font.getPath(), font);
    }

    @Override
    public @Nullable FontBuilder getFont(@NotNull Path path) {
        return fonts.get(path);
    }

    @Override
    public @Nullable FontBuilder removeFont(@NotNull Path path) {
        return fonts.remove(path);
    }

    @Override
    public @NotNull Collection<FontBuilder> getFonts() {
        return fonts.values();
    }
}

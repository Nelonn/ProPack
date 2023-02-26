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

import me.nelonn.flint.path.Path;
import me.nelonn.propack.MeshMapping;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.asset.ArmorTexture;
import me.nelonn.propack.asset.Font;
import me.nelonn.propack.asset.ItemModel;
import me.nelonn.propack.asset.SoundAsset;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.core.builder.asset.ArmorTextureBuilder;
import me.nelonn.propack.core.builder.asset.FontBuilder;
import me.nelonn.propack.core.builder.asset.ItemModelBuilder;
import me.nelonn.propack.core.builder.asset.SoundAssetBuilder;
import me.nelonn.propack.core.util.Sha1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LocalResourcePack implements ResourcePack {
    private final Project project;
    private final Map<Path, ItemModel> itemModels;
    private final Map<Path, SoundAsset> sounds;
    private final Map<Path, ArmorTexture> armorTextures;
    private final Map<Path, Font> fonts;
    private final MeshMapping meshMapping;
    private final File file;
    private final File zip;
    private final Sha1 sha1;
    private final UploadedPack uploadedPack;

    public LocalResourcePack(@NotNull Project project,
                             @NotNull Collection<ItemModelBuilder> itemModels,
                             @NotNull Collection<SoundAssetBuilder> soundAssets,
                             @NotNull Collection<ArmorTextureBuilder> armorTextures,
                             @NotNull Collection<FontBuilder> fonts,
                             @NotNull MeshMapping meshMapping,
                             @NotNull File file,
                             @NotNull File zip,
                             @NotNull Sha1 sha1,
                             @Nullable UploadedPack uploadedPack) {
        this.project = project;
        this.itemModels = new HashMap<>();
        for (ItemModelBuilder itemModel : itemModels) {
            this.itemModels.put(itemModel.getPath(), itemModel.build(this));
        }
        this.sounds = new HashMap<>();
        for (SoundAssetBuilder soundAsset : soundAssets) {
            this.sounds.put(soundAsset.getPath(), soundAsset.build(this));
        }
        this.armorTextures = new HashMap<>();
        for (ArmorTextureBuilder armorTexture : armorTextures) {
            this.armorTextures.put(armorTexture.getPath(), armorTexture.build(this));
        }
        this.fonts = new HashMap<>();
        for (FontBuilder font : fonts) {
            this.fonts.put(font.getPath(), font.build(this));
        }
        this.meshMapping = meshMapping;
        this.file = file;
        this.zip = zip;
        this.sha1 = sha1;
        this.uploadedPack = uploadedPack;
    }

    @Override
    public @NotNull String getName() {
        return project.getName();
    }

    @Override
    public @Nullable ItemModel getItemModel(@NotNull Path path) {
        return itemModels.get(path);
    }

    @Override
    public @NotNull Collection<ItemModel> getItemModels() {
        return itemModels.values();
    }

    @Override
    public @Nullable SoundAsset getSound(@NotNull Path path) {
        return sounds.get(path);
    }

    @Override
    public @NotNull Collection<SoundAsset> getSounds() {
        return sounds.values();
    }

    @Override
    public @Nullable ArmorTexture getArmorTexture(@NotNull Path path) {
        return armorTextures.get(path);
    }

    @Override
    public @NotNull Collection<ArmorTexture> getArmorTextures() {
        return armorTextures.values();
    }

    @Override
    public @Nullable Font getFont(@NotNull Path path) {
        return fonts.get(path);
    }

    @Override
    public @NotNull Collection<Font> getFonts() {
        return fonts.values();
    }

    @Override
    public @NotNull MeshMapping getMeshMapping() {
        return meshMapping;
    }

    public @NotNull Project getProject() {
        return project;
    }

    public @NotNull File getFile() {
        return file;
    }

    public @NotNull File getZip() {
        return zip;
    }

    public @NotNull Sha1 getSha1() {
        return sha1;
    }

    public @Nullable UploadedPack getUpload() {
        return uploadedPack;
    }

}

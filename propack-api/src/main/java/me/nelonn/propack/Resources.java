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

package me.nelonn.propack;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.ArmorTexture;
import me.nelonn.propack.asset.Font;
import me.nelonn.propack.asset.ItemModel;
import me.nelonn.propack.asset.SoundAsset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Resources {
    private final Map<Path, ItemModel> itemModels;
    private final Map<Path, SoundAsset> sounds;
    private final Map<Path, ArmorTexture> armorTextures;
    private final Map<Path, Font> fonts;
    private final Meshes meshes;

    public Resources(@NotNull Iterable<ItemModel> itemModels,
                     @NotNull Iterable<SoundAsset> sounds,
                     @NotNull Iterable<ArmorTexture> armorTextures,
                     @NotNull Iterable<Font> fonts,
                     @NotNull Meshes meshes) {
        this.itemModels = new HashMap<>();
        itemModels.forEach(asset -> this.itemModels.put(asset.friendlyPath(), asset));
        this.sounds = new HashMap<>();
        sounds.forEach(asset -> this.sounds.put(asset.friendlyPath(), asset));
        this.armorTextures = new HashMap<>();
        armorTextures.forEach(asset -> this.armorTextures.put(asset.friendlyPath(), asset));
        this.fonts = new HashMap<>();
        fonts.forEach(asset -> this.fonts.put(asset.friendlyPath(), asset));
        this.meshes = meshes;
    }

    public @Nullable ItemModel itemModel(@NotNull Path path) {
        return itemModels.get(path);
    }

    public @NotNull ItemModel itemModel$(@NotNull Path path) {
        return Objects.requireNonNull(itemModel(path));
    }

    public @NotNull Collection<ItemModel> getItemModels() {
        return Collections.unmodifiableCollection(itemModels.values());
    }

    public @Nullable SoundAsset sound(@NotNull Path path) {
        return sounds.get(path);
    }

    public @NotNull SoundAsset sound$(@NotNull Path path) {
        return Objects.requireNonNull(sound(path));
    }

    public @NotNull Collection<SoundAsset> getSounds() {
        return Collections.unmodifiableCollection(sounds.values());
    }

    public @Nullable ArmorTexture armorTexture(@NotNull Path path) {
        return armorTextures.get(path);
    }

    public @NotNull ArmorTexture armorTexture$(@NotNull Path path) {
        return Objects.requireNonNull(armorTexture(path));
    }

    public @NotNull Collection<ArmorTexture> getArmorTextures() {
        return Collections.unmodifiableCollection(armorTextures.values());
    }

    public @Nullable Font font(@NotNull Path path) {
        return fonts.get(path);
    }

    public @NotNull Font font$(@NotNull Path path) {
        return Objects.requireNonNull(font(path));
    }

    public @NotNull Collection<Font> getFonts() {
        return Collections.unmodifiableCollection(fonts.values());
    }

    public @NotNull Meshes getMeshes() {
        return meshes;
    }
}

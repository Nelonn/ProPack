package me.nelonn.propack.core.loader;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.MeshMapping;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.asset.ArmorTexture;
import me.nelonn.propack.asset.Font;
import me.nelonn.propack.asset.ItemModel;
import me.nelonn.propack.asset.SoundAsset;
import me.nelonn.propack.core.builder.asset.ArmorTextureBuilder;
import me.nelonn.propack.core.builder.asset.FontBuilder;
import me.nelonn.propack.core.builder.asset.ItemModelBuilder;
import me.nelonn.propack.core.builder.asset.SoundAssetBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoadedResourcePack implements ResourcePack {
    private final String name;
    private final Map<Path, ItemModel> itemModels;
    private final Map<Path, SoundAsset> sounds;
    private final Map<Path, ArmorTexture> armorTextures;
    private final Map<Path, Font> fonts;
    private final MeshMapping meshMapping;

    public LoadedResourcePack(@NotNull String name,
                              @NotNull Collection<ItemModelBuilder> itemModels,
                              @NotNull Collection<SoundAssetBuilder> soundAssets,
                              @NotNull Collection<ArmorTextureBuilder> armorTextures,
                              @NotNull Collection<FontBuilder> fonts,
                              @NotNull MeshMapping meshMapping) {
        this.name = name;
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
    }

    @Override
    public @NotNull String getName() {
        return name;
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

    @Override
    public @NotNull Optional<UploadedPack> getUpload() {
        return Optional.empty();
    }
}

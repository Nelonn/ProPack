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

package me.nelonn.propack.core.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nelonn.flint.path.Identifier;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.MeshesMap;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.core.builder.ResourcesCreator;
import me.nelonn.propack.core.builder.asset.*;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProPackFileLoader {
    public @NotNull Resources load(@NotNull File file) {
        try {
            return load(IOUtil.readString(file));
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading '" + file.getName() + "'", e);
        }
    }

    public @NotNull Resources load(@NotNull String content) {
        try {
            JsonObject root = GsonHelper.deserialize(content);
            int version = GsonHelper.getInt(root, "version");
            if (version != 1) {
                throw new UnsupportedOperationException("Version " + version + " not supported");
            }

            String name = GsonHelper.getString(root, "name");

            JsonObject resources = GsonHelper.getObject(root, "resources");

            JsonObject itemModelsObject = GsonHelper.getObject(resources, "item_models");
            Map<Path, ItemModelBuilder> itemModels = new HashMap<>();
            for (Map.Entry<String, JsonElement> itemModelElement : itemModelsObject.entrySet()) {
                Path path = Path.of(itemModelElement.getKey());
                JsonObject modelObject = GsonHelper.asObject(itemModelElement.getValue(), itemModelElement.getKey());
                String type = GsonHelper.getString(modelObject, "Type");
                if (type.equals("DefaultItemModel")) {
                    Path mesh = Path.of(GsonHelper.getString(modelObject, "Mesh"));
                    JsonArray targetArray = GsonHelper.getArray(modelObject, "Target");
                    Set<Identifier> targetItems = parseTargetItems(targetArray);
                    itemModels.put(path, new DefaultItemModelBuilder(path).setMesh(mesh).setTargetItems(targetItems));
                } else if (type.equals("CombinedItemModel")) {
                    Path mesh = Path.of(GsonHelper.getString(modelObject, "Mesh"));
                    JsonArray targetArray = GsonHelper.getArray(modelObject, "Target");
                    Set<Identifier> targetItems = parseTargetItems(targetArray);
                    JsonArray elementsArray = GsonHelper.getArray(modelObject, "Elements");
                    Set<String> elements = new HashSet<>();
                    Util.forEachStringArray(elementsArray, "Elements", elements::add);
                    itemModels.put(path, new CombinedItemModelBuilder(path)
                            .setMesh(mesh).setTargetItems(targetItems).setElements(elements));
                } else if (type.equals("SlotItemModel")) {
                    Path mesh = Path.of(GsonHelper.getString(modelObject, "Mesh"));
                    JsonArray targetArray = GsonHelper.getArray(modelObject, "Target");
                    Set<Identifier> targetItems = parseTargetItems(targetArray);
                    JsonObject slotsObject = GsonHelper.getObject(modelObject, "Slots");
                    Map<String, SlotItemModel.Slot> slots = new HashMap<>();
                    for (Map.Entry<String, JsonElement> slotEntry : slotsObject.entrySet()) {
                        Set<String> slotEntries = new HashSet<>();
                        JsonArray jsonArray = GsonHelper.asArray(slotEntry.getValue(), slotEntry.getKey());
                        Util.forEachStringArray(jsonArray, slotEntry.getKey(), slotEntries::add);
                        SlotItemModel.Slot slot = new SlotItemModel.Slot(slotEntry.getKey(), slotEntries);
                        slots.put(slot.getName(), slot);
                    }
                    itemModels.put(path, new SlotItemModelBuilder(path).setMesh(mesh).setSlots(slots).setTargetItems(targetItems));
                }
            }

            JsonObject soundsObject = GsonHelper.getObject(resources, "sounds");
            Map<Path, SoundAssetBuilder> sounds = new HashMap<>();
            for (Map.Entry<String, JsonElement> soundElement : soundsObject.entrySet()) {
                Path path = Path.of(soundElement.getKey());
                Path soundPath = Path.of(GsonHelper.asString(soundElement.getValue(), soundElement.getKey()));
                sounds.put(path, new SoundAssetBuilder(path).setSoundPath(soundPath));
            }

            JsonObject armorTexturesObject = GsonHelper.getObject(resources, "armor_textures");
            Map<Path, ArmorTextureBuilder> armorTextures = new HashMap<>();
            for (Map.Entry<String, JsonElement> armorTextureEntry : armorTexturesObject.entrySet()) {
                Path path = Path.of(armorTextureEntry.getKey());
                JsonObject armorTextureObject = GsonHelper.asObject(armorTextureEntry.getValue(), armorTextureEntry.getKey());
                Color color = Util.hexToRGB(GsonHelper.getString(armorTextureObject, "Color"));
                boolean hasLayer1 = GsonHelper.getBoolean(armorTextureObject, "Layer1");
                boolean hasLayer2 = GsonHelper.getBoolean(armorTextureObject, "Layer2");
                armorTextures.put(path, new ArmorTextureBuilder(path)
                        .setColor(color).setHasLayer1(hasLayer1).setHasLayer2(hasLayer2));
            }

            JsonObject fontsObject = GsonHelper.getObject(resources, "fonts");
            Map<Path, FontBuilder> fonts = new HashMap<>();
            for (Map.Entry<String, JsonElement> fontEntry : fontsObject.entrySet()) {
                Path path = Path.of(fontEntry.getKey());
                Path fontPath = Path.of(GsonHelper.asString(fontEntry.getValue(), fontEntry.getKey()));
                fonts.put(path, new FontBuilder(path).setFontPath(fontPath));
            }

            JsonObject meshMappingObject = GsonHelper.getObject(resources, "mesh_mapping");
            Map<Identifier, Map<Path, Integer>> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : meshMappingObject.entrySet()) {
                Identifier itemId = Identifier.of(entry.getKey());
                JsonObject jsonObject = GsonHelper.asObject(entry.getValue(), entry.getKey());
                Map<Path, Integer> meshMap = new HashMap<>();
                for (Map.Entry<String, JsonElement> meshEntry : jsonObject.entrySet()) {
                    int cmd = GsonHelper.asInt(meshEntry.getValue(), meshEntry.getKey());
                    meshMap.put(Path.of(meshEntry.getKey()), cmd);
                }
                map.put(itemId, meshMap);
            }
            MeshesMap meshMapping = new MeshesMap(map);

            return ResourcesCreator.create(
                    itemModels.values(),
                    sounds.values(),
                    armorTextures.values(),
                    fonts.values(),
                    meshMapping
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading resource pack", e);
        }
    }

    private Set<Identifier> parseTargetItems(JsonArray jsonArray) {
        HashSet<Identifier> output = new HashSet<>();
        Util.forEachStringArray(jsonArray, "Target", s -> {
            output.add(Identifier.of(s));
        });
        return output;
    }
}

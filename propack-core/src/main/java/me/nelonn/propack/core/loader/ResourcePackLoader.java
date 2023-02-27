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

package me.nelonn.propack.core.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nelonn.flint.path.Identifier;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.MapItemDefinition;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.core.builder.asset.*;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.Util;
import me.nelonn.propack.definition.Item;
import me.nelonn.propack.definition.ItemDefinition;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourcePackLoader {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    public @NotNull ResourcePack load(@NotNull File file) {
        try {
            JsonObject root = GsonHelper.deserialize(IOUtil.readString(file));
            int version = GsonHelper.getInt(root, "version");
            if (version != 1) {
                throw new UnsupportedOperationException("Version " + version + " not supported");
            }

            JsonObject itemsObject = GsonHelper.getObject(root, "items");
            Map<Identifier, Item> items = new HashMap<>();
            for (Map.Entry<String, JsonElement> itemElement : itemsObject.entrySet()) {
                boolean isBlock = GsonHelper.asBoolean(itemElement.getValue(), itemElement.getKey());
                Item item = new Item(Identifier.of(itemElement.getKey()), isBlock);
                items.put(item.getId(), item);
            }
            MapItemDefinition itemDefinition = new MapItemDefinition(items);

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
                    Set<Item> targetItems = parseTargetItems(targetArray, itemDefinition);
                    itemModels.put(path, new DefaultItemModelBuilder(path).setMesh(mesh).setTargetItems(targetItems));
                } else if (type.equals("CombinedItemModel")) {
                    Path mesh = Path.of(GsonHelper.getString(modelObject, "Mesh"));
                    JsonArray targetArray = GsonHelper.getArray(modelObject, "Target");
                    Set<Item> targetItems = parseTargetItems(targetArray, itemDefinition);
                    JsonArray elementsArray = GsonHelper.getArray(modelObject, "Elements");
                    Set<String> elements = new HashSet<>();
                    Util.forEachStringArray(elementsArray, "Elements", elements::add);
                    itemModels.put(path, new CombinedItemModelBuilder(path)
                            .setMesh(mesh).setTargetItems(targetItems).setElements(elements));
                } else if (type.equals("SlotItemModel")) {
                    Path mesh = Path.of(GsonHelper.getString(modelObject, "Mesh"));
                    JsonArray targetArray = GsonHelper.getArray(modelObject, "Target");
                    Set<Item> targetItems = parseTargetItems(targetArray, itemDefinition);
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
            // TODO: deserialization

            JsonObject fontsObject = GsonHelper.getObject(resources, "fonts");
            // TODO: deserialization

            JsonObject meshMappingObject = GsonHelper.getObject(resources, "mesh_mapping");
            // TODO: deserialization

            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when loading '" + file.getName() + "'", e);
        }
    }

    private Set<Item> parseTargetItems(JsonArray jsonArray, ItemDefinition itemDefinition) {
        HashSet<Item> output = new HashSet<>();
        Util.forEachStringArray(jsonArray, "Target", s -> {
            output.add(itemDefinition.getItem(Identifier.of(s)));
        });
        return output;
    }
}

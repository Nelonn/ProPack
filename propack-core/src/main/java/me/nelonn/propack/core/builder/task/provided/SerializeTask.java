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

package me.nelonn.propack.core.builder.task.provided;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.core.builder.MeshMappingBuilder;
import me.nelonn.propack.core.builder.asset.*;
import me.nelonn.propack.core.builder.task.AbstractTask;
import me.nelonn.propack.core.builder.task.TaskBootstrap;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.definition.Item;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class SerializeTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = GatherSourcesTask::new;
    public static final Extra<File> EXTRA_FILE = new Extra<>(File.class, "propack.serialize.file");

    public SerializeTask(@NotNull Project project) {
        super("serialize", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        File buildDir = getProject().getBuildDir();
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);

        root.addProperty("name", getProject().getName());

        JsonObject items = new JsonObject();
        root.add("items", items);

        for (Item item : getProject().getItemDefinition().getItems()) {
            items.addProperty(item.getId().toString(), item.isBlock());
        }

        JsonObject resources = new JsonObject();
        root.add("resources", resources);

        JsonObject itemModels = new JsonObject();
        for (ItemModelBuilder itemModel : io.getAssets().getItemModels()) {
            JsonObject itemModelObject = new JsonObject();
            if (itemModel instanceof DefaultItemModelBuilder) {
                DefaultItemModelBuilder defaultModel = (DefaultItemModelBuilder) itemModel;
                itemModelObject.addProperty("Type", "DefaultItemModel");
                itemModelObject.addProperty("Mesh", defaultModel.getMesh().toString());
                JsonArray targetItems = new JsonArray();
                for (Item targetItem : defaultModel.getTargetItems()) {
                    targetItems.add(targetItem.getId().toString());
                }
                itemModelObject.add("Target", targetItems);
            } else if (itemModel instanceof CombinedItemModelBuilder) {
                CombinedItemModelBuilder combinedModel = (CombinedItemModelBuilder) itemModel;
                itemModelObject.addProperty("Type", "CombinedItemModel");
                itemModelObject.addProperty("Mesh", combinedModel.getMesh().toString());
                JsonArray elementsArray = new JsonArray(combinedModel.getElements().size());
                combinedModel.getElements().forEach(elementsArray::add);
                itemModelObject.add("Elements", elementsArray);
                JsonArray targetItems = new JsonArray();
                for (Item targetItem : combinedModel.getTargetItems()) {
                    targetItems.add(targetItem.getId().toString());
                }
                itemModelObject.add("Target", targetItems);
            } else if (itemModel instanceof SlotItemModelBuilder) {
                SlotItemModelBuilder slotModel = (SlotItemModelBuilder) itemModel;
                itemModelObject.addProperty("Type", "SlotItemModel");
                itemModelObject.addProperty("Mesh", slotModel.getMesh().toString());
                JsonObject slotsObject = new JsonObject();
                itemModelObject.add("Slots", slotsObject);
                for (SlotItemModel.Slot slot : slotModel.getSlots().values()) {
                    JsonArray slotEntries = new JsonArray(slot.getEntries().size());
                    slot.getEntries().forEach(slotEntries::add);
                    slotsObject.add(slot.getName(), slotEntries);
                }
                JsonArray targetItems = new JsonArray();
                for (Item targetItem : slotModel.getTargetItems()) {
                    targetItems.add(targetItem.getId().toString());
                }
                itemModelObject.add("Target", targetItems);
            }
            itemModels.add(itemModel.getPath().toString(), itemModelObject);
        }
        resources.add("item_models", itemModels);

        JsonObject sounds = new JsonObject();
        for (SoundAssetBuilder soundAsset : io.getAssets().getSounds()) {
            sounds.addProperty(soundAsset.getPath().toString(), soundAsset.getSoundPath().toString());
        }
        resources.add("sounds", sounds);

        JsonObject armorTextures = new JsonObject();
        for (ArmorTextureBuilder armorTexture : io.getAssets().getArmorTextures()) {
            JsonObject armorTextureObject = new JsonObject();
            Color color = armorTexture.getColor();
            String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
            armorTextureObject.addProperty("Color", hex);
            armorTextureObject.addProperty("Layer1", armorTexture.hasLayer1());
            armorTextureObject.addProperty("Layer2", armorTexture.hasLayer2());
            armorTextures.add(armorTexture.getPath().toString(), armorTextureObject);
        }
        resources.add("armor_textures", armorTextures);

        JsonObject fonts = new JsonObject();
        for (FontBuilder font : io.getAssets().getFonts()) {
            fonts.addProperty(font.getPath().toString(), font.getFontPath().toString());
        }
        resources.add("fonts", fonts);

        MeshMappingBuilder meshMappingBuilder = io.getExtras().get(ProcessModelsTask.EXTRA_MESH_MAPPING_BUILDER);
        JsonObject meshMappingObject = new JsonObject();
        for (MeshMappingBuilder.ItemEntry itemEntry : meshMappingBuilder.getMappers()) {
            JsonObject mapping = new JsonObject();
            for (Map.Entry<Integer, Path> entry : itemEntry.getMap().entrySet()) {
                mapping.addProperty(entry.getValue().toString(), entry.getKey());
            }
            meshMappingObject.add(itemEntry.getItem().getId().toString(), mapping);
        }
        resources.add("mesh_mapping", meshMappingObject);

        File outputFile = new File(buildDir, getProject().getName() + ".propack");
        if (outputFile.exists()) {
            try {
                Files.delete(outputFile.toPath());
            } catch (Exception e) {
                LOGGER.error("Unable to delete " + outputFile, e);
            }
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            fileOutputStream.write(root.toString().getBytes(StandardCharsets.UTF_8));
            io.getExtras().put(EXTRA_FILE, outputFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.core.builder.Mapper;
import me.nelonn.propack.core.builder.MappingsBuilder;
import me.nelonn.propack.core.builder.asset.*;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.Sha1;
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
        Sha1 sha1 = io.getExtras().get(PackageTask.EXTRA_SHA1);
        root.addProperty("sha1", sha1.toString());

        MappingsBuilder mappingsBuilder = io.getExtras().get(ProcessModelsTask.EXTRA_MAPPINGS_BUILDER);
        JsonObject mappingsObject = new JsonObject();
        for (Mapper mapper : mappingsBuilder.getMappers()) {
            JsonObject mapping = new JsonObject();
            for (Map.Entry<Integer, Path> entry : mapper.entrySet()) {
                mapping.addProperty(entry.getValue().toString(), entry.getKey());
            }
            mappingsObject.add(mapper.getItem().getId().toString(), mapping);
        }
        root.add("mappings", mappingsObject);

        JsonObject itemModels = new JsonObject();
        for (ItemModelBuilder itemModel : io.getAssets().getItemModels()) {
            JsonObject itemModelObject = new JsonObject();
            if (itemModel instanceof DefaultItemModelBuilder) {
                DefaultItemModelBuilder defaultModel = (DefaultItemModelBuilder) itemModel;
                itemModelObject.addProperty("Type", "DefaultItemModel");
                JsonArray targetItems = new JsonArray();
                for (Item targetItem : defaultModel.getTargetItems()) {
                    targetItems.add(targetItem.getId().toString());
                }
                itemModelObject.add("Target", targetItems);
                itemModelObject.addProperty("Mesh", defaultModel.getMesh().toString());
            }
            itemModels.add(itemModel.getPath().toString(), itemModelObject);
        }
        root.add("item_models", itemModels);

        JsonObject sounds = new JsonObject();
        for (SoundAssetBuilder soundAsset : io.getAssets().getSounds()) {
            sounds.addProperty(soundAsset.getPath().toString(), soundAsset.getSoundPath().toString());
        }
        root.add("sounds", sounds);

        JsonObject armorTextures = new JsonObject();
        for (ArmorTextureBuilder armorTexture : io.getAssets().getArmorTextures()) {
            Color color = armorTexture.getColor();
            String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
            armorTextures.addProperty(armorTexture.getPath().toString(), hex);
        }
        root.add("armor_textures", armorTextures);

        JsonObject fonts = new JsonObject();
        for (FontBuilder font : io.getAssets().getFonts()) {
            fonts.addProperty(font.getPath().toString(), font.getFontPath().toString());
        }
        root.add("fonts", fonts);

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

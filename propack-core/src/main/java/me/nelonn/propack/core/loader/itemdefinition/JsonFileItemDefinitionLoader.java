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

package me.nelonn.propack.core.loader.itemdefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.MapItemDefinition;
import me.nelonn.propack.builder.loader.ItemDefinitionLoader;
import me.nelonn.propack.builder.util.Extras;
import me.nelonn.propack.core.loader.ProjectLoader;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.definition.Item;
import me.nelonn.propack.definition.ItemDefinition;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JsonFileItemDefinitionLoader implements ItemDefinitionLoader {
    public static final JsonFileItemDefinitionLoader INSTANCE = new JsonFileItemDefinitionLoader();

    private JsonFileItemDefinitionLoader() {
    }

    @Override
    public @NotNull String getType() {
        return "JsonFile";
    }

    @Override
    public @NotNull ItemDefinition load(@NotNull JsonObject rootObject, @NotNull Extras extras) throws Exception {
        boolean lenient = GsonHelper.getBoolean(rootObject, "Lenient", true);
        String filePath = GsonHelper.getString(rootObject, "File");
        File file = new File(extras.get(ProjectLoader.EXTRA_CONFIG_DIR), filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File '" + file.getName() + "' not found");
        } else if (file.isDirectory()) {
            throw new IllegalArgumentException("File '" + file.getName() + "' is directory");
        }
        String itemDefinitionFileContent = IOUtil.readString(file);
        JsonObject itemDefinitionFileObject = GsonHelper.deserialize(itemDefinitionFileContent, lenient);
        JsonArray itemsArray = GsonHelper.getArray(itemDefinitionFileObject, "item");
        JsonArray blocksArray = GsonHelper.getArray(itemDefinitionFileObject, "block");
        Map<Identifier, Item> itemMap = new HashMap<>();
        for (int i = 0; i < itemsArray.size(); i++) {
            try {
                Identifier identifier = Identifier.of(itemsArray.get(i).getAsString());
                itemMap.put(identifier, new Item(identifier, false));
            } catch (Exception e) {
                throw new JsonSyntaxException("Element in array 'item' at index " + i + " is not a string");
            }
        }
        for (int i = 0; i < blocksArray.size(); i++) {
            try {
                Identifier identifier = Identifier.of(blocksArray.get(i).getAsString());
                itemMap.put(identifier, new Item(identifier, true));
            } catch (Exception e) {
                throw new JsonSyntaxException("Element in array 'block' at index " + i + " is not a string");
            }
        }
        return new MapItemDefinition(itemMap);
    }
}

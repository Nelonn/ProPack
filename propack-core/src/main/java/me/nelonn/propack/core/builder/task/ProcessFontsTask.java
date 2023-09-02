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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.file.JsonFile;
import me.nelonn.propack.builder.task.AbstractTask;
import me.nelonn.propack.builder.task.FileProcessingException;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.core.builder.asset.FontBuilder;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ProcessFontsTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = ProcessFontsTask::new;

    public ProcessFontsTask(@NotNull Project project) {
        super("processFonts", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".font.json") || !(file instanceof JsonFile)) continue;
                io.getFiles().removeFile(filePath);
                JsonObject jsonObject = ((JsonFile) file).getContent().deepCopy();
                Path resourcePath = PathUtil.resourcePath(filePath, ".font.json");
                io.getAssets().putFont(new FontBuilder(resourcePath).setFontPath(resourcePath));
                JsonArray providersArray = jsonObject.getAsJsonArray("providers");
                if (providersArray != null) {
                    for (JsonElement jsonElement : providersArray) {
                        JsonObject providerObject = jsonElement.getAsJsonObject();
                        if (GsonHelper.hasString(providerObject, "type")) {
                            String type = GsonHelper.getString(providerObject, "type");
                            if (type.equalsIgnoreCase("bitmap")) {
                                int ascent = GsonHelper.getInt(providerObject, "ascent");
                                int height = GsonHelper.getInt(providerObject, "height", 0);
                                if (ascent > height) {
                                    LOGGER.warn("Ascent {} higher than height {}", ascent, height);
                                }
                            }
                        }
                        if (GsonHelper.hasString(providerObject, "file")) {
                            String path = GsonHelper.getString(providerObject, "file");
                            path = PathUtil.resolve(path, resourcePath).toString();
                            providerObject.addProperty("file", path);
                        }
                    }
                }
                String fontPath = PathUtil.assetsPath(resourcePath, "font") + ".json";
                io.getFiles().addFile(new JsonFile(fontPath, jsonObject));
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
    }
}

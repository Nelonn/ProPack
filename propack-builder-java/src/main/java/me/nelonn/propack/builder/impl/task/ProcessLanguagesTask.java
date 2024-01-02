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

package me.nelonn.propack.builder.impl.task;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.api.Project;
import me.nelonn.propack.builder.api.file.File;
import me.nelonn.propack.builder.api.file.JsonFile;
import me.nelonn.propack.builder.api.task.TaskIO;
import me.nelonn.propack.builder.api.task.AbstractTask;
import me.nelonn.propack.builder.api.task.FileProcessingException;
import me.nelonn.propack.builder.api.task.TaskBootstrap;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ProcessLanguagesTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = ProcessLanguagesTask::new;

    public ProcessLanguagesTask(@NotNull Project project) {
        super("processLanguages", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        Map<String, JsonFile> languageFiles = new HashMap<>();
        Map<String, String> allLangTranslations = getProject().getBuildConfiguration().getAllLangTranslations();
        if (!allLangTranslations.isEmpty()) {
            for (String lang : getProject().getBuildConfiguration().getLanguages()) {
                JsonObject translationsObject = new JsonObject();
                for (Map.Entry<String, String> translations : allLangTranslations.entrySet()) {
                    translationsObject.addProperty(translations.getKey(), translations.getValue());
                }
                String path = "assets/minecraft/lang/" + lang + ".json";
                languageFiles.put(path, new JsonFile(path, translationsObject));
            }
        }
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".lang.json")) continue;
                io.getFiles().removeFile(filePath);
                if (!(file instanceof JsonFile)) {
                    LOGGER.error("{} :: lang file is not Json", filePath);
                    continue;
                }
                JsonObject json = ((JsonFile) file).getContent();
                Path resourcePath = PathUtil.resourcePath(filePath);
                String langCode = resourcePath.value();
                langCode = langCode.substring(langCode.indexOf('/') + 1, langCode.length() - ".font.json".length());
                String langPath = "assets/" + resourcePath.namespace() + "/lang/" + langCode + ".json";
                JsonFile langJsonFile = languageFiles.computeIfAbsent(langPath, key -> new JsonFile(key, new JsonObject()));
                for (String key : json.keySet()) {
                    String translation = GsonHelper.getString(json, key);
                    key = key.replace("<namespace>", resourcePath.namespace());
                    langJsonFile.getContent().addProperty(key, translation);
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (JsonFile languageFile : languageFiles.values()) {
            io.getFiles().addFile(languageFile);
        }
    }
}

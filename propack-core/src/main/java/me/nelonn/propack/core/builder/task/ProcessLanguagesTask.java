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

import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.file.JsonFile;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.PathUtil;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ProcessLanguagesTask extends AbstractTask {
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
                if (!filePath.startsWith("content/") || !filePath.endsWith(".lang.json") || !(file instanceof JsonFile)) continue;
                io.getFiles().removeFile(filePath);
                JsonObject jsonObject = ((JsonFile) file).getContent();
                Path resourcePath = PathUtil.resourcePath(filePath);
                String langCode = resourcePath.getValue();
                langCode = langCode.substring(langCode.indexOf('/') + 1, langCode.length() - ".font.json".length());
                final String langPath = PathUtil.assetsPath(resourcePath, "lang") + ".json";
                JsonFile langJsonFile = Util.getOrPut(languageFiles, langPath, () -> new JsonFile(langPath, new JsonObject()));
                for (String key : jsonObject.keySet()) {
                    String translation = GsonHelper.getString(jsonObject, key);
                    key = key.replace("<namespace>", resourcePath.getNamespace());
                    translation = translation.replace("<namespace>", resourcePath.getNamespace());
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

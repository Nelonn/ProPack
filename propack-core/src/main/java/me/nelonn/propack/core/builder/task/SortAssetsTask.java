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

import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.task.AbstractTask;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.core.util.PathUtil;
import org.jetbrains.annotations.NotNull;

public class SortAssetsTask extends AbstractTask {
    public static final TaskBootstrap BOOTSTRAP = SortAssetsTask::new;

    public SortAssetsTask(@NotNull Project project) {
        super("sortAssets", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        for (File file : io.getFiles().copy()) {
            String filePath = file.getPath();
            if (filePath.startsWith("assets/") ||
                    filePath.equalsIgnoreCase("pack.mcmeta") ||
                    filePath.equalsIgnoreCase("pack.png")) continue;
            io.getFiles().removeFile(filePath);
            if (filePath.startsWith("include/")) {
                io.getFiles().addFile(file.copyAs(filePath.substring("include/".length())));
            } else if (filePath.startsWith("content/")) {
                filePath = filePath.substring("content/".length());
                String[] pathSplit = filePath.split("/", 2);
                if (pathSplit.length == 1) {
                    throw new IllegalArgumentException("Invalid path " + file.getPath());
                }
                String assetsNamespace = "assets/" + pathSplit[0];
                filePath = pathSplit[1];
                if (filePath.endsWith(".png") || filePath.endsWith(".png.mcmeta")) {
                    io.getFiles().addFile(file.copyAs(PathUtil.join(assetsNamespace, "textures", filePath)));
                } else if (filePath.endsWith(".ogg")) {
                    io.getFiles().addFile(file.copyAs(PathUtil.join(assetsNamespace, "sounds", filePath)));
                } else if (filePath.endsWith(".ttf") || filePath.endsWith(".bin")) {
                    io.getFiles().addFile(file.copyAs(PathUtil.join(assetsNamespace, "font", filePath)));
                }
            }
        }
    }
}

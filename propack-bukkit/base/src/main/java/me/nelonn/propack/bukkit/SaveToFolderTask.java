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

package me.nelonn.propack.bukkit;

import me.nelonn.propack.builder.api.Project;
import me.nelonn.propack.builder.api.task.AbstractTask;
import me.nelonn.propack.builder.api.task.TaskIO;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveToFolderTask extends AbstractTask {
    public Path destDir = Paths.get("undefined");

    public SaveToFolderTask(@NotNull Project project) {
        super("copyToIA", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        try {
            RmRf.deleteRecursively(destDir);
            Files.createDirectories(destDir);
            for (me.nelonn.propack.builder.api.file.File file : io.getFiles()) {
                Path filePath = destDir.resolve(file.getPath());
                Files.createDirectories(filePath.getParent());
                try (OutputStream outputStream = Files.newOutputStream(filePath);
                     InputStream inputStream = file.openInputStream()) {
                    final byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, read);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

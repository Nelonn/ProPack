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

import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.flint.path.PathException;
import me.nelonn.flint.path.PathImpl;
import me.nelonn.propack.builder.api.Project;
import me.nelonn.propack.builder.api.StrictMode;
import me.nelonn.propack.builder.api.file.JsonFile;
import me.nelonn.propack.builder.api.file.RealFile;
import me.nelonn.propack.builder.api.file.TextFile;
import me.nelonn.propack.builder.api.task.AbstractTask;
import me.nelonn.propack.builder.api.task.TaskBootstrap;
import me.nelonn.propack.builder.api.task.TaskIO;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.regex.Pattern;

public class GatherSourcesTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = GatherSourcesTask::new;

    public GatherSourcesTask(@NotNull Project project) {
        super("gatherSources", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        addDir(io, "include", new File(getProject().getProjectDir(), "include"));
        addDir(io, "content", new File(getProject().getProjectDir(), "content"));
        io.getFiles().addFile(getProject().getMetaFile());
        io.getFiles().addFile(getProject().getIconFile());
    }

    public void addDir(@NotNull TaskIO io, @NotNull String to, @NotNull File directory) {
        if (Path.check(to, Path::isAllowedInPathValue).isPresent()) {
            throw new IllegalArgumentException("Non [a-z0-9/._-] character in path '" + to + "'");
        }
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            String fileName = file.getName().toLowerCase();
            @Nullable Pattern pattern = file.isDirectory() ? getProject().getBuildConfiguration().getDirIgnore() :
                    getProject().getBuildConfiguration().getFileIgnore();
            if (pattern != null && pattern.matcher(fileName).matches()) continue;
            if (Path.checkNamespace(fileName).isPresent()) {
                String message = to + '/' + fileName + ": Non [a-z0-9._-] character in file name";
                if (getProject().getBuildConfiguration().getStrictMode() == StrictMode.ENABLED) {
                    throw new IllegalArgumentException(message);
                } else if (getProject().getBuildConfiguration().getStrictMode() == StrictMode.WARN) {
                    LOGGER.warn(message);
                }
                continue;
            }
            if (file.isDirectory()) {
                addDir(io, PathUtil.join(to, fileName), file);
            } else {
                addFile(io, to, file);
            }
        }
    }

    private void addFile(@NotNull TaskIO io, @NotNull String to, @NotNull File file) {
        String fileName = file.getName().toLowerCase();
        if (Path.check(to, Path::isAllowedInPathValue).isPresent()) {
            throw new IllegalArgumentException("Invalid directory '" + to + "'");
        }
        if (Path.checkNamespace(fileName).isPresent()) {
            throw new IllegalArgumentException("Invalid file name '" + fileName + "'");
        }
        Pattern pattern = getProject().getBuildConfiguration().getFileIgnore();
        if (pattern != null && pattern.matcher(fileName).matches()) return;
        String path = PathUtil.join(to, fileName);
        try {
            if (fileName.endsWith(".json") || fileName.endsWith(".json5") || fileName.endsWith(".jsonc")) {
                JsonObject content = GsonHelper.deserialize(IOUtil.readString(file), true);
                io.getFiles().addFile(new JsonFile(path, content));
            } else if (fileName.endsWith(".mcmeta") || fileName.endsWith(".fsh") || fileName.endsWith(".vsh")) {
                io.getFiles().addFile(new TextFile(path, IOUtil.readString(file)));
            } else {
                io.getFiles().addFile(new RealFile(path, file));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Something went wrong when adding '" + fileName + "'", e);
        }
    }
}

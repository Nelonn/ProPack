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

import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.task.FileCollection;
import me.nelonn.propack.core.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentFileCollection implements FileCollection {
    private final Map<String, File> files;

    public ConcurrentFileCollection() {
        files = new ConcurrentHashMap<>();
    }

    @Override
    public void addFile(@NotNull File file, boolean override) {
        if (!override && contains(file.getPath())) {
            throw new IllegalArgumentException("File '" + file.getPath() + "' already exists");
        }
        files.put(file.getPath(), file);
    }

    @Override
    public @Nullable File getFile(@NotNull String path) {
        return files.get(PathUtil.format(path));
    }

    @Override
    public @Nullable File removeFile(@NotNull String path) {
        return files.remove(PathUtil.format(path));
    }

    @Override
    public @NotNull Collection<File> getFiles() {
        return files.values();
    }

    @Override
    public boolean contains(@NotNull String path) {
        return files.containsKey(PathUtil.format(path));
    }

    @Override
    public boolean isEmpty() {
        return files.isEmpty();
    }

    @Override
    public void clear() {
        files.clear();
    }

    @Override
    public @NotNull ConcurrentFileCollection copy() {
        ConcurrentFileCollection n = new ConcurrentFileCollection();
        for (File file : this) {
            n.addFile(file);
        }
        return n;
    }

    @Override
    public @NotNull Iterator<File> iterator() {
        return files.values().iterator();
    }

}

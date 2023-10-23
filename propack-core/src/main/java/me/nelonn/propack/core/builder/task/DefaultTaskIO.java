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

package me.nelonn.propack.core.builder.task;

import me.nelonn.propack.builder.task.AssetCollection;
import me.nelonn.propack.builder.task.FileCollection;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extras;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DefaultTaskIO implements TaskIO {
    private final ConcurrentFileCollection fileCollection;
    private final ConcurrentAssetCollection assetCollection;
    private final Extras extras;
    private final File tempDirectory;

    public DefaultTaskIO(@NotNull File tempDirectory) {
        this.fileCollection = new ConcurrentFileCollection();
        this.assetCollection = new ConcurrentAssetCollection();
        this.extras = new Extras();
        this.tempDirectory = tempDirectory;
    }

    @Override
    public @NotNull FileCollection getFiles() {
        return fileCollection;
    }

    @Override
    public @NotNull AssetCollection getAssets() {
        return assetCollection;
    }

    @Override
    public @NotNull Extras getExtras() {
        return extras;
    }

    @Override
    public @NotNull File getTempDirectory() {
        return tempDirectory;
    }
}

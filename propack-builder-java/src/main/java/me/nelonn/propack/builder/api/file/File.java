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

package me.nelonn.propack.builder.api.file;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public interface File extends Comparable<File> {

    /**
     * @return file path
     */
    @NotNull String getPath();

    /**
     * Close it after use
     * @return file input stream
     * @throws IOException IOException
     */
    @NotNull InputStream openInputStream() throws IOException;

    default int compareTo(@NotNull File other) {
        return other.getPath().compareTo(this.getPath());
    }

    @NotNull File copyAs(@NotNull String path);
}

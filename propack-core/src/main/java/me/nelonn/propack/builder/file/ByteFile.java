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

package me.nelonn.propack.builder.file;

import me.nelonn.propack.core.util.IOUtil;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ByteFile extends AbstractFile implements VirtualFile {
    private final byte[] content;

    public ByteFile(@NotNull String path, byte @NotNull [] content) {
        super(path);
        this.content = content;
    }

    public ByteFile(@NotNull String path, @NotNull InputStream inputStream) throws IOException {
        this(path, IOUtil.readAllBytes(inputStream));
    }

    @Override
    public @NotNull InputStream openInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public @NotNull ByteFile copyAs(@NotNull String path) {
        return new ByteFile(path, content);
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(content, content.length);
    }

}

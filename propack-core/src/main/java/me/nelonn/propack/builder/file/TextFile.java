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

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextFile extends AbstractFile implements MutableFile<String> {
    private String content;
    private final Charset charset;

    public TextFile(@NotNull String path, @NotNull String content, @NotNull Charset charset) {
        super(path);
        this.content = content;
        this.charset = charset;
    }

    public TextFile(@NotNull String path, @NotNull String content) {
        this(path, content, StandardCharsets.UTF_8);
    }

    @Override
    public @NotNull TextFile copyAs(@NotNull String path) {
        return new TextFile(path, content, charset);
    }

    @Override
    public @NotNull TextFile copy() {
        return copyAs(getPath());
    }

    @Override
    public byte[] getBytes() {
        return content.getBytes(charset);
    }

    @Override
    public @NotNull String getContent() {
        return content;
    }

    @Override
    public void setContent(@NotNull String content) {
        this.content = content;
    }
}

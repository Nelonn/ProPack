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

package me.nelonn.propack.builder.file;

import com.google.gson.JsonObject;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonFile extends AbstractFile implements MutableFile<JsonObject> {
    private JsonObject content;
    private final Charset charset;

    public JsonFile(@NotNull String path, @NotNull JsonObject content, @NotNull Charset charset) {
        super(path.endsWith(".json5") || path.endsWith(".jsonc") ? Util.substringLast(path, 1) : path);
        this.content = content;
        this.charset = charset;
    }

    public JsonFile(@NotNull String path, @NotNull JsonObject content) {
        this(path, content, StandardCharsets.UTF_8);
    }

    @Override
    public @NotNull JsonFile copyAs(@NotNull String path) {
        return new JsonFile(path, content.deepCopy());
    }

    @Override
    public @NotNull JsonFile copy() {
        return copyAs(getPath());
    }

    @Override
    public byte[] getBytes() {
        return content.toString().getBytes(charset);
    }

    @Override
    public @NotNull JsonObject getContent() {
        return content;
    }

    @Override
    public void setContent(@NotNull JsonObject content) {
        this.content = content;
    }
}

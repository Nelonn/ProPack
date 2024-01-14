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

package me.nelonn.propack.core;

import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.Sha1;
import org.jetbrains.annotations.NotNull;

public class UploadedPackImpl implements UploadedPack {
    private final String name;
    private final String url;
    private final byte [] sha1Bytes;
    private final String sha1String;

    public UploadedPackImpl(@NotNull String name, @NotNull String url, byte @NotNull [] sha1Bytes, @NotNull String sha1String) {
        this.name = name;
        this.url = url;
        this.sha1Bytes = sha1Bytes;
        this.sha1String = sha1String;
    }

    public UploadedPackImpl(@NotNull String name, @NotNull String url, @NotNull Sha1 sha1) {
        this(name, url, sha1.asBytes(), sha1.asString());
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getUrl() {
        return url;
    }

    @Override
    public byte @NotNull [] getSha1Bytes() {
        return sha1Bytes;
    }

    @Override
    public @NotNull String getSha1String() {
        return sha1String;
    }

    @Override
    public String toString() {
        return "UploadedPack{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", sha1String='" + sha1String + '\'' +
                '}';
    }
}

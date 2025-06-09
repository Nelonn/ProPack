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

import me.nelonn.propack.Sha1;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.builder.api.hosting.Hosting;
import me.nelonn.propack.core.UploadedPackImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

public class StaticHosting extends Hosting {
    private String packUrl;

    public String getPackUrl() {
        return packUrl;
    }

    public void setPackUrl(String packUrl) {
        this.packUrl = packUrl;
    }

    @Override
    public @Nullable UploadedPack upload(@NotNull File file, @NotNull Sha1 sha1, @NotNull String name, @Nullable Map<String, Object> options) {
        return packUrl != null ? new UploadedPackImpl(name, packUrl, sha1) : null;
    }
}

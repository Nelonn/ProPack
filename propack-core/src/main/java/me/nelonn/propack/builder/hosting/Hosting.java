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

package me.nelonn.propack.builder.hosting;

import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.Sha1;
import me.nelonn.propack.UploadedPack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class Hosting {
    private HostingMap hostingMap;
    private Identifier id;

    public abstract @NotNull UploadedPack upload(@NotNull File file, @NotNull Sha1 sha1, @NotNull String name, @Nullable Map<String, Object> options) throws IOException;

    public final boolean register(@NotNull HostingMap hostingMap, @NotNull Identifier id) {
        if (allowChangesFrom(hostingMap)) {
            this.hostingMap = hostingMap;
            this.id = id;
            return true;
        }
        return false;
    }

    public final boolean unregister(@NotNull HostingMap hostingMap) {
        if (allowChangesFrom(hostingMap)) {
            this.hostingMap = null;
            this.id = null;
            return true;
        }
        return false;
    }

    private boolean allowChangesFrom(@NotNull HostingMap hostingMap) {
        return this.hostingMap == null || this.hostingMap == hostingMap;
    }

    public final boolean isRegistered() {
        return this.hostingMap != null;
    }

    public @NotNull Identifier getId() {
        if (id == null) {
            throw new IllegalStateException("Hosting not registered");
        }
        return id;
    }
}

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

package me.nelonn.propack.bukkit;

import me.nelonn.propack.UploadedPack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourcePackOffer {
    private final UploadedPack upload;
    private final Component prompt;
    private final boolean shouldForce;

    public ResourcePackOffer(@NotNull UploadedPack upload, @Nullable Component prompt, boolean shouldForce) {
        this.upload = upload;
        this.prompt = prompt;
        this.shouldForce = shouldForce;
    }

    public @NotNull UploadedPack getUpload() {
        return upload;
    }

    public @Nullable Component getPrompt() {
        return prompt;
    }

    public boolean getShouldForce() {
        return shouldForce;
    }

    @Override
    public String toString() {
        return "ResourcePackOffer{" +
                "upload=" + upload +
                ", prompt=" + prompt +
                ", shouldForce=" + shouldForce +
                '}';
    }
}

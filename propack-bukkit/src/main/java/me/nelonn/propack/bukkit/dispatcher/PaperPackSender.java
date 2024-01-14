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

package me.nelonn.propack.bukkit.dispatcher;

import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.bukkit.ResourcePackOffer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class PaperPackSender implements PackSender {
    @Nullable
    public static final PaperPackSender INSTANCE;

    static {
        PaperPackSender instance = null;
        try {
            Method method = Player.class.getDeclaredMethod("setResourcePack", String.class, String.class, boolean.class, Component.class);
            instance = new PaperPackSender(method);
        } catch (Exception ignored) {
        }
        INSTANCE = instance;
    }

    private final Method method;

    private PaperPackSender(@NotNull Method method) {
        this.method = method;
    }

    @Override
    public void send(@NotNull Player player, @NotNull ResourcePackOffer packOffer) {
        UploadedPack uploadedPack = packOffer.getUpload();
        try {
            method.invoke(player, uploadedPack.getUrl(), uploadedPack.getSha1String(), packOffer.getShouldForce(), packOffer.getPrompt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

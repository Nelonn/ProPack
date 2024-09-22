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

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class Util {
    public static void send(@NotNull CommandSender r, @NotNull String s, @NotNull TagResolver... tags) {
        ProPackPlugin.getInstance().adventure().sender(r).sendMessage(MiniMessage.miniMessage().deserialize(s, tags));
    }

    public static <I, O> O map(I value, @NotNull Function<I, O> fn) {
        return value == null ? null : fn.apply(value);
    }

    private Util() {
        throw new UnsupportedOperationException();
    }
}

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

package me.nelonn.propack.bukkit.command.reload;

import me.nelonn.propack.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseReloadCommand extends Command {
    protected BaseReloadCommand(@NotNull String name, String permission, String description, String usageMessage, @NotNull List<String> aliases) {
        super(name, permission, description, usageMessage, aliases);
    }

    protected BaseReloadCommand(@NotNull String name, String... aliases) {
        super(name, aliases);
    }

    protected BaseReloadCommand(@NotNull String name) {
        super(name);
    }

    @Override
    protected void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        execute(sender);
    }

    public abstract void execute(@NotNull CommandSender sender);
}

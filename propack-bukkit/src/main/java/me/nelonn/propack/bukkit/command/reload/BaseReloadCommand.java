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

package me.nelonn.propack.bukkit.command.reload;

import me.nelonn.commandlib.Command;
import me.nelonn.commandlib.CommandContext;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseReloadCommand extends Command<CommandSender> {
    public BaseReloadCommand(@NotNull String name, String... aliases) {
        super(name, aliases);
    }

    public BaseReloadCommand(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean run(@NotNull CommandContext<CommandSender> commandContext) {
        execute(commandContext.getSource());
        return true;
    }

    public abstract void execute(@NotNull CommandSender sender);
}

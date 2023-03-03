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

package me.nelonn.propack.bukkit.command;

import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.Util;
import me.nelonn.propack.bukkit.command.reload.ReloadCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ProPackCommand extends Command {
    private final ProPackPlugin plugin;

    public ProPackCommand(@NotNull ProPackPlugin plugin) {
        super("propack");
        addChildren(new HelpCommand(plugin), new ReloadCommand(plugin), new BuildCommand(plugin));
        this.plugin = plugin;
    }

    protected void onCommand(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {
        Util.send(sender, "<white>" + plugin.getDescription().getName() + " <gray>v" + plugin.getDescription().getVersion());
        if (sender.hasPermission("propack.admin")) {
            Util.send(sender, "<gray>Run <white>/propack help <gray>to view usage information.");
        }
    }
}

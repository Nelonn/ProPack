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

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.Util;
import me.nelonn.propack.bukkit.definition.PackDefinition;
import me.nelonn.propack.bukkit.definition.ProjectDefinition;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BuildCommand extends Command {
    private final ProPackPlugin plugin;

    public BuildCommand(@NotNull ProPackPlugin plugin) {
        super("build");
        setPermission("propack.admin");
        this.plugin = plugin;
    }

    protected void onCommand(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            Util.send(sender, "<red>Usage: /" + s + " <project>");
            return;
        }
        PackDefinition definition = plugin.getCore().getPackManager().getDefinition(args[0]);
        if (!(definition instanceof ProjectDefinition projectDefinition)) {
            Util.send(sender, "<red>Resource pack '" + args[0] + "' is not project");
            return;
        }
        new Thread(() -> {
            try {
                projectDefinition.build();
                ResourcePack resourcePack = projectDefinition.getResourcePack().orElseThrow();
                if (resourcePack.isUploaded()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Optional<ResourcePack> playerPack = ProPack.getCore().getDispatcher().getAppliedResourcePack(player);
                        if (playerPack.isPresent() && playerPack.get().getName().equals(resourcePack.getName())) {
                            ProPack.getCore().getDispatcher().sendOfferAsDefault(player, resourcePack);
                        }
                    }
                }
            } catch (Exception e) {
                Util.send(sender, "<red>Exception: " + e.getMessage());
                Util.send(sender, "<red>Check console for additional info");
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length > 1) return Collections.emptyList();
        String lower = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        return plugin.getCore().getPackManager().getDefinitions().stream().filter(packDefinition -> {
            return packDefinition instanceof ProjectDefinition && packDefinition.getName().startsWith(lower);
        }).map(PackDefinition::getName).toList();
    }
}

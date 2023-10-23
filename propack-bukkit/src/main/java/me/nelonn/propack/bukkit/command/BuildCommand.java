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

package me.nelonn.propack.bukkit.command;

import me.nelonn.commandlib.Command;
import me.nelonn.commandlib.CommandContext;
import me.nelonn.commandlib.suggestion.Suggestions;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.bukkit.ProPack;
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.Util;
import me.nelonn.propack.bukkit.definition.PackDefinition;
import me.nelonn.propack.bukkit.definition.ProjectPack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BuildCommand extends Command<CommandSender> {
    private final ProPackPlugin plugin;

    public BuildCommand(@NotNull ProPackPlugin plugin) {
        super("build");
        requires(s -> s.hasPermission("propack.admin"));
        this.plugin = plugin;
    }

    @Override
    public boolean run(@NotNull CommandContext<CommandSender> context) {
        CommandSender sender = context.getSource();
        if (context.getArguments().length < 1) {
            Util.send(sender, "<red>Usage: /" + context.getInput() + " <project>");
            return false;
        }
        PackDefinition definition = plugin.getCore().getPackManager().getDefinition(context.getArguments()[0]);
        if (!(definition instanceof ProjectPack projectPack)) {
            Util.send(sender, "<red>Resource pack '" + context.getArguments()[0] + "' is not project");
            return false;
        }
        new Thread(() -> {
            try {
                projectPack.build();
                ResourcePack resourcePack = projectPack.getResourcePack().orElseThrow();
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
        return true;
    }

    @Override
    public @Nullable List<String> suggest(@NotNull CommandContext<CommandSender> context) {
        if (context.getArguments().length > 1) return Suggestions.EMPTY;
        List<String> values = new ArrayList<>(); // we don't want StreamAPI here because it is network thread
        for (PackDefinition packDefinition : plugin.getCore().getPackManager().getDefinitions()) {
            values.add(packDefinition.getName());
        }
        return Suggestions.util(context.getArguments()[0], values);
    }
}

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
import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.Util;
import me.nelonn.propack.bukkit.definition.PackDefinition;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GetUrlCommand extends Command<CommandSender> {
    private final ProPackPlugin plugin;

    public GetUrlCommand(@NotNull ProPackPlugin plugin) {
        super("get-url");
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
        if (definition == null) {
            Util.send(sender, "<red>Resource pack '" + context.getArguments()[0] + "' not found");
            return false;
        }
        ResourcePack resourcePack = definition.getResourcePack();
        if (resourcePack == null) {
            Util.send(sender, "<red>Resource pack '" + context.getArguments()[0] + "' not loaded or not built");
            return false;
        }
        if (!resourcePack.isUploaded()) {
            Util.send(sender, "<red>Resource pack '" + context.getArguments()[0] + "' not uploaded");
            return false;
        }
        String url = resourcePack.getUpload().getUrl();
        Util.send(sender, "URL: <green><hover:show_text:'Click to copy'><click:copy_to_clipboard:'" + url + "'>" + url);
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

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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public abstract class Command implements TabExecutor {
    private final String name;
    private final List<String> aliases;
    private final List<Command> children = new ArrayList<>();
    private String permission;
    private String permissionMessage = null;
    protected String description;
    protected String usageMessage;
    private BukkitWrapper bukkitWrapper;

    protected Command(@NotNull String name, String permission, String description, String usageMessage, @NotNull List<String> aliases) {
        this.name = name;
        this.permission = permission;
        this.description = description == null ? "" : description;
        this.usageMessage = usageMessage == null ? "/<command>" : usageMessage;
        this.aliases = new ArrayList<>(aliases);
    }

    protected Command(@NotNull String name, String... aliases) {
        this(name, null, "", "/<command>", List.of(aliases));
    }

    protected Command(@NotNull String name) {
        this(name, null, "", "/<command>", new ArrayList<>());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command bukkitCommand, @NotNull String commandLabel, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            if (this.permissionMessage == null) {
                sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is a mistake.");
            } else if (this.permissionMessage.length() != 0) {
                ProPackPlugin.getInstance().adventure().sender(sender)
                        .sendMessage(LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(permissionMessage.replace("<name>", this.name).replace("<permission>", this.permission)));
            }
            return true;
        }
        if (args.length == 0) {
            onCommand(sender, commandLabel, args);
        } else {
            Command subCommand = findChild(args[0].toLowerCase(Locale.ROOT));
            if (subCommand != null) {
                return subCommand.onCommand(sender, bukkitCommand, commandLabel + " " + args[0].toLowerCase(Locale.ROOT), subArgs(args));
            } else {
                onCommand(sender, commandLabel, args);
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command bukkitCommand, @NotNull String commandLabel, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        String commandName = args[0].toLowerCase(Locale.ROOT);
        Command subCommand = findChild(commandName);
        if (subCommand != null) {
            if (args.length > 1) {
                return subCommand.onTabComplete(sender, bukkitCommand, commandLabel + " " + commandName, subArgs(args));
            }
            return Collections.emptyList();
        }
        if (!children.isEmpty() && args.length == 1) {
            List<String> childCommands = new ArrayList<>();
            for (Command cmd : children) {
                if (!cmd.hasPermission(sender)) continue;
                predicateName(cmd, string -> {
                    if (string.startsWith(commandName)) {
                        childCommands.add(string);
                    }
                    return true;
                });
            }
            return childCommands;
        }
        return onTabComplete(sender, commandLabel, args);
    }

    protected abstract void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args);

    protected List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return Collections.emptyList();
    }

    public boolean hasPermission(CommandSender sender) {
        return this.permission == null || this.permission.isEmpty() || sender.hasPermission(this.permission);
    }

    public void sendUsageMessage(CommandSender sender, String commandLabel) {
        for (String line : ("&c" + usageMessage.replace("<command>", commandLabel)).split("\n")) {
            ProPackPlugin.getInstance().adventure().sender(sender)
                            .sendMessage(LegacyComponentSerializer.legacySection().deserialize(line));
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void addChildren(Command... children) {
        this.children.addAll(List.of(children));
    }

    public void removeChild(Command child) {
        this.children.remove(child);
    }

    public List<Command> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsageMessage() {
        return usageMessage;
    }

    public void setUsageMessage(String usageMessage) {
        this.usageMessage = usageMessage;
    }

    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                ", aliases=" + aliases +
                ", children=" + children +
                ", permission='" + permission + '\'' +
                ", permissionMessage='" + permissionMessage + '\'' +
                ", description='" + description + '\'' +
                ", usageMessage='" + usageMessage + '\'' +
                '}';
    }

    private Command findChild(String name) {
        for (Command child : children) {
            if (predicateName(child, string -> string.equalsIgnoreCase(name))) return child;
        }
        return null;
    }

    public void register(@NotNull Plugin plugin) {
        if (bukkitWrapper != null) return;
        SimpleCommandMap simpleCommandMap;
        try {
            simpleCommandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
        } catch (Exception e) {
            return;
        }

        bukkitWrapper = new BukkitWrapper(plugin, this);
        simpleCommandMap.register(plugin.getName().toLowerCase(), bukkitWrapper);
    }

    private static boolean predicateName(Command command, Predicate<String> predicate) {
        if (predicate.test(command.getName())) return true;
        for (String alias : command.getAliases()) {
            if (predicate.test(alias)) return true;
        }
        return false;
    }

    private static String[] subArgs(String[] args) {
        String[] cropArgs = new String[args.length - 1];
        if (args.length == 1) return cropArgs;
        System.arraycopy(args, 1, cropArgs, 0, cropArgs.length);
        return cropArgs;
    }

    public static class BukkitWrapper extends org.bukkit.command.Command implements PluginIdentifiableCommand {
        private final Plugin owningPlugin;
        private final Command command;

        public BukkitWrapper(@NotNull Plugin owner, @NotNull Command command) {
            super(command.getName(), command.getDescription(), command.getUsageMessage(), command.getAliases());
            this.owningPlugin = owner;
            this.command = command;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            boolean success;

            if (!owningPlugin.isEnabled()) {
                throw new CommandException("Cannot execute command '" + commandLabel + "' in plugin " + owningPlugin.getDescription().getFullName() + " - plugin is disabled.");
            }

            if (!testPermission(sender)) {
                return true;
            }

            try {
                success = command.onCommand(sender, this, commandLabel, args);
            } catch (Throwable ex) {
                throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin " + owningPlugin.getDescription().getFullName(), ex);
            }

            if (!success && command.getUsageMessage().length() > 0) {
                command.sendUsageMessage(sender, commandLabel);
            }

            return success;
        }

        @Override
        @NotNull
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            List<String> completions;
            try {
                completions = command.onTabComplete(sender, this, alias, args);
            } catch (Throwable ex) {
                StringBuilder message = new StringBuilder();
                message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
                for (String arg : args) {
                    message.append(arg).append(' ');
                }
                message.deleteCharAt(message.length() - 1).append("' in plugin ").append(owningPlugin.getDescription().getFullName());
                throw new CommandException(message.toString(), ex);
            }

            return Objects.requireNonNullElseGet(completions, () -> super.tabComplete(sender, alias, args));
        }

        @Override
        @NotNull
        public Plugin getPlugin() {
            return owningPlugin;
        }

        @NotNull
        public Command getCommand() {
            return command;
        }
    }
}

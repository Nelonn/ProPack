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

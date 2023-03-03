package me.nelonn.propack.bukkit.command.reload;

import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.Util;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadConfigCommand extends BaseReloadCommand {
    private final ProPackPlugin plugin;

    protected ReloadConfigCommand(@NotNull ProPackPlugin plugin) {
        super("config", "conf");
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSender sender) {
        plugin.reloadConfig();
        Util.send(sender, "<white>ProPack <gray>config reloaded successfully");
    }
}

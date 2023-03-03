package me.nelonn.propack.bukkit.command.reload;

import me.nelonn.propack.bukkit.ProPackPlugin;
import me.nelonn.propack.bukkit.Util;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadPacksCommand extends BaseReloadCommand {
    private final ProPackPlugin plugin;

    protected ReloadPacksCommand(@NotNull ProPackPlugin plugin) {
        super("packs");
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSender sender) {
        plugin.reloadPacks();
        Util.send(sender, "<white>ProPack <gray>resource packs reloaded successfully");
    }
}

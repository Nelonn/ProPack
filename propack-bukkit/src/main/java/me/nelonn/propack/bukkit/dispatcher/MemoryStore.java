package me.nelonn.propack.bukkit.dispatcher;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStore implements Store, Listener {
    private final Map<UUID, SentPack> map = new ConcurrentHashMap<>();

    public MemoryStore(@NotNull Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @Nullable SentPack getActiveResourcePack(@NotNull UUID uuid) {
        return map.get(uuid);
    }

    @Override
    public void setActiveResourcePack(@NotNull UUID uuid, @Nullable SentPack sentPack) {
        map.put(uuid, sentPack);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        map.remove(event.getPlayer().getUniqueId());
    }
}

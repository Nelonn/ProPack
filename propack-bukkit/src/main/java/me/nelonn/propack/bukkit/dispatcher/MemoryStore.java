package me.nelonn.propack.bukkit.dispatcher;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStore implements Store, Listener {
    private final Map<UUID, SentPack> map = new ConcurrentHashMap<>();

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

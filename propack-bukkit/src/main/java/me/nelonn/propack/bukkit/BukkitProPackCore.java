package me.nelonn.propack.bukkit;

import me.nelonn.propack.bukkit.definition.PackManager;
import me.nelonn.propack.bukkit.dispatcher.Dispatcher;
import me.nelonn.propack.bukkit.dispatcher.MemoryStore;
import me.nelonn.propack.bukkit.dispatcher.StoreMap;
import me.nelonn.propack.core.ProPackCore;
import org.jetbrains.annotations.NotNull;

public class BukkitProPackCore extends ProPackCore {
    private final PackManager packManager;
    private final StoreMap storeMap;
    private final Dispatcher dispatcher;

    public BukkitProPackCore(@NotNull ProPackPlugin plugin) {
        super(plugin.getDataFolder());
        packManager = new PackManager(this, plugin.getDataFolder());
        storeMap = new StoreMap();
        storeMap.register("memory_store", new MemoryStore(plugin));
        dispatcher = new Dispatcher(plugin);
    }

    public PackManager getPackManager() {
        return packManager;
    }

    public StoreMap getStoreMap() {
        return storeMap;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}

package me.nelonn.propack.bukkit.dispatcher;

import me.nelonn.flint.path.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StoreMap {
    private final Map<Identifier, Store> knownStores = new HashMap<>();

    public boolean register(@NotNull Identifier id, @NotNull Store store) {
        if (knownStores.containsKey(id)) return false;
        knownStores.put(id, store);
        return true;
    }

    public boolean register(@NotNull String id, @NotNull Store store) {
        return register(Identifier.ofWithFallback(id, "propack"), store);
    }

    public boolean unregister(@NotNull Identifier id) {
        return knownStores.remove(id) != null;
    }

    public boolean unregister(@NotNull String id) {
        return unregister(Identifier.ofWithFallback(id, "propack"));
    }

    public @Nullable Store get(@NotNull Identifier id) {
        return knownStores.get(id);
    }

    public @Nullable Store get(@NotNull String id) {
        return get(Identifier.ofWithFallback(id, "propack"));
    }
}

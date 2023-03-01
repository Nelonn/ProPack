package me.nelonn.propack.core;

import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.builder.hosting.Hosting;
import me.nelonn.propack.builder.hosting.HostingMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleHostingMap implements HostingMap {
    protected final Map<Identifier, Hosting> knownHostings = new HashMap<>();

    public SimpleHostingMap() {
        setDefaultHosting();
    }

    private void setDefaultHosting() {
        register("propack", new DevServer("dev_server", "127.0.0.1", 3000));
    }

    @Override
    public boolean register(@NotNull String namespace, @NotNull Hosting hosting) {
        if (hosting.register(this)) {
            knownHostings.put(Identifier.of(namespace, hosting.getName()), hosting);
            return true;
        }
        return false;
    }

    @Override
    public void registerAll(@NotNull String namespace, @NotNull List<Hosting> hostings) {
        for (Hosting hosting : hostings) {
            register(namespace, hosting);
        }
    }

    @Override
    public @NotNull Hosting getHosting(@NotNull Identifier id) {
        return knownHostings.get(id);
    }

    @Override
    public @NotNull Map<Identifier, Hosting> getKnownHostings() {
        return knownHostings;
    }
}

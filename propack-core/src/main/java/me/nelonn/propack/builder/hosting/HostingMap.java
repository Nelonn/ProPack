package me.nelonn.propack.builder.hosting;

import me.nelonn.flint.path.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface HostingMap {
    boolean register(@NotNull String namespace, @NotNull Hosting hosting);

    void registerAll(@NotNull String namespace, @NotNull List<Hosting> hostings);

    @NotNull Hosting getHosting(@NotNull Identifier id);

    @NotNull Map<Identifier, Hosting> getKnownHostings();
}

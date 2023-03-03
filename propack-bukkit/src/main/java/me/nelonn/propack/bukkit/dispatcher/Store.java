package me.nelonn.propack.bukkit.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Store {

    @Nullable SentPack getActiveResourcePack(@NotNull UUID uuid);

    void setActiveResourcePack(@NotNull UUID uuid, @Nullable SentPack sentPack);

}

package me.nelonn.propack.bukkit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Store {

    @Nullable String getActiveResourcePack(@NotNull UUID uuid);

    void setActiveResourcePack(@NotNull UUID uuid, @Nullable String rpName);

}

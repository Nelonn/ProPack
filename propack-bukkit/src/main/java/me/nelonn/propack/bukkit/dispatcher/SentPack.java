package me.nelonn.propack.bukkit.dispatcher;

import org.jetbrains.annotations.NotNull;

public class SentPack {
    public final String name;
    public final String sha1;

    public SentPack(@NotNull String name, @NotNull String sha1) {
        this.name = name;
        this.sha1 = sha1;
    }
}

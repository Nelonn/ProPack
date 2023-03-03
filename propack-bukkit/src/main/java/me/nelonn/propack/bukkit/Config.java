/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Nelonn <two.nelonn@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nelonn.propack.bukkit;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum Config {
    DISPATCHER_STORE("dispatcher.store", "propack:memory_store"),
    DISPATCHER_ENABLED("dispatcher.enabled", true),
    DISPATCHER_REPLACE("dispatcher.replace", false),
    DISPATCHER_PACK("dispatcher.pack"),
    DISPATCHER_DELAY("dispatcher.delay", 0),
    DISPATCHER_REQUIRED("dispatcher.required", false),
    DISPATCHER_PROMPT("dispatcher.prompt", "&#fa4943Accept the pack to enjoy a full experience"),

    DEV_SERVER_ENABLED("dev_server.enabled", false),
    DEV_SERVER_RETURN_IP("dev_server.return_ip", "127.0.0.1"),
    DEV_SERVER_PORT("dev_server.port", 3000),

    PATCH_PACKETS_ITEMS("patch_packets.items", true),
    PATCH_PACKETS_SOUNDS("patch_packets.sounds", true);

    private final String path;
    private final Object def;

    Config(@NotNull String path, @Nullable Object def) {
        this.path = path;
        this.def = def;
    }

    Config(@NotNull String path) {
        this(path, null);
    }

    public @NotNull String getPath() {
        return path;
    }

    public Object getValue() {
        return ProPackPlugin.getInstance().getConfig().get(path, def);
    }

    public String asString() {
        return (String) getValue();
    }

    public Boolean asBoolean() {
        return (Boolean) getValue();
    }

    public Integer asInteger() {
        return (Integer) getValue();
    }

    public List<String> asStringList() {
        return ProPackPlugin.getInstance().getConfig().getStringList(path);
    }

    public ConfigurationSection asConfigSection() {
        return ProPackPlugin.getInstance().getConfig().getConfigurationSection(path);
    }

    @Override
    public String toString() {
        return asString();
    }

}

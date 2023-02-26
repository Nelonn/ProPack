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

public enum Settings {
    DISPATCH_ENABLED("dispatch.enabled", true),
    DISPATCH_DELAY("dispatch.delay", -1),
    DISPATCH_REQUIRED("dispatch.required", true),
    DISPATCH_PACK("dispatch.pack"),
    DISPATCH_PROMPT("dispatch.prompt", "&#fa4943Accept the pack to enjoy a full experience"),

    PATCH_PACKETS_ITEMS("patch_packets.items", true),
    PATCH_PACKETS_SOUNDS("patch_packets.sounds", true);

    private final String path;
    private final Object def;

    Settings(@NotNull String path, @Nullable Object def) {
        this.path = path;
        this.def = def;
    }

    Settings(@NotNull String path) {
        this(path, null);
    }

    public String getPath() {
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

    public List<String> asStringList() {
        return ProPackPlugin.getInstance().getConfig().getStringList(path);
    }

    public ConfigurationSection asConfigSection() {
        return ProPackPlugin.getInstance().getConfig().getConfigurationSection(path);
    }

    public String toString() {
        return asString();
    }

}

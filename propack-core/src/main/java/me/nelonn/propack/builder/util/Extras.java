/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Michael Neonov <two.nelonn@gmail.com>
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

package me.nelonn.propack.builder.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Extras {
    private final Map<String, Object> map;

    public Extras() {
        map = new ConcurrentHashMap<>();
    }

    public @Nullable Object get(@NotNull String name, @Nullable Object defaultValue) {
        return map.getOrDefault(name, defaultValue);
    }

    public @Nullable Object get(@NotNull String name) {
        return get(name, null);
    }

    public void put(@NotNull String name, @NotNull Object value) {
        map.put(name, value);
    }

    public void remove(@NotNull String name) {
        map.remove(name);
    }

    public boolean contains(@NotNull String name) {
        return map.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable T getTyped(@NotNull String name, @Nullable T defaultValue) {
        Object o = get(name);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (T) o;
        } catch (ClassCastException e) {
            //typeWarning(name, o, defaultValue.getClass().getSimpleName(), defaultValue, e);
            return defaultValue;
        }
    }

    private <T> @Nullable T getTyped(@NotNull String name) {
        return getTyped(name, null);
    }

    public <T> @Nullable T get(@NotNull Extra<T> extra) {
        return getTyped(extra.getName());
    }

    public <T> @Nullable T get(@NotNull Extra<T> extra, @Nullable T defaultValue) {
        return getTyped(extra.getName(), defaultValue);
    }

    public <T> void put(@NotNull Extra<T> extra, @NotNull T value) {
        put(extra.getName(), value);
    }

    public @Nullable Boolean getBoolean(@NotNull String name, @Nullable Boolean defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Boolean getBoolean(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable Byte getByte(@NotNull String name, @Nullable Byte defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Byte getByte(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable Character getChar(@NotNull String name, @Nullable Character defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Character getChar(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable Short getShort(@NotNull String name, @Nullable Short defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Short getShort(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable Integer getInt(@NotNull String name, @Nullable Integer defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Integer getInt(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable Long getLong(@NotNull String name, @Nullable Long defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Long getLong(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable Float getFloat(@NotNull String name, @Nullable Float defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Float getFloat(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable Double getDouble(@NotNull String name, @Nullable Double defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable Double getDouble(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable String getString(@NotNull String name, @Nullable String defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable String getString(@NotNull String name) {
        return getTyped(name);
    }

    public @Nullable File getFile(@NotNull String name, @Nullable File defaultValue) {
        return getTyped(name, defaultValue);
    }

    public @Nullable File getFile(@NotNull String name) {
        return getTyped(name);
    }
}

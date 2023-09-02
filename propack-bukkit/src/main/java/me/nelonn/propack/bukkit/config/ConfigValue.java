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

package me.nelonn.propack.bukkit.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigValue<T> {
    private final String path;
    private final T defaultValue;
    private final Class<T> clazz;
    private final Deserializer<T> deserializer;

    public ConfigValue(@NotNull String path, @Nullable T defaultValue, @Nullable Class<T> clazz, @Nullable Deserializer<T> deserializer) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.clazz = clazz == null ? getOwnGeneric() : clazz;
        this.deserializer = deserializer == null ? new DefaultDeserializer<>() : deserializer;
    }

    public ConfigValue(@NotNull String path, @Nullable T defaultValue, @Nullable Deserializer<T> deserializer) {
        this(path, defaultValue, null, deserializer);
    }

    public ConfigValue(@NotNull String path, @Nullable T defaultValue, @Nullable Class<T> clazz) {
        this(path, defaultValue, clazz, null);
    }

    public ConfigValue(@NotNull String path, @Nullable Deserializer<T> deserializer) {
        this(path, null, deserializer);
    }

    public ConfigValue(@NotNull String path, @Nullable Class<T> clazz) {
        this(path, null, clazz);
    }

    public ConfigValue(@NotNull String path, @Nullable T defaultValue) {
        this(path, defaultValue, (Class<T>) null);
    }

    public ConfigValue(@NotNull String path) {
        this(path, (T) null);
    }

    public @NotNull String getPath() {
        return path;
    }

    public @Nullable T getDefaultValue() {
        return defaultValue;
    }

    public @NotNull Deserializer<T> getDeserializer() {
        return deserializer;
    }

    public @NotNull Class<T> getClazz() {
        return clazz;
    }

    @SuppressWarnings("unchecked")
    private Class<T> getOwnGeneric() {
        return (Class<T>) getClass().getGenericSuperclass().getClass();
    }

    public interface Deserializer<T> {
        @NotNull
        T deserialize(Object obj);
    }

    public static class DefaultDeserializer<T> implements Deserializer<T> {

        @SuppressWarnings("unchecked")
        @Override
        @NotNull
        public T deserialize(Object obj) {
            return (T) obj;
        }

    }
}
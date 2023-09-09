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

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SectionConfig {
    private ConfigurationSection config;

    public SectionConfig(@Nullable ConfigurationSection config) {
        this.config = config;
    }

    public void setRaw(ConfigurationSection config) {
        this.config = config;
    }

    public ConfigurationSection getRaw() {
        return config;
    }

    @Nullable
    public <T> T getNullable(@NotNull ConfigValue<T> configValue) {
        if (config == null) {
            throw new NullPointerException();
        }
        String path = configValue.getPath();
        Object obj = config.get(path);
        if (obj == null) {
            return configValue.getDefaultValue();
        }
        return configValue.getDeserializer().deserialize(obj);
    }

    @NotNull
    public <T> T get(@NotNull ConfigValue<T> configValue) {
        T value = getNullable(configValue);
        if (value == null) {
            throw new NullPointerException("Config value at '" + configValue.getPath() + "' is null");
        }
        return value;
    }
}
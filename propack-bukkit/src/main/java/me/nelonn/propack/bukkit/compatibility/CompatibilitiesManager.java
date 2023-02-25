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

package me.nelonn.propack.bukkit.compatibility;

import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.bukkit.compatibility.provided.placeholderapi.PlaceholderAPICompatibility;
import me.nelonn.propack.bukkit.compatibility.provided.protocollib.ProtocolLibCompatibility;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CompatibilitiesManager {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private static final ConcurrentHashMap<String, Class<? extends CompatibilityProvider<?>>> COMPATIBILITY_PROVIDERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CompatibilityProvider<?>> ACTIVE_COMPATIBILITY_PROVIDERS = new ConcurrentHashMap<>();

    public static void enableNativeCompatibilities(@NotNull Plugin plugin) {
        new CompatibilityListener(plugin);
        addCompatibility("ProtocolLib", ProtocolLibCompatibility.class, true);
        addCompatibility("PlaceholderAPI", PlaceholderAPICompatibility.class, true);
    }

    public static void disableCompatibilities() {
        ACTIVE_COMPATIBILITY_PROVIDERS.forEach((pluginName, compatibilityProvider) -> disableCompatibility(pluginName));
    }

    public static boolean enableCompatibility(@NotNull String pluginName) {
        try {
            if (!ACTIVE_COMPATIBILITY_PROVIDERS.containsKey(pluginName) &&
                    COMPATIBILITY_PROVIDERS.containsKey(pluginName) &&
                    Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                CompatibilityProvider<?> compatibilityProvider = COMPATIBILITY_PROVIDERS.get(pluginName).getConstructor().newInstance();
                compatibilityProvider.enable(pluginName);
                ACTIVE_COMPATIBILITY_PROVIDERS.put(pluginName, compatibilityProvider);
                LOGGER.info("Plugin '{}' detected, enabling hooks", pluginName);
                return true;
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean disableCompatibility(@NotNull String pluginName) {
        try {
            if (!ACTIVE_COMPATIBILITY_PROVIDERS.containsKey(pluginName)) return false;
            if (ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName).isEnabled()) {
                ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName).disable();
            }
            ACTIVE_COMPATIBILITY_PROVIDERS.remove(pluginName);
            LOGGER.info("Unhooking plugin '{}'", pluginName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addCompatibility(@NotNull String compatibilityPluginName, @NotNull Class<? extends CompatibilityProvider<?>> clazz, boolean tryEnable) {
        try {
            COMPATIBILITY_PROVIDERS.put(compatibilityPluginName, clazz);
            return !tryEnable || enableCompatibility(compatibilityPluginName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addCompatibility(@NotNull String compatibilityPluginName, @NotNull Class<? extends CompatibilityProvider<?>> clazz) {
        return addCompatibility(compatibilityPluginName, clazz, false);
    }

    public static @Nullable CompatibilityProvider<?> getActiveCompatibility(@NotNull String pluginName) {
        return ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName);
    }

    public static @Nullable Class<? extends CompatibilityProvider<?>> getCompatibility(@NotNull String pluginName) {
        return COMPATIBILITY_PROVIDERS.get(pluginName);
    }

    public static boolean isCompatibilityEnabled(@NotNull String pluginName) {
        return ACTIVE_COMPATIBILITY_PROVIDERS.containsKey(pluginName) && ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName).isEnabled();
    }

    public static @NotNull ConcurrentMap<String, Class<? extends CompatibilityProvider<?>>> getCompatibilityProviders() {
        return COMPATIBILITY_PROVIDERS;
    }

    public static @NotNull ConcurrentMap<String, CompatibilityProvider<?>> getActiveCompatibilityProviders() {
        return ACTIVE_COMPATIBILITY_PROVIDERS;
    }

    public static boolean hasPlugin(@NotNull String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

    private CompatibilitiesManager() {
        throw new UnsupportedOperationException();
    }
}

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

    public static boolean enableCompatibility(final String pluginName) {
        try {
            if (!ACTIVE_COMPATIBILITY_PROVIDERS.containsKey(pluginName) && COMPATIBILITY_PROVIDERS.containsKey(pluginName) && hasPlugin(pluginName)) {
                final CompatibilityProvider<?> compatibilityProvider = COMPATIBILITY_PROVIDERS.get(pluginName).getConstructor().newInstance();
                compatibilityProvider.enable(pluginName);
                ACTIVE_COMPATIBILITY_PROVIDERS.put(pluginName, compatibilityProvider);
                LOGGER.info("Plugin '{}' detected, enabling hooks", pluginName);
                return true;
            }
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean disableCompatibility(final String pluginName) {
        try {
            if (!ACTIVE_COMPATIBILITY_PROVIDERS.containsKey(pluginName)) return false;
            if (ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName).isEnabled()) {
                ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName).disable();
            }
            ACTIVE_COMPATIBILITY_PROVIDERS.remove(pluginName);
            LOGGER.info("Unhooking plugin '{}'", pluginName);
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addCompatibility(final String compatibilityPluginName, final Class<? extends CompatibilityProvider<?>> clazz, final boolean tryEnable) {
        try {
            if (compatibilityPluginName != null && clazz != null) {
                COMPATIBILITY_PROVIDERS.put(compatibilityPluginName, clazz);
                return !tryEnable || enableCompatibility(compatibilityPluginName);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean addCompatibility(final String compatibilityPluginName, final Class<? extends CompatibilityProvider<?>> clazz) {
        return addCompatibility(compatibilityPluginName, clazz, false);
    }

    public static CompatibilityProvider<?> getActiveCompatibility(final String pluginName) {
        return ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName);
    }

    public static Class<? extends CompatibilityProvider<?>> getCompatibility(final String pluginName) {
        return COMPATIBILITY_PROVIDERS.get(pluginName);
    }

    public static boolean isCompatibilityEnabled(final String pluginName) {
        return ACTIVE_COMPATIBILITY_PROVIDERS.containsKey(pluginName) && ACTIVE_COMPATIBILITY_PROVIDERS.get(pluginName).isEnabled();
    }

    public static ConcurrentMap<String, Class<? extends CompatibilityProvider<?>>> getCompatibilityProviders() {
        return COMPATIBILITY_PROVIDERS;
    }

    public static ConcurrentMap<String, CompatibilityProvider<?>> getActiveCompatibilityProviders() {
        return ACTIVE_COMPATIBILITY_PROVIDERS;
    }

    public static boolean hasPlugin(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    private CompatibilitiesManager() {
    }
}

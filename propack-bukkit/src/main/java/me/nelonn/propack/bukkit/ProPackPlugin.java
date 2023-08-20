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

import me.nelonn.propack.bukkit.command.ProPackCommand;
import me.nelonn.propack.bukkit.compatibility.CompatibilitiesManager;
import me.nelonn.propack.bukkit.config.PluginConfig;
import me.nelonn.propack.bukkit.dispatcher.ActivePackStore;
import me.nelonn.propack.core.DevServer;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.core.util.LogManagerCompat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class ProPackPlugin extends JavaPlugin {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private static final SharedLoader.Library library = new SharedLoader.Library("flint-path-0.0.1.jar", "me.nelonn.flint.path.Path");

    public static ProPackPlugin getInstance() {
        return (ProPackPlugin) Bukkit.getPluginManager().getPlugin("ProPack");
    }

    private BukkitAudiences adventure;
    private BukkitProPackCore core;
    private DevServer devServer;
    private PluginConfig config;

    @Override
    public void onLoad() {
        new SharedLoader(this).loadIfNotExists(library);

        File modulesDir = new File(getDataFolder(), "modules");
        if (!getDataFolder().exists()) {
            IOUtil.extractResources(ProPackPlugin.class, "example/", new File(getDataFolder(), "example"));
            saveResource("example.json", false);
            saveResource("config.yml", false);
            modulesDir.mkdirs();
        } else {
            if (!new File(getDataFolder(), "config.yml").exists()) {
                saveResource("config.yml", false);
            }
            if (!modulesDir.exists()) {
                modulesDir.mkdirs();
            }
        }
    }

    @Override
    public void onEnable() {
        try {
            Class.forName(library.getCheckClass());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("ProPack shared library not loaded");
        }

        adventure = BukkitAudiences.create(this);

        core = new BukkitProPackCore(this);
        ProPack.setCore(core);
        config = new PluginConfig(this, "config.yml");
        reloadModules();
        reloadConfig();
        reloadPacks();

        new ProPackCommand(this).register(this);

        LOGGER.info("Successfully enabled");

        CompatibilitiesManager.enableNativeCompatibilities(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        CompatibilitiesManager.disableCompatibilities();
        adventure.close();
        adventure = null;
        core.getModuleManager().disableAll();
        if (devServer != null) {
            core.getHostingMap().unregister(devServer);
            try {
                devServer.close();
            } catch (Exception ignored) {
            }
            devServer = null;
        }
        LOGGER.info("ProPack {} is now disabled", getDescription().getVersion());
    }

    @Override
    public void reloadConfig() {
        config.load();
        if (devServer != null) {
            core.getHostingMap().unregister(devServer);
            try {
                devServer.close();
            } catch (Exception ignored) {
            }
            devServer = null;
        }
        if (config.get(Config.devServerEnabled)) {
            devServer = new DevServer(config.get(Config.devServerHostIp), config.get(Config.devServerPort));
            core.getHostingMap().register("dev_server", devServer);
        }
        String dispatcherStore = config.get(Config.dispatcherStore);
        ActivePackStore activePackStore = core.getActivePackStoreMap().get(dispatcherStore);
        if (activePackStore == null) {
            LOGGER.error("Store '{}' not found", dispatcherStore);
        }
        core.getDispatcher().setStore(activePackStore);
    }

    public void reloadModules() {
        try {
            core.getModuleManager().loadAll();
        } catch (Exception e) {
            LOGGER.error("Unable to load modules", e);
        }
    }

    public void reloadPacks() {
        core.getPackManager().loadAll();
    }

    public @NotNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @NotNull
    public PluginConfig config() {
        return config;
    }

    public BukkitProPackCore getCore() {
        return core;
    }
}

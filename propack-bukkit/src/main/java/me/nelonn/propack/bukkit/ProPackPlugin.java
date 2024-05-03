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

package me.nelonn.propack.bukkit;

import me.nelonn.commandlib.bukkit.BukkitCommands;
import me.nelonn.configlib.PluginConfig;
import me.nelonn.propack.bukkit.command.ProPackCommand;
import me.nelonn.propack.bukkit.compatibility.CompatibilitiesManager;
import me.nelonn.propack.bukkit.dispatcher.ActivePackStore;
import me.nelonn.propack.bukkit.packet.ItemPatcher;
import me.nelonn.propack.bukkit.packet.PacketListener;
import me.nelonn.propack.core.util.JarResources;
import me.nelonn.propack.core.util.LogManagerCompat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

public final class ProPackPlugin extends JavaPlugin {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private static final DependencyManager.JarLibrary library = new DependencyManager.JarLibrary("flint-path-0.0.1.jar", "me.nelonn.flint.path.Path");

    public static ProPackPlugin getInstance() {
        return JavaPlugin.getPlugin(ProPackPlugin.class);
    }

    private BukkitAudiences adventure;
    private BukkitProPackCore core;
    private ItemPatcher itemPatcher;
    private DevServer devServer;
    private PluginConfig config;

    @Override
    public void onLoad() {
        LogManagerCompat.FORCE_LOGGER_NAME = "ProPack";

        new DependencyManager(this).addLibrary(library);

        File modulesDir = new File(getDataFolder(), "modules");
        if (!getDataFolder().exists()) {
            JarResources.extractDirectory(this, "resources/example/", new File(getDataFolder(), "example"));
            JarResources.extractFile(this, "resources/example.json", new File(getDataFolder(), "example.json"));
            JarResources.extractFile(this, "resources/config.yml", new File(getDataFolder(), "config.yml"));
            modulesDir.mkdirs();
        } else {
            if (!new File(getDataFolder(), "config.yml").exists()) {
                JarResources.extractFile(this, "resources/config.yml", new File(getDataFolder(), "config.yml"));
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
        itemPatcher = new ItemPatcher();
        config = new PluginConfig(this, "resources/config.yml", "config.yml");
        reloadModules();
        reloadConfig();
        reloadPacks();

        PacketListener.register(this);

        BukkitCommands.register(this, new ProPackCommand(this));

        CompatibilitiesManager.enableNativeCompatibilities(this);

        LOGGER.info("Successfully enabled");
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

    public ItemPatcher getItemPatcher() {
        return itemPatcher;
    }

    public BukkitProPackCore getCore() {
        return core;
    }
}

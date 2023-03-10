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
import me.nelonn.propack.bukkit.definition.PackManager;
import me.nelonn.propack.bukkit.dispatcher.Dispatcher;
import me.nelonn.propack.bukkit.dispatcher.MemoryStore;
import me.nelonn.propack.bukkit.dispatcher.StoreMap;
import me.nelonn.propack.core.DevServer;
import me.nelonn.propack.core.ProPackCore;
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

    public static ProPackPlugin getInstance() {
        return (ProPackPlugin) Bukkit.getPluginManager().getPlugin("ProPack");
    }

    private BukkitAudiences adventure;
    private ProPackCore proPackCore;
    private PackManager packManager;
    private StoreMap storeMap;
    private Dispatcher dispatcher;
    private DevServer devServer;

    @Override
    public void onLoad() {
        ProPack.setPlugin(this);

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
        adventure = BukkitAudiences.create(this);

        proPackCore = new ProPackCore(getDataFolder());
        reloadConfig();
        proPackCore.getModuleManager().fullReload();

        packManager = new PackManager(proPackCore, getDataFolder());
        reloadPacks();

        storeMap = new StoreMap();
        storeMap.register("memory_store", new MemoryStore(this));

        dispatcher = new Dispatcher(this);
        Bukkit.getPluginManager().registerEvents(dispatcher, this);

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
        proPackCore.getModuleManager().disableAllAndClear();
        if (devServer != null) {
            proPackCore.getHostingMap().unregister(devServer);
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
        super.reloadConfig();
        proPackCore.getModuleManager().fullReload();
        if (devServer != null) {
            proPackCore.getHostingMap().unregister(devServer);
            try {
                devServer.close();
            } catch (Exception ignored) {
            }
            devServer = null;
        }
        proPackCore.getProjectLoader().getItemDefinitionLoaders().add(BukkitItemDefinitionLoader.INSTANCE);
        if (Config.DEV_SERVER_ENABLED.asBoolean()) {
            devServer = new DevServer(Config.DEV_SERVER_RETURN_IP.asString(), Config.DEV_SERVER_PORT.asInt());
            proPackCore.getHostingMap().register("dev_server", devServer);
        }
    }

    public void reloadPacks() {
        packManager.loadAll();
    }

    public @NotNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public ProPackCore getCore() {
        return proPackCore;
    }

    public PackManager getPackManager() {
        return packManager;
    }

    public StoreMap getStoreMap() {
        return storeMap;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}

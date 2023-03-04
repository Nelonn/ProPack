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

    public static ProPackPlugin getInstance() {
        return (ProPackPlugin) Bukkit.getPluginManager().getPlugin("ProPack");
    }

    private BukkitAudiences adventure;
    private BukkitProPackCore core;
    private DevServer devServer;

    @Override
    public void onLoad() {
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

        core = new BukkitProPackCore(this);
        ProPack.setCore(core);
        reloadConfig();
        reloadModules();
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
        core.getModuleManager().disableAllAndClear();
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
        super.reloadConfig();
        Config.setFileConfiguration(getConfig());
        core.getModuleManager().fullReload();
        if (devServer != null) {
            core.getHostingMap().unregister(devServer);
            try {
                devServer.close();
            } catch (Exception ignored) {
            }
            devServer = null;
        }
        core.getProjectLoader().getItemDefinitionLoaders().add(BukkitItemDefinitionLoader.INSTANCE);
        if (Config.DEV_SERVER_ENABLED.asBoolean()) {
            devServer = new DevServer(Config.DEV_SERVER_RETURN_IP.asString(), Config.DEV_SERVER_PORT.asInt());
            core.getHostingMap().register("dev_server", devServer);
        }
    }

    public void reloadModules() {
        core.getModuleManager().fullReload();
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

    public BukkitProPackCore getCore() {
        return core;
    }
}

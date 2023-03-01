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

import me.nelonn.propack.core.ProPackCore;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.IOUtil;
import me.nelonn.propack.bukkit.command.ProPackCommand;
import me.nelonn.propack.bukkit.compatibility.CompatibilitiesManager;
import me.nelonn.propack.bukkit.resourcepack.PackContainer;
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
    private PackContainer packContainer;
    private Dispatcher dispatcher;

    @Override
    public void onLoad() {
        ProPack.setPlugin(this);

        if (!getDataFolder().exists()) {
            IOUtil.extractResources(ProPackPlugin.class, "example/", new File(getDataFolder(), "example"));
            saveResource("example.json", false);
            saveResource("config.yml", false);
        } else if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        proPackCore = new ProPackCore();
        proPackCore.getProjectLoader().getItemDefinitionLoaders().add(BukkitItemDefinitionLoader.INSTANCE);
        packContainer = new PackContainer(proPackCore, getDataFolder());
        reloadConfigs();

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
        LOGGER.info("ProPack {} is now disabled", getDescription().getVersion());
    }

    public void reloadConfigs() {
        reloadConfig();
        packContainer.loadAll();
    }

    public @NotNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public ProPackCore getProPackCore() {
        return proPackCore;
    }

    public PackContainer getPackContainer() {
        return packContainer;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}

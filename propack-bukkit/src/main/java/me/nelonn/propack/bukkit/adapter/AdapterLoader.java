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

package me.nelonn.propack.bukkit.adapter;

import org.bukkit.Bukkit;

public class AdapterLoader {
    public static final Adapter ADAPTER;

    static {
        Adapter loaded;
        try {
            String craftBukkit = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            String minecraft = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String nmsVersion = craftBukkit;
            if (minecraft.equalsIgnoreCase("1.17.1")) {
                nmsVersion += "_2";
            }
            Class<?> clazz = Class.forName("me.nelonn.propack.bukkit.adapter.impl." + nmsVersion + ".PaperweightAdapter");
            if (Adapter.class.isAssignableFrom(clazz)) {
                loaded = (Adapter) clazz.getDeclaredConstructor().newInstance();
            } else {
                throw new IllegalArgumentException("Class '" + clazz.getName() + "' must implement '" + Adapter.class.getName() + "'");
            }
        } catch (Exception e) {
            loaded = null;
            e.printStackTrace();
            //throw new IllegalStateException("Unable to load bukkit adapter", e);
        }
        ADAPTER = loaded;
    }
}

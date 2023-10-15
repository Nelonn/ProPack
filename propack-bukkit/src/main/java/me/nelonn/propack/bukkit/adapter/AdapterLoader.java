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

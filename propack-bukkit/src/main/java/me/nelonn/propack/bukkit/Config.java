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

import me.nelonn.configlib.ConfigValue;
import me.nelonn.configlib.MiniMessageText;

public final class Config {
    public static final ConfigValue<String> dispatcherStore = new ConfigValue<>("dispatcher.store", "propack:memory_store");
    public static final ConfigValue<Boolean> dispatcherEnabled = new ConfigValue<>("dispatcher.enabled", true);
    public static final ConfigValue<Boolean> dispatcherReplace = new ConfigValue<>("dispatcher.replace", false);
    public static final ConfigValue<String> dispatcherPack = new ConfigValue<>("dispatcher.pack");
    public static final ConfigValue<Integer> dispatcherDelay = new ConfigValue<>("dispatcher.delay", 0);
    public static final ConfigValue<Boolean> dispatcherRequired = new ConfigValue<>("dispatcher.required", false);
    public static final ConfigValue<MiniMessageText> dispatcherPrompt = new ConfigValue<>("dispatcher.prompt", MiniMessageText.DESERIALIZER.deserialize("&#fa4943Accept the pack to enjoy a full experience"), MiniMessageText.DESERIALIZER);

    public static final ConfigValue<Boolean> devServerEnabled = new ConfigValue<>("dev_server.enabled", false);
    public static final ConfigValue<String> devServerHostIp = new ConfigValue<>("dev_server.host_ip", "127.0.0.1");
    public static final ConfigValue<Integer> devServerPort = new ConfigValue<>("dev_server.port", 3000);

    public static final ConfigValue<Boolean> patchPacketItems = new ConfigValue<>("patch_packets.items", true);
    public static final ConfigValue<Boolean> patchPacketSounds = new ConfigValue<>("patch_packets.sounds", true);

    private Config() {
        throw new UnsupportedOperationException();
    }
}

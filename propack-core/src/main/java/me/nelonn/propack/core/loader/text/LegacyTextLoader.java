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

package me.nelonn.propack.core.loader.text;

import com.google.gson.JsonObject;
import me.nelonn.propack.core.util.GsonHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class LegacyTextLoader implements TextLoader {
    public static final LegacyTextLoader INSTANCE = new LegacyTextLoader();

    private LegacyTextLoader() {
    }

    @Override
    public @NotNull String getType() {
        return "Legacy";
    }

    @Override
    public @NotNull Component load(@NotNull JsonObject rootObject) {
        String input = GsonHelper.getString(rootObject, "Text");
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }

}

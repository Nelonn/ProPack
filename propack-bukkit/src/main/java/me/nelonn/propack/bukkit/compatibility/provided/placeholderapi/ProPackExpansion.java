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

package me.nelonn.propack.bukkit.compatibility.provided.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.nelonn.propack.bukkit.ProPackPlugin;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ProPackExpansion extends PlaceholderExpansion {
    private final ProPackPlugin plugin;

    public static void register(@NotNull final ProPackPlugin plugin) {
        new ProPackExpansion(plugin).register();
    }

    public ProPackExpansion(final ProPackPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "propack";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nelonn";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(final OfflinePlayer player, @NotNull final String params) {
        /*final Glyph glyph = plugin.getFontManager().getGlyphFromName(params);
        if (glyph != null)
            return String.valueOf(glyph.getCharacter());*/
        return null; // Placeholder is unknown by the Expansion
    }
}

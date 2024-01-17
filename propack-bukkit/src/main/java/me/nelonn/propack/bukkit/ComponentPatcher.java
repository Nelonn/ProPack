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

import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.Font;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.entity.Player;

public class ComponentPatcher {

    public static Component patch(Component input, Player player) {
        ResourcePack resourcePack = ProPack.getAppliedResourcePack(player);
        if (resourcePack == null) return input;
        Resources resources = resourcePack.resources();
        return patch(input, resources);
    }

    public static Component patch(Component input, Resources resources) {
        input = input.children(input.children().stream().map(component -> patchSingle(component, resources)).toList());
        if (input instanceof TranslatableComponent translatableComponent) {
            input = translatableComponent.arguments(translatableComponent
                    .arguments().stream()
                    .map(argument -> patchSingle(argument.asComponent(), resources))
                    .toList());
        }
        return input;
    }

    public static Component patchSingle(Component input, Resources resources) {
        Style style = input.style();
        Key fontKey = style.font();
        if (fontKey == null) return input;
        Path fontPath = Path.of(fontKey.namespace(), fontKey.value());
        Font font = resources.font(fontPath);
        if (font == null) return input;
        Path realPath = font.realPath();
        if (fontPath.equals(realPath)) return input;
        return input.style(style.font(Key.key(realPath.namespace(), realPath.value())));
    }

}

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

package me.nelonn.propack.asset;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.definition.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class CombinedItemModel extends MultiItemModel {
    private final Set<String> elements;

    public CombinedItemModel(@NotNull ResourcePack resourcePack, @NotNull Path path, @NotNull Set<Item> targetItems,
                             @NotNull Path baseMesh, @NotNull Set<String> elements) {
        super(resourcePack, path, targetItems, baseMesh);
        this.elements = Collections.unmodifiableSet(elements);
    }

    public @NotNull Path getMesh(String... elements) {
        if (elements.length == 0) return baseMesh;
        StringBuilder sb = new StringBuilder();
        Arrays.stream(elements).sorted().forEach(s -> {
            if (!this.elements.contains(s)) {
                throw new IllegalArgumentException(this + " does not contain element '" + s + "'");
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(s);
        });
        String hex = Integer.toHexString(sb.toString().hashCode());
        return Path.of(path.getNamespace(), path.getValue() + '-' + hex);
    }

    public @NotNull Set<String> getElements() {
        return elements;
    }

    public boolean hasElement(@NotNull String element) {
        return elements.contains(element);
    }
}

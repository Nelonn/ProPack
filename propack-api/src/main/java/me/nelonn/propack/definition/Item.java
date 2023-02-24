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

package me.nelonn.propack.definition;

import me.nelonn.flint.path.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Item {
    private final Identifier id;
    private final boolean isBlock;

    public Item(@NotNull Identifier id, boolean isBlock) {
        this.id = id;
        this.isBlock = isBlock;
    }

    public @NotNull Identifier getId() {
        return id;
    }

    public boolean isBlock() {
        return isBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return isBlock == item.isBlock && Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isBlock);
    }
}

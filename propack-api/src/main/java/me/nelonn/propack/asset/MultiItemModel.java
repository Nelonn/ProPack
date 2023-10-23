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

package me.nelonn.propack.asset;

import me.nelonn.flint.path.Identifier;
import me.nelonn.flint.path.Path;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class MultiItemModel extends AbstractItemModel {
    protected final Path baseMesh;

    protected MultiItemModel(@NotNull Path path, @NotNull Set<Identifier> targetItems, @NotNull Path baseMesh) {
        super(path, targetItems);
        this.baseMesh = baseMesh;
    }

    public @NotNull Path getBaseMesh() {
        return baseMesh;
    }
}

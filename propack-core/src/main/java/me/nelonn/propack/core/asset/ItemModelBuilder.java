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

package me.nelonn.propack.core.asset;

import me.nelonn.flint.path.Key;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.ItemModel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class ItemModelBuilder extends AbstractAssetBuilder<ItemModel> {
    protected Set<Key> targetItems;

    protected ItemModelBuilder(@NotNull Path path) {
        super(path);
    }

    public Set<Key> getTargetItems() {
        return targetItems;
    }

    public ItemModelBuilder setTargetItems(Set<Key> targetItems) {
        this.targetItems = targetItems;
        return this;
    }
}

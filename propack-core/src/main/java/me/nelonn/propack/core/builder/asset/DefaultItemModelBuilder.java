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

package me.nelonn.propack.core.builder.asset;

import me.nelonn.flint.path.Key;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.DefaultItemModel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DefaultItemModelBuilder extends ItemModelBuilder {
    private Path mesh;

    public DefaultItemModelBuilder(@NotNull Path path) {
        super(path);
    }

    @Override
    public DefaultItemModelBuilder setTargetItems(Set<Key> targetItems) {
        return (DefaultItemModelBuilder) super.setTargetItems(targetItems);
    }

    public Path getMesh() {
        return mesh;
    }

    public DefaultItemModelBuilder setMesh(Path mesh) {
        this.mesh = mesh;
        return this;
    }

    public @NotNull DefaultItemModel build() {
        return new DefaultItemModel(path, targetItems, mesh);
    }
}

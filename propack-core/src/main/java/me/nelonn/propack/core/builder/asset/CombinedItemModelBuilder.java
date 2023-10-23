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

import me.nelonn.flint.path.Identifier;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.CombinedItemModel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CombinedItemModelBuilder extends ItemModelBuilder {
    private Path mesh;
    private Set<String> elements;

    public CombinedItemModelBuilder(@NotNull Path path) {
        super(path);
    }

    @Override
    public CombinedItemModelBuilder setTargetItems(Set<Identifier> targetItems) {
        return (CombinedItemModelBuilder) super.setTargetItems(targetItems);
    }

    public Path getMesh() {
        return mesh;
    }

    public CombinedItemModelBuilder setMesh(Path mesh) {
        this.mesh = mesh;
        return this;
    }

    public Set<String> getElements() {
        return elements;
    }

    public CombinedItemModelBuilder setElements(Set<String> elements) {
        this.elements = elements;
        return this;
    }

    public @NotNull CombinedItemModel build() {
        return new CombinedItemModel(path, targetItems, mesh, elements);
    }
}

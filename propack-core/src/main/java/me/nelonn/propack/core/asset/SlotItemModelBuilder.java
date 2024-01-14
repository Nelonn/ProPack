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
import me.nelonn.propack.asset.SlotItemModel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class SlotItemModelBuilder extends ItemModelBuilder {
    private Path mesh;
    private Map<String, SlotItemModel.Slot> slots;

    public SlotItemModelBuilder(@NotNull Path path) {
        super(path);
    }

    @Override
    public SlotItemModelBuilder setTargetItems(Set<Key> targetItems) {
        return (SlotItemModelBuilder) super.setTargetItems(targetItems);
    }

    public Path getMesh() {
        return mesh;
    }

    public SlotItemModelBuilder setMesh(Path mesh) {
        this.mesh = mesh;
        return this;
    }

    public Map<String, SlotItemModel.Slot> getSlots() {
        return slots;
    }

    public SlotItemModelBuilder setSlots(Map<String, SlotItemModel.Slot> slots) {
        this.slots = slots;
        return this;
    }

    public @NotNull SlotItemModel build() {
        return new SlotItemModel(path, targetItems, mesh, slots);
    }
}

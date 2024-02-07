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

package me.nelonn.propack.builder.impl.json.mesh;

import me.nelonn.bestvecs.Vec3f;
import me.nelonn.propack.core.util.Direction;
import org.jetbrains.annotations.NotNull;

public class ModelRotation {
    public Vec3f origin;
    public Direction.Axis axis;
    public float angle;
    public boolean rescale;

    public ModelRotation(@NotNull Vec3f origin, @NotNull Direction.Axis axis, float angle, boolean rescale) {
        this.origin = origin.immutable();
        this.axis = axis;
        this.angle = angle;
        this.rescale = rescale;
    }
}
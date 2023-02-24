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

package me.nelonn.propack.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Vec3f {
    public static final Vec3f NEGATIVE_X = new Vec3f(-1.0F, 0.0F, 0.0F);
    public static final Vec3f POSITIVE_X = new Vec3f(1.0F, 0.0F, 0.0F);
    public static final Vec3f NEGATIVE_Y = new Vec3f(0.0F, -1.0F, 0.0F);
    public static final Vec3f POSITIVE_Y = new Vec3f(0.0F, 1.0F, 0.0F);
    public static final Vec3f NEGATIVE_Z = new Vec3f(0.0F, 0.0F, -1.0F);
    public static final Vec3f POSITIVE_Z = new Vec3f(0.0F, 0.0F, 1.0F);
    public static final Vec3f ZERO = new Vec3f(0.0F, 0.0F, 0.0F);

    private float x;
    private float y;
    private float z;

    public Vec3f() {
    }

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public Vec3f scale(float scale) {
        return new Vec3f(x * scale, y * scale, z * scale);
    }

    public Vec3f add(float x, float y, float z) {
        return new Vec3f(this.x + x, this.y + y, this.z + z);
    }

    public Vec3f add(@NotNull Vec3f vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vec3f subtract(float x, float y, float z) {
        return new Vec3f(this.x - x, this.y - y, this.z - z);
    }

    public Vec3f subtract(@NotNull Vec3f vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec3f vec3f = (Vec3f) o;
        return Float.compare(vec3f.x, x) == 0 && Float.compare(vec3f.y, y) == 0 && Float.compare(vec3f.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return '[' + this.x + ", " + this.y + ", " + this.z + ']';
    }
}
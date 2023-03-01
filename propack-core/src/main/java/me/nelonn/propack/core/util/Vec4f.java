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

public final class Vec4f {
    public static final Vec4f ZERO = new Vec4f(0.0F, 0.0F, 0.0F, 0.0F);

    private final float x;
    private final float y;
    private final float z;
    private final float w;

    public Vec4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }

    public @NotNull Vec4f scale(float scale) {
        return new Vec4f(x * scale, y * scale, z * scale, w * scale);
    }

    public @NotNull Vec4f add(float x, float y, float z, float w) {
        return new Vec4f(this.x + x, this.y + y, this.z + z, this.w + w);
    }

    public @NotNull Vec4f add(@NotNull Vec4f vec) {
        return add(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
    }

    public @NotNull Vec4f subtract(float x, float y, float z, float w) {
        return new Vec4f(this.x - x, this.y - y, this.z - z, this.w - w);
    }

    public @NotNull Vec4f subtract(@NotNull Vec4f vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec4f vec4f = (Vec4f) o;
        return Float.compare(vec4f.x, x) == 0 && Float.compare(vec4f.y, y) == 0 &&
                Float.compare(vec4f.z, z) == 0 && Float.compare(vec4f.w, w) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }

    @Override
    public String toString() {
        return '[' + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ']';
    }
}
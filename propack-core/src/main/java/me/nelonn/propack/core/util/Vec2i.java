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

package me.nelonn.propack.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Vec2i {
    public static final Vec2i ZERO = new Vec2i(0, 0);

    private final int x;
    private final int y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public @NotNull Vec2i scale(int scale) {
        return new Vec2i(x * scale, y * scale);
    }

    public @NotNull Vec2i add(int x, int y) {
        return new Vec2i(this.x + x, this.y + y);
    }

    public @NotNull Vec2i add(@NotNull Vec2i vec) {
        return add(vec.getX(), vec.getY());
    }

    public @NotNull Vec2i subtract(int x, int y) {
        return new Vec2i(this.x - x, this.y - y);
    }

    public @NotNull Vec2i subtract(@NotNull Vec2i vec) {
        return subtract(vec.getX(), vec.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2i vec2i = (Vec2i) o;
        return x == vec2i.x && y == vec2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return '[' + this.x + ", " + this.y + ']';
    }
}

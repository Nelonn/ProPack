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

package me.nelonn.propack.builder.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Extra<T> {
    private final Class<T> type;
    private final String name;

    public Extra(final @NotNull Class<T> type, final @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public Extra(final @NotNull String name) { // experimental
        this.type = (Class<T>) getClass().getGenericSuperclass().getClass();
        this.name = name;
    }

    public @NotNull Class<T> getType() {
        return type;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Extra<?> extra = (Extra<?>) o;
        return Objects.equals(type, extra.type) && Objects.equals(name, extra.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
}

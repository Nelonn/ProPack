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

package me.nelonn.propack.builder.task;

import me.nelonn.propack.builder.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractTask implements Task {
    private final String name;
    private final Project project;

    protected AbstractTask(@NotNull String name, @NotNull Project project) {
        this.name = name;
        this.project = project;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Project getProject() {
        return project;
    }

    @Override
    public String toString() {
        return project.getName() + ':' + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTask that = (AbstractTask) o;
        return Objects.equals(name, that.name) && Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, project);
    }
}

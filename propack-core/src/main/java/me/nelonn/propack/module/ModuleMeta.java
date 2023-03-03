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

package me.nelonn.propack.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ModuleMeta {

    @NotNull String getName();

    default @NotNull String getDisplayName() {
        return getName() + " v" + getVersion();
    }

    @NotNull String getBootstrapper();

    @NotNull String getVersion();

    @Nullable String getDescription();

    @NotNull List<String> getAuthors();

    @NotNull List<String> getContributors();

    @Nullable String getWebsite();

    @Nullable String getLoggerPrefix();

}

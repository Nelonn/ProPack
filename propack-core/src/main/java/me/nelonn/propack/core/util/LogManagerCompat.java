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

import com.google.common.base.Throwables;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class LogManagerCompat {

    public static String FORCE_LOGGER_NAME = null;

    public static @NotNull Logger getLogger() {
        return LoggerFactory.getLogger(FORCE_LOGGER_NAME != null ? FORCE_LOGGER_NAME : getCallerCallerClassName());
    }

    @SuppressWarnings("deprecation")
    private static String getCallerCallerClassName() {
        List<StackTraceElement> lazyStack = Throwables.lazyStackTrace(new Throwable());
        // 0 - this method
        // 1 - caller
        // 2 - caller caller
        return lazyStack.get(2).getClassName();
    }

    private LogManagerCompat() {
        throw new UnsupportedOperationException();
    }
}

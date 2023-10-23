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

import me.nelonn.flint.path.Path;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class PathUtil {
    public static @NotNull Path resolve(@NotNull String input, @NotNull String curNamespace, @NotNull String curPath) {
        input = input.replace("<namespace>", curNamespace);
        if (!input.startsWith("./") && !input.startsWith("../")) {
            return Path.ofWithFallback(input, curNamespace);
        }
        List<String> path = new ArrayList<>(Arrays.asList(curPath.split("/")));
        String[] relative = input.split("/");
        for (String s : relative) {
            if (s.equals("..")) {
                path.remove(path.size() - 1);
            } else if (!s.isEmpty() && !s.equals(".")) {
                path.add(s);
            }
        }
        return Path.of(curNamespace, String.join("/", path));
    }

    public static @NotNull Path resolve(@NotNull String input, @NotNull Path contentPath) {
        return resolve(input, contentPath.getNamespace(), parentDirectory(contentPath));
    }

    public static @NotNull String parentDirectory(@NotNull String filePath) {
        return filePath.lastIndexOf('/') == -1 ? "" : filePath.substring(0, filePath.lastIndexOf('/'));
    }

    public static @NotNull String parentDirectory(@NotNull Path filePath) {
        return parentDirectory(filePath.getValue());
    }

    public static @NotNull Path resourcePath(@NotNull String input) {
        String[] pathSplit = input.substring("content/".length()).split("/", 2);
        if (pathSplit.length == 1) {
            throw new IllegalArgumentException("Invalid path " + input);
        }
        return Path.of(pathSplit[0], pathSplit[1]);
    }

    public static @NotNull Path resourcePath(@NotNull String input, @NotNull String extension) {
        return resourcePath(Util.substringLast(input, extension));
    }

    public static @NotNull String contentPath(@NotNull Path path) {
        return "content/" + path.getNamespace() + '/' + path.getValue();
    }

    public static @NotNull String assetsPath(@NotNull Path path, @NotNull String type) {
        return "assets/" + path.getNamespace() + '/' + type + '/' + path.getValue();
    }

    public static @NotNull Path append(@NotNull Path path, @NotNull String string) {
        return Path.of(path.getNamespace(), path.getValue() + string);
    }

    public static @NotNull String format(@NotNull String path) {
        path = path.toLowerCase(Locale.ROOT);
        path = path.replace("\\", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = Util.substringLast(path, 1);
        }
        return path;
    }

    public static @NotNull String join(String first, String... more) {
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isEmpty()) {
            sb.append(format(first));
        }
        for (String string : more) {
            if (string == null || string.isEmpty()) continue;
            if (sb.length() > 0) {
                sb.append("/");
            }
            sb.append(format(string));
        }
        return sb.toString();
    }

    private PathUtil() {
        throw new UnsupportedOperationException();
    }
}

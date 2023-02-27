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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.nelonn.propack.builder.task.TaskIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;

public final class Util {

    public static @NotNull Color hexToRGB(@NotNull String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return new Color(
                Integer.valueOf(hex.substring(0, 2), 16),
                Integer.valueOf(hex.substring(2, 4), 16),
                Integer.valueOf(hex.substring(4, 6), 16));
    }

    public static Vec2i parseVector2i(@NotNull JsonObject json, @NotNull String key, @Nullable Vec2i fallback) {
        if (!json.has(key)) return fallback;
        JsonArray jsonArray = GsonHelper.getArray(json, key);
        if (jsonArray.size() != 2) {
            throw new JsonParseException("Expected 2 '" + key + "' values, found: " + jsonArray.size());
        }
        int[] arr = new int[2];
        for(int i = 0; i < arr.length; ++i) {
            arr[i] = GsonHelper.asInt(jsonArray.get(i), key + '[' + i + ']');
        }
        return new Vec2i(arr[0], arr[1]);
    }

    public static Vec3f parseVector3f(@NotNull JsonObject json, @NotNull String key, @Nullable Vec3f fallback) {
        if (!json.has(key)) return fallback;
        JsonArray jsonArray = GsonHelper.getArray(json, key);
        if (jsonArray.size() != 3) {
            throw new JsonParseException("Expected 3 '" + key + "' values, found: " + jsonArray.size());
        }
        float[] arr = new float[3];
        for(int i = 0; i < arr.length; ++i) {
            arr[i] = GsonHelper.asFloat(jsonArray.get(i), key + '[' + i + ']');
        }
        return new Vec3f(arr[0], arr[1], arr[2]);
    }

    public static @NotNull JsonArray serializeVector2i(@NotNull Vec2i vec2i) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(vec2i.getX());
        jsonArray.add(vec2i.getY());
        return jsonArray;
    }

    public static @NotNull JsonArray serializeVector3f(@NotNull Vec3f vec3f) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(vec3f.getX());
        jsonArray.add(vec3f.getY());
        jsonArray.add(vec3f.getZ());
        return jsonArray;
    }

    public static @NotNull String substringLast(@NotNull String string, int size) {
        return string.substring(0, string.length() - size);
    }

    public static @NotNull String substringLast(@NotNull String string, @NotNull String cut) {
        return substringLast(string, cut.length());
    }

    public static @NotNull File tempFile(@NotNull TaskIO io, @NotNull String path) {
        File file = new File(io.getTempDirectory(), path);
        file.getParentFile().mkdirs();
        return file;
    }

    /*@SuppressWarnings("unchecked")
    public static @NotNull Hosting provideHosting(@NotNull String string) {
        try {
            Class<? extends Hosting> clazz = (Class<? extends Hosting>) Class.forName(string);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class '" + string + "' not found");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Class '" + string + "' must implement '" + Hosting.class.getName() + "'");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class '" + string + "' has no constructor without arguments");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }*/

    private Util() {
        throw new UnsupportedOperationException();
    }
}

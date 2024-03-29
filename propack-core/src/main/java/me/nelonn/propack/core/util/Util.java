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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.nelonn.bestvecs.ImmVec3f;
import me.nelonn.bestvecs.Vec3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Consumer;

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

    public static Vec2i parseVec2i(@NotNull JsonObject json, @NotNull String key, @Nullable Vec2i fallback) {
        if (!json.has(key)) return fallback;
        JsonArray jsonArray = GsonHelper.getArray(json, key);
        if (jsonArray.size() != 2) {
            throw new JsonParseException("Expected 2 '" + key + "' values, found: " + jsonArray.size());
        }
        int[] arr = new int[2];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = GsonHelper.asInt(jsonArray.get(i), key + '[' + i + ']');
        }
        return new Vec2i(arr[0], arr[1]);
    }

    public static ImmVec3f parseVec3f(@NotNull JsonObject json, @NotNull String key) {
        JsonArray jsonArray = GsonHelper.getArray(json, key);
        if (jsonArray.size() != 3) {
            throw new JsonParseException("Expected 3 '" + key + "' values, found: " + jsonArray.size());
        }
        float[] arr = new float[3];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = GsonHelper.asFloat(jsonArray.get(i), key + '[' + i + ']');
        }
        return Vec3f.immutable(arr[0], arr[1], arr[2]);
    }

    public static ImmVec3f parseVec3f(@NotNull JsonObject json, @NotNull String key, @Nullable ImmVec3f fallback) {
        if (!json.has(key)) return fallback;
        return parseVec3f(json, key);
    }

    public static Vec4f parseVec4f(@NotNull JsonObject json, @NotNull String key) {
        JsonArray jsonArray = GsonHelper.getArray(json, key);
        if (jsonArray.size() != 4) {
            throw new JsonParseException("Expected 4 '" + key + "' values, found: " + jsonArray.size());
        }
        float[] arr = new float[4];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = GsonHelper.asFloat(jsonArray.get(i), key + '[' + i + ']');
        }
        return new Vec4f(arr[0], arr[1], arr[2], arr[3]);
    }

    public static @NotNull JsonArray serializeVec2i(@NotNull Vec2i vec2i) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(vec2i.getX());
        jsonArray.add(vec2i.getY());
        return jsonArray;
    }

    public static @NotNull JsonArray serializeVec3f(@NotNull Vec3f vec3f) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(vec3f.x());
        jsonArray.add(vec3f.y());
        jsonArray.add(vec3f.z());
        return jsonArray;
    }

    public static @NotNull JsonArray serializeVec4f(@NotNull Vec4f vec4f) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(vec4f.getX());
        jsonArray.add(vec4f.getY());
        jsonArray.add(vec4f.getZ());
        jsonArray.add(vec4f.getW());
        return jsonArray;
    }

    public static void forEachStringArray(@NotNull JsonArray input, @NotNull String name, @NotNull Consumer<String> output) {
        for (int i = 0; i < input.size(); i++) {
            JsonElement element = input.get(i);
            output.accept(GsonHelper.asString(element, name + '[' + i + ']'));
        }
    }

    public static @NotNull String substringLast(@NotNull String string, int size) {
        return string.substring(0, string.length() - size);
    }

    public static @NotNull String substringLast(@NotNull String string, @NotNull String cut) {
        return substringLast(string, cut.length());
    }

    private Util() {
        throw new UnsupportedOperationException();
    }
}

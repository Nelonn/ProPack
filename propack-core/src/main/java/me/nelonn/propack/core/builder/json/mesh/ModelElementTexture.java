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

package me.nelonn.propack.core.builder.json.mesh;

import com.google.gson.*;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.Util;
import me.nelonn.propack.core.util.Vec4f;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class ModelElementTexture {
    private Vec4f uv;
    public final int rotation;

    public ModelElementTexture(@Nullable Vec4f uv, int rotation) {
        this.uv = uv;
        this.rotation = rotation;
    }

    public float getU(int rotation) {
        if (this.uv == null) {
            throw new NullPointerException("uv");
        } else {
            int i = getRotatedUVIndex(rotation);
            return i != 0 && i != 1 ? uv.getZ() : uv.getX();
        }
    }

    public float getV(int rotation) {
        if (this.uv == null) {
            throw new NullPointerException("uv");
        } else {
            int i = getRotatedUVIndex(rotation);
            return i != 0 && i != 3 ? uv.getW() : uv.getY();
        }
    }

    private int getRotatedUVIndex(int rotation) {
        return (rotation + this.rotation / 90) % 4;
    }

    public int getDirectionIndex(int offset) {
        return (offset + 4 - rotation / 90) % 4;
    }

    public Vec4f getUV() {
        return uv;
    }

    public void setUV(Vec4f uv) {
        if (this.uv == null) {
            this.uv = uv;
        }
    }

    protected static class Deserializer implements JsonDeserializer<ModelElementTexture>, JsonSerializer<ModelElementTexture> {
        protected Deserializer() {
        }

        public ModelElementTexture deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vec4f uv = jsonObject.has("uv") ? Util.parseVec4f(jsonObject, "uv") : null;
            int i = deserializeRotation(jsonObject);
            return new ModelElementTexture(uv, i);
        }

        protected int deserializeRotation(JsonObject object) {
            int i = GsonHelper.getInt(object, "rotation", 0);
            if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
                return i;
            } else {
                throw new JsonParseException("Invalid rotation " + i + " found, only 0/90/180/270 allowed");
            }
        }

        public JsonElement serialize(ModelElementTexture modelElementTexture, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("uv", Util.serializeVec4f(modelElementTexture.getUV()));
            if (modelElementTexture.rotation != 0) {
                jsonObject.addProperty("rotation", modelElementTexture.rotation);
            }
            return jsonObject;
        }
    }
}
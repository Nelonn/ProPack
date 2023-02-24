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

package me.nelonn.propack.core.builder.json.mesh;

import com.google.gson.*;
import me.nelonn.propack.core.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class ModelElementTexture {
    public float[] uvs;
    public final int rotation;

    public ModelElementTexture(@Nullable float[] uvs, int rotation) {
        this.uvs = uvs;
        this.rotation = rotation;
    }

    public float getU(int rotation) {
        if (this.uvs == null) {
            throw new NullPointerException("uvs");
        } else {
            int i = this.getRotatedUVIndex(rotation);
            return this.uvs[i != 0 && i != 1 ? 2 : 0];
        }
    }

    public float getV(int rotation) {
        if (this.uvs == null) {
            throw new NullPointerException("uvs");
        } else {
            int i = this.getRotatedUVIndex(rotation);
            return this.uvs[i != 0 && i != 3 ? 3 : 1];
        }
    }

    private int getRotatedUVIndex(int rotation) {
        return (rotation + this.rotation / 90) % 4;
    }

    public int getDirectionIndex(int offset) {
        return (offset + 4 - this.rotation / 90) % 4;
    }

    public void setUvs(float[] uvs) {
        if (this.uvs == null) {
            this.uvs = uvs;
        }

    }

    protected static class Deserializer implements JsonDeserializer<ModelElementTexture>, JsonSerializer<ModelElementTexture> {
        private static final int DEFAULT_ROTATION = 0;

        protected Deserializer() {
        }

        public ModelElementTexture deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            float[] fs = this.deserializeUVs(jsonObject);
            int i = this.deserializeRotation(jsonObject);
            return new ModelElementTexture(fs, i);
        }

        protected int deserializeRotation(JsonObject object) {
            int i = GsonHelper.getInt(object, "rotation", 0);
            if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
                return i;
            } else {
                throw new JsonParseException("Invalid rotation " + i + " found, only 0/90/180/270 allowed");
            }
        }

        @Nullable
        private float[] deserializeUVs(JsonObject object) {
            if (!object.has("uv")) {
                return null;
            } else {
                JsonArray jsonArray = GsonHelper.getArray(object, "uv");
                if (jsonArray.size() != 4) {
                    throw new JsonParseException("Expected 4 uv values, found: " + jsonArray.size());
                } else {
                    float[] fs = new float[4];

                    for(int i = 0; i < fs.length; ++i) {
                        fs[i] = GsonHelper.asFloat(jsonArray.get(i), "uv[" + i + "]");
                    }

                    return fs;
                }
            }
        }

        public JsonElement serialize(ModelElementTexture modelElementTexture, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(modelElementTexture.uvs[0]);
            jsonArray.add(modelElementTexture.uvs[1]);
            jsonArray.add(modelElementTexture.uvs[2]);
            jsonArray.add(modelElementTexture.uvs[3]);
            jsonObject.add("uv", jsonArray);
            if (modelElementTexture.rotation != 0) {
                jsonObject.addProperty("rotation", modelElementTexture.rotation);
            }
            return jsonObject;
        }
    }
}
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
import me.nelonn.propack.core.util.Direction;
import me.nelonn.propack.core.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class ModelElementFace {
    public final Direction cullFace;
    public final int tintIndex;
    public final String textureId;
    public final ModelElementTexture textureData;

    public ModelElementFace(@Nullable Direction cullFace, int tintIndex, String textureId, ModelElementTexture textureData) {
        this.cullFace = cullFace;
        this.tintIndex = tintIndex;
        this.textureId = textureId;
        this.textureData = textureData;
    }

    protected static class Deserializer implements JsonDeserializer<ModelElementFace>, JsonSerializer<ModelElementFace> {
        protected Deserializer() {
        }

        public ModelElementFace deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Direction direction = deserializeCullFace(jsonObject);
            int i = deserializeTintIndex(jsonObject);
            String string = deserializeTexture(jsonObject);
            ModelElementTexture modelElementTexture = ctx.deserialize(jsonObject, ModelElementTexture.class);
            return new ModelElementFace(direction, i, string, modelElementTexture);
        }

        protected int deserializeTintIndex(JsonObject object) {
            return GsonHelper.getInt(object, "tintindex", -1);
        }

        private String deserializeTexture(JsonObject object) {
            return GsonHelper.getString(object, "texture");
        }

        @Nullable
        private Direction deserializeCullFace(JsonObject object) {
            String string = GsonHelper.getString(object, "cullface", "");
            return Direction.byName(string);
        }

        @Override
        public JsonElement serialize(ModelElementFace modelElementFace, Type type, JsonSerializationContext ctx) {
            JsonObject jsonObject = ctx.serialize(modelElementFace.textureData).getAsJsonObject();
            if (modelElementFace.cullFace != null) {
                jsonObject.addProperty("cullface", modelElementFace.cullFace.name().toLowerCase());
            }
            if (modelElementFace.tintIndex != -1) {
                jsonObject.addProperty("tintindex", modelElementFace.tintIndex);
            }
            jsonObject.addProperty("texture", modelElementFace.textureId);
            return jsonObject;
        }
    }
}
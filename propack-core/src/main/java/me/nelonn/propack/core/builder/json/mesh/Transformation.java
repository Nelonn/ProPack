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
import me.nelonn.propack.core.util.Util;
import me.nelonn.propack.core.util.Vec3f;

import java.lang.reflect.Type;
import java.util.Objects;

public class Transformation {
    public static final Transformation IDENTITY = new Transformation(Vec3f.ZERO, Vec3f.ZERO, new Vec3f(1.0F, 1.0F, 1.0F));
    public final Vec3f rotation;
    public final Vec3f translation;
    public final Vec3f scale;

    public Transformation(Vec3f rotation, Vec3f translation, Vec3f scale) {
        this.rotation = rotation;
        this.translation = translation;
        this.scale = scale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transformation that = (Transformation) o;
        return Objects.equals(rotation, that.rotation) && Objects.equals(translation, that.translation) && Objects.equals(scale, that.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotation, translation, scale);
    }

    protected static class Deserializer implements JsonDeserializer<Transformation>, JsonSerializer<Transformation> {
        private static final Vec3f DEFAULT_ROTATION = new Vec3f(0.0F, 0.0F, 0.0F);
        private static final Vec3f DEFAULT_TRANSLATION = new Vec3f(0.0F, 0.0F, 0.0F);
        private static final Vec3f DEFAULT_SCALE = new Vec3f(1.0F, 1.0F, 1.0F);

        protected Deserializer() {
        }

        public Transformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vec3f rotationVec = Util.parseVec3f(jsonObject, "rotation", DEFAULT_ROTATION);
            Vec3f translationVec = Util.parseVec3f(jsonObject, "translation", DEFAULT_TRANSLATION);
            Vec3f scaleVec = Util.parseVec3f(jsonObject, "scale", DEFAULT_SCALE);
            return new Transformation(rotationVec, translationVec, scaleVec);
        }

        public JsonElement serialize(Transformation transformation, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (transformation.rotation != DEFAULT_ROTATION) {
                jsonObject.add("rotation", Util.serializeVec3f(transformation.rotation));
            }
            if (transformation.translation != DEFAULT_TRANSLATION) {
                jsonObject.add("translation", Util.serializeVec3f(transformation.translation));
            }
            if (transformation.scale != DEFAULT_SCALE) {
                jsonObject.add("scale", Util.serializeVec3f(transformation.scale));
            }
            return jsonObject;
        }
    }
}
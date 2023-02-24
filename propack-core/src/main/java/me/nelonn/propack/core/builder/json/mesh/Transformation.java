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

public class Transformation {
    public static final Transformation IDENTITY = new Transformation(new Vec3f(), new Vec3f(), new Vec3f(1.0F, 1.0F, 1.0F));
    public final Vec3f rotation;
    public final Vec3f translation;
    public final Vec3f scale;

    public Transformation(Vec3f rotation, Vec3f translation, Vec3f scale) {
        this.rotation = rotation;
        this.translation = translation;
        this.scale = scale;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (this.getClass() != o.getClass()) {
            return false;
        } else {
            Transformation transformation = (Transformation)o;
            return this.rotation.equals(transformation.rotation) && this.scale.equals(transformation.scale) && this.translation.equals(transformation.translation);
        }
    }

    public int hashCode() {
        int i = this.rotation.hashCode();
        i = 31 * i + this.translation.hashCode();
        i = 31 * i + this.scale.hashCode();
        return i;
    }

    protected static class Deserializer implements JsonDeserializer<Transformation>, JsonSerializer<Transformation> {
        private static final Vec3f DEFAULT_ROTATION = new Vec3f(0.0F, 0.0F, 0.0F);
        private static final Vec3f DEFAULT_TRANSLATION = new Vec3f(0.0F, 0.0F, 0.0F);
        private static final Vec3f DEFAULT_SCALE = new Vec3f(1.0F, 1.0F, 1.0F);

        protected Deserializer() {
        }

        public Transformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vec3f rotationVec = Util.parseVector3f(jsonObject, "rotation", DEFAULT_ROTATION);
            Vec3f translationVec = Util.parseVector3f(jsonObject, "translation", DEFAULT_TRANSLATION);
            //translationVec.scale(0.0625F);
            //translationVec.clamp(-5.0F, 5.0F);
            Vec3f scaleVec = Util.parseVector3f(jsonObject, "scale", DEFAULT_SCALE);
            //scaleVec.clamp(-4.0F, 4.0F);
            return new Transformation(rotationVec, translationVec, scaleVec);
        }

        public JsonElement serialize(Transformation transformation, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (transformation.rotation != DEFAULT_ROTATION) {
                jsonObject.add("rotation", Util.serializeVector3f(transformation.rotation));
            }
            if (transformation.translation != DEFAULT_TRANSLATION) {
                jsonObject.add("translation", Util.serializeVector3f(transformation.translation));
            }
            if (transformation.scale != DEFAULT_SCALE) {
                jsonObject.add("scale", Util.serializeVector3f(transformation.scale));
            }
            return jsonObject;
        }
    }
}
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

package me.nelonn.propack.builder.impl.json.mesh;

import com.google.gson.*;
import me.nelonn.bestvecs.ImmVec3f;
import me.nelonn.bestvecs.Vec3f;
import me.nelonn.propack.core.util.Util;

import java.lang.reflect.Type;
import java.util.Objects;

public class Transformation {
    public static final Transformation IDENTITY = new Transformation(ImmVec3f.ZERO, ImmVec3f.ZERO, ImmVec3f.ONE);
    public final ImmVec3f rotation;
    public final ImmVec3f translation;
    public final ImmVec3f scale;

    public Transformation(Vec3f rotation, Vec3f translation, Vec3f scale) {
        this.rotation = rotation.immutable();
        this.translation = translation.immutable();
        this.scale = scale.immutable();
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
        private static final ImmVec3f DEFAULT_ROTATION = ImmVec3f.ZERO;
        private static final ImmVec3f DEFAULT_TRANSLATION = ImmVec3f.ZERO;
        private static final ImmVec3f DEFAULT_SCALE = ImmVec3f.ONE;

        protected Deserializer() {
        }

        public Transformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ImmVec3f rotationVec = Util.parseVec3f(jsonObject, "rotation", DEFAULT_ROTATION);
            ImmVec3f translationVec = Util.parseVec3f(jsonObject, "translation", DEFAULT_TRANSLATION);
            ImmVec3f scaleVec = Util.parseVec3f(jsonObject, "scale", DEFAULT_SCALE);
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
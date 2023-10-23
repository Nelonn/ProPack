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

import com.google.common.collect.Maps;
import com.google.gson.*;
import me.nelonn.bestvecs.ImmVec3f;
import me.nelonn.bestvecs.Vec3f;
import me.nelonn.propack.core.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class ModelElement {
    // TODO: fix
    public /*final*/ Vec3f from;
    public /*final*/ Vec3f to;
    public final Map<Direction, ModelElementFace> faces;
    public final ModelRotation rotation;
    public final boolean shade;

    public ModelElement(@NotNull Vec3f from, @NotNull Vec3f to, @NotNull Map<Direction, ModelElementFace> faces,
                        @Nullable ModelRotation rotation, boolean shade) {
        this.from = from.immutable();
        this.to = to.immutable();
        this.faces = new EnumMap<>(faces);
        this.rotation = rotation;
        this.shade = shade;
        this.initTextures();
    }

    private void initTextures() {
        for (Map.Entry<Direction, ModelElementFace> entry : this.faces.entrySet()) {
            float[] fs = getRotatedMatrix(entry.getKey());
            Vec4f uv = new Vec4f(fs[0], fs[1], fs[2], fs[3]);
            entry.getValue().textureData.setUV(uv);
        }
    }

    private float[] getRotatedMatrix(Direction direction) {
        switch (direction) {
            case DOWN:
                return new float[]{this.from.x(), 16.0F - this.to.z(), this.to.x(), 16.0F - this.from.z()};
            case UP:
                return new float[]{this.from.x(), this.from.z(), this.to.x(), this.to.z()};
            case NORTH:
            default:
                return new float[]{16.0F - this.to.x(), 16.0F - this.to.y(), 16.0F - this.from.x(), 16.0F - this.from.y()};
            case SOUTH:
                return new float[]{this.from.x(), 16.0F - this.to.y(), this.to.x(), 16.0F - this.from.y()};
            case WEST:
                return new float[]{this.from.x(), 16.0F - this.to.y(), this.to.z(), 16.0F - this.from.y()};
            case EAST:
                return new float[]{16.0F - this.to.z(), 16.0F - this.to.y(), 16.0F - this.from.z(), 16.0F - this.from.y()};
        }
    }

    protected static class Deserializer implements JsonDeserializer<ModelElement>, JsonSerializer<ModelElement> {
        protected Deserializer() {
        }

        public ModelElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vec3f vec3f = deserializeFrom(jsonObject);
            Vec3f vec3f2 = deserializeTo(jsonObject);
            ModelRotation modelRotation = deserializeRotation(jsonObject);
            Map<Direction, ModelElementFace> map = deserializeFacesValidating(jsonDeserializationContext, jsonObject);
            if (jsonObject.has("shade") && !GsonHelper.hasBoolean(jsonObject, "shade")) {
                throw new JsonParseException("Expected shade to be a Boolean");
            } else {
                boolean bl = GsonHelper.getBoolean(jsonObject, "shade", true);
                return new ModelElement(vec3f, vec3f2, map, modelRotation, bl);
            }
        }

        @Nullable
        private ModelRotation deserializeRotation(JsonObject object) {
            ModelRotation modelRotation = null;
            if (object.has("rotation")) {
                JsonObject jsonObject = GsonHelper.getObject(object, "rotation");
                Vec3f vec3f = Util.parseVec3f(jsonObject, "origin");
                //vec3f.scale(0.0625F); these classes copied from mojang minecraft client, this line is unused
                Direction.Axis axis = deserializeAxis(jsonObject);
                float f = deserializeRotationAngle(jsonObject);
                boolean bl = GsonHelper.getBoolean(jsonObject, "rescale", false);
                modelRotation = new ModelRotation(vec3f, axis, f, bl);
            }

            return modelRotation;
        }

        private float deserializeRotationAngle(JsonObject object) {
            float f = GsonHelper.getFloat(object, "angle");
            if (f != 0.0F && Math.abs(f) != 22.5F && Math.abs(f) != 45.0F) {
                throw new JsonParseException("Invalid rotation " + f + " found, only -45/-22.5/0/22.5/45 allowed");
            } else {
                return f;
            }
        }

        private Direction.Axis deserializeAxis(JsonObject object) {
            String string = GsonHelper.getString(object, "axis");
            Direction.Axis axis = Direction.Axis.fromName(string.toLowerCase(Locale.ROOT));
            if (axis == null) {
                throw new JsonParseException("Invalid rotation axis: " + string);
            } else {
                return axis;
            }
        }

        private Map<Direction, ModelElementFace> deserializeFacesValidating(JsonDeserializationContext context, JsonObject object) {
            Map<Direction, ModelElementFace> map = this.deserializeFaces(context, object);
            if (map.isEmpty()) {
                throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
            } else {
                return map;
            }
        }

        private Map<Direction, ModelElementFace> deserializeFaces(JsonDeserializationContext context, JsonObject object) {
            Map<Direction, ModelElementFace> map = Maps.newEnumMap(Direction.class);
            JsonObject jsonObject = GsonHelper.getObject(object, "faces");

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                Direction direction = getDirection(entry.getKey());
                map.put(direction, context.deserialize(entry.getValue(), ModelElementFace.class));
            }

            return map;
        }

        private Direction getDirection(String name) {
            Direction direction = Direction.byName(name);
            if (direction == null) {
                throw new JsonParseException("Unknown facing: " + name);
            } else {
                return direction;
            }
        }

        private Vec3f deserializeTo(JsonObject object) {
            Vec3f vec3f = Util.parseVec3f(object, "to");
            if (!(vec3f.x() < -16.0F) && !(vec3f.y() < -16.0F) && !(vec3f.z() < -16.0F) &&
                    !(vec3f.x() > 32.0F) && !(vec3f.y() > 32.0F) && !(vec3f.z() > 32.0F)) {
                return vec3f;
            } else {
                throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vec3f);
            }
        }

        private Vec3f deserializeFrom(JsonObject object) {
            Vec3f vec3f = Util.parseVec3f(object, "from");
            if (!(vec3f.x() < -16.0F) && !(vec3f.y() < -16.0F) && !(vec3f.z() < -16.0F) &&
                    !(vec3f.x() > 32.0F) && !(vec3f.y() > 32.0F) && !(vec3f.z() > 32.0F)) {
                return vec3f;
            } else {
                throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vec3f);
            }
        }

        @Override
        public JsonElement serialize(ModelElement modelElement, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("from", Util.serializeVec3f(modelElement.from));
            jsonObject.add("to", Util.serializeVec3f(modelElement.to));

            if (modelElement.rotation != null) {
                JsonObject rotation = new JsonObject();
                rotation.addProperty("angle", modelElement.rotation.angle);
                rotation.addProperty("axis", modelElement.rotation.axis.getName());
                rotation.add("origin", Util.serializeVec3f(modelElement.rotation.origin));
                if (modelElement.rotation.rescale) {
                    rotation.addProperty("rescale", true);
                }
                jsonObject.add("rotation", rotation);
            }

            JsonObject faces = new JsonObject();
            for (Map.Entry<Direction, ModelElementFace> entry : modelElement.faces.entrySet()) {
                faces.add(entry.getKey().name().toLowerCase(), jsonSerializationContext.serialize(entry.getValue()));
            }
            jsonObject.add("faces", faces);

            return jsonObject;
        }
    }
}
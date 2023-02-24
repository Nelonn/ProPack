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

import com.google.common.collect.Maps;
import com.google.gson.*;
import me.nelonn.propack.core.util.Direction;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

public class ModelElement {
    public /*final*/ Vec3f from;
    public /*final*/ Vec3f to;
    public final Map<Direction, ModelElementFace> faces;
    public final ModelRotation rotation;
    public final boolean shade;

    public ModelElement(Vec3f from, Vec3f to, Map<Direction, ModelElementFace> faces, @Nullable ModelRotation rotation, boolean shade) {
        this.from = from;
        this.to = to;
        this.faces = faces;
        this.rotation = rotation;
        this.shade = shade;
        this.initTextures();
    }

    private void initTextures() {
        for (Map.Entry<Direction, ModelElementFace> entry : this.faces.entrySet()) {
            float[] fs = this.getRotatedMatrix(entry.getKey());
            entry.getValue().textureData.setUvs(fs);
        }
    }

    private float[] getRotatedMatrix(Direction direction) {
        switch (direction) {
            case DOWN:
                return new float[]{this.from.getX(), 16.0F - this.to.getZ(), this.to.getX(), 16.0F - this.from.getZ()};
            case UP:
                return new float[]{this.from.getX(), this.from.getZ(), this.to.getX(), this.to.getZ()};
            case NORTH:
            default:
                return new float[]{16.0F - this.to.getX(), 16.0F - this.to.getY(), 16.0F - this.from.getX(), 16.0F - this.from.getY()};
            case SOUTH:
                return new float[]{this.from.getX(), 16.0F - this.to.getY(), this.to.getX(), 16.0F - this.from.getY()};
            case WEST:
                return new float[]{this.from.getZ(), 16.0F - this.to.getY(), this.to.getZ(), 16.0F - this.from.getY()};
            case EAST:
                return new float[]{16.0F - this.to.getZ(), 16.0F - this.to.getY(), 16.0F - this.from.getZ(), 16.0F - this.from.getY()};
        }
    }

    protected static class Deserializer implements JsonDeserializer<ModelElement>, JsonSerializer<ModelElement> {
        private static final boolean DEFAULT_SHADE = true;

        protected Deserializer() {
        }

        public ModelElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vec3f vec3f = this.deserializeFrom(jsonObject);
            Vec3f vec3f2 = this.deserializeTo(jsonObject);
            ModelRotation modelRotation = this.deserializeRotation(jsonObject);
            Map<Direction, ModelElementFace> map = this.deserializeFacesValidating(jsonDeserializationContext, jsonObject);
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
                Vec3f vec3f = this.deserializeVec3f(jsonObject, "origin");
                //vec3f.scale(0.0625F); why is needed, mojang ????
                Direction.Axis axis = this.deserializeAxis(jsonObject);
                float f = this.deserializeRotationAngle(jsonObject);
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
                Direction direction = this.getDirection(entry.getKey());
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
            Vec3f vec3f = this.deserializeVec3f(object, "to");
            if (!(vec3f.getX() < -16.0F) && !(vec3f.getY() < -16.0F) && !(vec3f.getZ() < -16.0F) && !(vec3f.getX() > 32.0F) && !(vec3f.getY() > 32.0F) && !(vec3f.getZ() > 32.0F)) {
                return vec3f;
            } else {
                throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vec3f);
            }
        }

        private Vec3f deserializeFrom(JsonObject object) {
            Vec3f vec3f = this.deserializeVec3f(object, "from");
            if (!(vec3f.getX() < -16.0F) && !(vec3f.getY() < -16.0F) && !(vec3f.getZ() < -16.0F) && !(vec3f.getX() > 32.0F) && !(vec3f.getY() > 32.0F) && !(vec3f.getZ() > 32.0F)) {
                return vec3f;
            } else {
                throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vec3f);
            }
        }

        private Vec3f deserializeVec3f(JsonObject object, String name) {
            JsonArray jsonArray = GsonHelper.getArray(object, name);
            if (jsonArray.size() != 3) {
                throw new JsonParseException("Expected 3 " + name + " values, found: " + jsonArray.size());
            } else {
                float[] fs = new float[3];

                for(int i = 0; i < fs.length; ++i) {
                    fs[i] = GsonHelper.asFloat(jsonArray.get(i), name + "[" + i + "]");
                }

                return new Vec3f(fs[0], fs[1], fs[2]);
            }
        }

        @Override
        public JsonElement serialize(ModelElement modelElement, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("from", serializeVec3f(modelElement.from));
            jsonObject.add("to", serializeVec3f(modelElement.to));

            if (modelElement.rotation != null) {
                JsonObject rotation = new JsonObject();
                rotation.addProperty("angle", modelElement.rotation.angle);
                rotation.addProperty("axis", modelElement.rotation.axis.getName());
                rotation.add("origin", serializeVec3f(modelElement.rotation.origin));
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

        private JsonArray serializeVec3f(Vec3f vec3f) {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(vec3f.getX());
            jsonArray.add(vec3f.getY());
            jsonArray.add(vec3f.getZ());
            return jsonArray;
        }
    }
}
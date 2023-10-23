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

import java.lang.reflect.Type;

public class ModelTransformation {
    public static final ModelTransformation NONE = new ModelTransformation();
    public final Transformation thirdPersonLeftHand;
    public final Transformation thirdPersonRightHand;
    public final Transformation firstPersonLeftHand;
    public final Transformation firstPersonRightHand;
    public final Transformation head;
    public final Transformation gui;
    public final Transformation ground;
    public final Transformation fixed;

    private ModelTransformation() {
        this(Transformation.IDENTITY, Transformation.IDENTITY, Transformation.IDENTITY, Transformation.IDENTITY,
                Transformation.IDENTITY, Transformation.IDENTITY, Transformation.IDENTITY, Transformation.IDENTITY);
    }

    public ModelTransformation(ModelTransformation other) {
        this.thirdPersonLeftHand = other.thirdPersonLeftHand;
        this.thirdPersonRightHand = other.thirdPersonRightHand;
        this.firstPersonLeftHand = other.firstPersonLeftHand;
        this.firstPersonRightHand = other.firstPersonRightHand;
        this.head = other.head;
        this.gui = other.gui;
        this.ground = other.ground;
        this.fixed = other.fixed;
    }

    public ModelTransformation(Transformation thirdPersonLeftHand, Transformation thirdPersonRightHand,
                               Transformation firstPersonLeftHand, Transformation firstPersonRightHand,
                               Transformation head, Transformation gui,
                               Transformation ground, Transformation fixed) {
        this.thirdPersonLeftHand = thirdPersonLeftHand;
        this.thirdPersonRightHand = thirdPersonRightHand;
        this.firstPersonLeftHand = firstPersonLeftHand;
        this.firstPersonRightHand = firstPersonRightHand;
        this.head = head;
        this.gui = gui;
        this.ground = ground;
        this.fixed = fixed;
    }

    public Transformation getTransformation(Mode renderMode) {
        return renderMode == Mode.THIRD_PERSON_LEFT_HAND ? thirdPersonLeftHand :
                renderMode == Mode.THIRD_PERSON_RIGHT_HAND ? thirdPersonRightHand :
                renderMode == Mode.FIRST_PERSON_LEFT_HAND ? firstPersonLeftHand :
                renderMode == Mode.FIRST_PERSON_RIGHT_HAND ? firstPersonRightHand :
                renderMode == Mode.HEAD ? head :
                renderMode == Mode.GUI ? gui :
                renderMode == Mode.GROUND ? ground :
                renderMode == Mode.FIXED ? fixed :
                    Transformation.IDENTITY;
    }

    public boolean isTransformationDefined(Mode renderMode) {
        return this.getTransformation(renderMode) != Transformation.IDENTITY;
    }

    public enum Mode {
        NONE,
        THIRD_PERSON_LEFT_HAND,
        THIRD_PERSON_RIGHT_HAND,
        FIRST_PERSON_LEFT_HAND,
        FIRST_PERSON_RIGHT_HAND,
        HEAD,
        GUI,
        GROUND,
        FIXED;

        public boolean isFirstPerson() {
            return this == FIRST_PERSON_LEFT_HAND || this == FIRST_PERSON_RIGHT_HAND;
        }
    }

    protected static class Deserializer implements JsonDeserializer<ModelTransformation>, JsonSerializer<ModelTransformation> {
        protected Deserializer() {
        }

        public ModelTransformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Transformation transformation = parseModelTransformation(jsonDeserializationContext, jsonObject, "thirdperson_righthand");
            Transformation transformation2 = parseModelTransformation(jsonDeserializationContext, jsonObject, "thirdperson_lefthand");
            if (transformation2 == Transformation.IDENTITY) {
                transformation2 = transformation;
            }
            Transformation transformation3 = parseModelTransformation(jsonDeserializationContext, jsonObject, "firstperson_righthand");
            Transformation transformation4 = parseModelTransformation(jsonDeserializationContext, jsonObject, "firstperson_lefthand");
            if (transformation4 == Transformation.IDENTITY) {
                transformation4 = transformation3;
            }
            Transformation transformation5 = parseModelTransformation(jsonDeserializationContext, jsonObject, "head");
            Transformation transformation6 = parseModelTransformation(jsonDeserializationContext, jsonObject, "gui");
            Transformation transformation7 = parseModelTransformation(jsonDeserializationContext, jsonObject, "ground");
            Transformation transformation8 = parseModelTransformation(jsonDeserializationContext, jsonObject, "fixed");
            return new ModelTransformation(transformation2, transformation, transformation4, transformation3, transformation5, transformation6, transformation7, transformation8);
        }

        private Transformation parseModelTransformation(JsonDeserializationContext ctx, JsonObject json, String key) {
            return json.has(key) ? (Transformation) ctx.deserialize(json.get(key), Transformation.class) : Transformation.IDENTITY;
        }

        public JsonElement serialize(ModelTransformation modelTransformation, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            serializeModelTransformation(jsonObject, jsonSerializationContext, "thirdperson_righthand", modelTransformation.thirdPersonRightHand);
            serializeModelTransformation(jsonObject, jsonSerializationContext, "thirdperson_lefthand", modelTransformation.thirdPersonLeftHand);
            serializeModelTransformation(jsonObject, jsonSerializationContext, "firstperson_righthand", modelTransformation.firstPersonRightHand);
            serializeModelTransformation(jsonObject, jsonSerializationContext, "firstperson_lefthand", modelTransformation.firstPersonLeftHand);
            serializeModelTransformation(jsonObject, jsonSerializationContext, "head", modelTransformation.head);
            serializeModelTransformation(jsonObject, jsonSerializationContext, "gui", modelTransformation.gui);
            serializeModelTransformation(jsonObject, jsonSerializationContext, "ground", modelTransformation.ground);
            serializeModelTransformation(jsonObject, jsonSerializationContext, "fixed", modelTransformation.fixed);
            return jsonObject;
        }

        private void serializeModelTransformation(JsonObject jsonObject, JsonSerializationContext ctx, String name, Transformation transformation) {
            if (transformation == Transformation.IDENTITY) return;
            JsonObject object = ctx.serialize(transformation).getAsJsonObject();
            if (object.size() < 1) return;
            jsonObject.add(name, object);
        }
    }
}
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.*;
import me.nelonn.flint.path.Identifier;
import me.nelonn.propack.core.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ModelOverride {
    private final Identifier modelId;
    private final List<Condition> conditions;

    public ModelOverride(Identifier modelId, List<Condition> conditions) {
        this.modelId = modelId;
        this.conditions = ImmutableList.copyOf(conditions);
    }

    public Identifier getModelId() {
        return this.modelId;
    }

    public Stream<Condition> streamConditions() {
        return this.conditions.stream();
    }

    public static class Condition {
        private final Identifier type;
        private final float threshold;

        public Condition(Identifier type, float threshold) {
            this.type = type;
            this.threshold = threshold;
        }

        public Identifier getType() {
            return this.type;
        }

        public float getThreshold() {
            return this.threshold;
        }
    }

    protected static class Deserializer implements JsonDeserializer<ModelOverride> {
        protected Deserializer() {
        }

        public ModelOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Identifier identifier = Identifier.of(GsonHelper.getString(jsonObject, "model"));
            List<Condition> list = this.deserializeMinPropertyValues(jsonObject);
            return new ModelOverride(identifier, list);
        }

        protected List<Condition> deserializeMinPropertyValues(JsonObject object) {
            Map<Identifier, Float> map = Maps.newLinkedHashMap();
            JsonObject jsonObject = GsonHelper.getObject(object, "predicate");

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                map.put(Identifier.of(entry.getKey()), GsonHelper.asFloat(entry.getValue(), entry.getKey()));
            }

            return map.entrySet().stream().map((entry) -> new Condition(entry.getKey(), entry.getValue())).collect(ImmutableList.toImmutableList());
        }
    }
}
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.*;
import me.nelonn.flint.path.Key;
import me.nelonn.propack.core.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelOverride {
    private final String model;
    private final List<Condition> conditions;

    public ModelOverride(@NotNull String model, @NotNull List<Condition> conditions) {
        this.model = model;
        this.conditions = ImmutableList.copyOf(conditions);
    }

    public @NotNull String getModel() {
        return model;
    }

    public @NotNull List<Condition> getConditions() {
        return new ArrayList<>(conditions);
    }

    public static class Condition {
        private final Key type;
        private final float threshold;

        public Condition(@NotNull Key type, float threshold) {
            this.type = type;
            this.threshold = threshold;
        }

        public @NotNull Key getType() {
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
            String model = GsonHelper.getString(jsonObject, "model");
            List<Condition> list = deserializeMinPropertyValues(jsonObject);
            return new ModelOverride(model, list);
        }

        protected List<Condition> deserializeMinPropertyValues(JsonObject object) {
            Map<Key, Float> map = Maps.newLinkedHashMap();
            JsonObject jsonObject = GsonHelper.getObject(object, "predicate");
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                map.put(Key.of(entry.getKey()), GsonHelper.asFloat(entry.getValue(), entry.getKey()));
            }
            return map.entrySet().stream().map(entry -> new Condition(entry.getKey(), entry.getValue())).collect(ImmutableList.toImmutableList());
        }
    }
}
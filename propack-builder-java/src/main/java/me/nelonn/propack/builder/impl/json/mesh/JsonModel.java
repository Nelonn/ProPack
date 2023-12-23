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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.Util;
import me.nelonn.propack.core.util.Vec2i;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonModel {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(JsonModel.class, new Deserializer())
            .registerTypeAdapter(ModelElement.class, new ModelElement.Deserializer())
            .registerTypeAdapter(ModelElementFace.class, new ModelElementFace.Deserializer())
            .registerTypeAdapter(ModelElementTexture.class, new ModelElementTexture.Deserializer())
            .registerTypeAdapter(Transformation.class, new Transformation.Deserializer())
            .registerTypeAdapter(ModelTransformation.class, new ModelTransformation.Deserializer())
            .registerTypeAdapter(ModelOverride.class, new ModelOverride.Deserializer())
            .create();
    private final String parent;
    private final Vec2i textureSize;
    protected final Map<String, String> textureMap;
    private final List<ModelElement> elements;
    @Nullable
    private final GuiLight guiLight;
    private final boolean ambientOcclusion;
    private final ModelTransformation transformations;
    private final List<ModelOverride> overrides;

    public static JsonModel deserialize(JsonObject jsonObject) {
        return GSON.fromJson(jsonObject, JsonModel.class);
    }

    public static JsonModel deserialize(Reader input) {
        return GsonHelper.deserialize(GSON, input, JsonModel.class);
    }

    public static JsonModel deserialize(String json) {
        return deserialize(new StringReader(json));
    }

    public JsonObject serialize() {
        return GSON.toJsonTree(this, JsonModel.class).getAsJsonObject();
    }

    public JsonModel(@Nullable String parent,
                     Vec2i textureSize,
                     Map<String, String> textureMap,
                     List<ModelElement> elements,
                     boolean ambientOcclusion,
                     @Nullable GuiLight guiLight,
                     ModelTransformation transformations,
                     List<ModelOverride> overrides) {
        this.parent = parent;
        this.textureSize = textureSize;
        this.textureMap = textureMap;
        this.elements = elements;
        this.ambientOcclusion = ambientOcclusion;
        this.guiLight = guiLight;
        this.transformations = transformations;
        this.overrides = overrides;
    }

    public String getParent() {
        return parent;
    }

    public Vec2i getTextureSize() {
        return textureSize;
    }

    public Map<String, String> getTextureMap() {
        return new HashMap<>(textureMap);
    }

    public List<ModelElement> getElements() {
        return new ArrayList<>(elements);
    }

    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    public GuiLight getGuiLight() {
        return guiLight != null ? guiLight : GuiLight.BLOCK;
    }

    public List<ModelOverride> getOverrides() {
        return new ArrayList<>(overrides);
    }

    public ModelTransformation getTransformations() {
        return new ModelTransformation(
                this.getTransformation(ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND),
                this.getTransformation(ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND),
                this.getTransformation(ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND),
                this.getTransformation(ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND),
                this.getTransformation(ModelTransformation.Mode.HEAD),
                this.getTransformation(ModelTransformation.Mode.GUI),
                this.getTransformation(ModelTransformation.Mode.GROUND),
                this.getTransformation(ModelTransformation.Mode.FIXED));
    }

    private Transformation getTransformation(ModelTransformation.Mode renderMode) {
        return transformations.getTransformation(renderMode);
    }

    @Override
    public String toString() {
        return "JsonUnbakedModel{" +
                "textureSize=" + textureSize +
                ", textureMap=" + textureMap +
                ", cubes=" + elements.size() +
                '}';
    }

    public enum GuiLight {
        ITEM("front"),
        BLOCK("side");

        private final String name;

        GuiLight(String name) {
            this.name = name;
        }

        public static GuiLight byName(String value) {
            for (GuiLight guiLight : values()) {
                if (guiLight.name.equals(value)) {
                    return guiLight;
                }
            }

            throw new IllegalArgumentException("Invalid gui light: " + value);
        }

        public boolean isSide() {
            return this == BLOCK;
        }
    }

    public static class Deserializer implements JsonDeserializer<JsonModel>, JsonSerializer<JsonModel> {
        private static final Vec2i DEFAULT_TEXTURE_SIZE = new Vec2i(16, 16);

        public JsonModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String parent = GsonHelper.getString(jsonObject, "parent", "");

            Vec2i textureSize = Util.parseVec2i(jsonObject, "texture_size", DEFAULT_TEXTURE_SIZE);

            Map<String, String> textures = texturesFromJson(jsonObject);

            List<ModelElement> elements = elementsFromJson(jsonObject, jsonDeserializationContext);

            boolean ambientOcclusion = ambientOcclusionFromJson(jsonObject);

            GuiLight guiLight = null;
            if (jsonObject.has("gui_light")) {
                guiLight = JsonModel.GuiLight.byName(GsonHelper.getString(jsonObject, "gui_light"));
            }

            ModelTransformation modelTransformation = ModelTransformation.NONE;
            if (jsonObject.has("display")) {
                JsonObject jsonObject2 = GsonHelper.getObject(jsonObject, "display");
                modelTransformation = jsonDeserializationContext.deserialize(jsonObject2, ModelTransformation.class);
            }

            List<ModelOverride> overrides = overridesFromJson(jsonObject, jsonDeserializationContext);

            return new JsonModel(parent, textureSize, textures, elements, ambientOcclusion, guiLight, modelTransformation, overrides);
        }

        private Map<String, String> texturesFromJson(JsonObject object) {
            Map<String, String> map = Maps.newHashMap();
            if (object.has("textures")) {
                JsonObject texturesObject = GsonHelper.getObject(object, "textures");
                for (Map.Entry<String, JsonElement> entry : texturesObject.entrySet()) {
                    map.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            return map;
        }

        private List<ModelElement> elementsFromJson(JsonObject json, JsonDeserializationContext context) {
            List<ModelElement> elements = Lists.newArrayList();
            if (json.has("elements")) {
                for (JsonElement jsonElement : GsonHelper.getArray(json, "elements")) {
                    elements.add(context.deserialize(jsonElement, ModelElement.class));
                }
            }
            return elements;
        }

        private boolean ambientOcclusionFromJson(JsonObject json) {
            return GsonHelper.getBoolean(json, "ambientocclusion", true);
        }

        private List<ModelOverride> overridesFromJson(JsonObject object, JsonDeserializationContext context) {
            List<ModelOverride> list = Lists.newArrayList();
            if (object.has("overrides")) {
                JsonArray jsonArray = GsonHelper.getArray(object, "overrides");
                for (JsonElement jsonElement : jsonArray) {
                    list.add(context.deserialize(jsonElement, ModelOverride.class));
                }
            }
            return list;
        }

        public JsonElement serialize(JsonModel model, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (model.parent != null && !model.parent.isEmpty()) {
                jsonObject.addProperty("parent", model.parent);
            }
            if (model.textureSize != null) {
                jsonObject.add("texture_size", Util.serializeVec2i(model.textureSize));
            }
            if (model.textureMap != null) {
                JsonObject textures = new JsonObject();
                for (Map.Entry<String, String> texture : model.textureMap.entrySet()) {
                    textures.addProperty(texture.getKey(), texture.getValue());
                }
                jsonObject.add("textures", textures);
            }
            if (model.elements != null && !model.elements.isEmpty()) {
                jsonObject.add("elements", jsonSerializationContext.serialize(model.elements));
            }
            if (!model.ambientOcclusion) {
                jsonObject.addProperty("ambientocclusion", false);
            }
            if (model.guiLight != null) {
                jsonObject.addProperty("gui_light", model.guiLight.name);
            }
            JsonObject display = jsonSerializationContext.serialize(model.transformations).getAsJsonObject();
            if (display.size() > 0) {
                jsonObject.add("display", display);
            }
            if (!model.overrides.isEmpty()) {
                jsonObject.add("overrides", jsonSerializationContext.serialize(model.overrides));
            }
            return jsonObject;
        }
    }
}

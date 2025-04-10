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

package me.nelonn.propack.builder.impl.task;

import com.google.gson.*;
import me.nelonn.bestvecs.ImmVec3f;
import me.nelonn.bestvecs.Vec3f;
import me.nelonn.flint.path.Key;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.builder.api.Project;
import me.nelonn.propack.builder.api.file.File;
import me.nelonn.propack.builder.api.file.JsonFile;
import me.nelonn.propack.builder.api.task.AbstractTask;
import me.nelonn.propack.builder.api.task.FileProcessingException;
import me.nelonn.propack.builder.api.task.TaskBootstrap;
import me.nelonn.propack.builder.api.task.TaskIO;
import me.nelonn.propack.builder.api.util.Extra;
import me.nelonn.propack.builder.impl.MeshesMapBuilder;
import me.nelonn.propack.core.asset.CombinedItemModelBuilder;
import me.nelonn.propack.core.asset.DefaultItemModelBuilder;
import me.nelonn.propack.core.asset.ItemModelBuilder;
import me.nelonn.propack.core.asset.SlotItemModelBuilder;
import me.nelonn.propack.builder.impl.json.mesh.JsonModel;
import me.nelonn.propack.builder.impl.json.mesh.ModelElement;
import me.nelonn.propack.builder.impl.json.mesh.ModelElementFace;
import me.nelonn.propack.core.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessModelsTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = ProcessModelsTask::new;
    public static final Extra<MeshesMapBuilder> EXTRA_MESH_MAPPING_BUILDER = new Extra<>(MeshesMapBuilder.class, "propack.process_models.mesh_mapping_builder");

    public ProcessModelsTask(@NotNull Project project) {
        super("processModels", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        MeshesMapBuilder meshesMapBuilder = new MeshesMapBuilder(getProject().getBuildConfiguration().getCustomModelDataStart());
        io.getExtras().put(EXTRA_MESH_MAPPING_BUILDER, meshesMapBuilder);
        Map<Path, Set<Key>> meshesToOverride = new HashMap<>();
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".model.json")) continue;
                io.getFiles().removeFile(filePath);
                if (!(file instanceof JsonFile)) {
                    LOGGER.error("{} :: model file is not Json", filePath);
                    continue;
                }
                JsonObject rootJson = ((JsonFile) file).getContent();
                Path resourcePath = PathUtil.resourcePath(filePath, ".model.json");
                String type = GsonHelper.getString(rootJson, "Type");
                String rawMeshPath = GsonHelper.getString(rootJson, "Mesh");
                Path meshPath = PathUtil.resolve(rawMeshPath, resourcePath);
                Set<Key> targetItems = parseTarget(rootJson);
                ItemModelBuilder builder;
                if (type.equals("DefaultItemModel")) {
                    builder = new DefaultItemModelBuilder(resourcePath).setMesh(meshPath);
                } else if (type.equals("CombinedItemModel")) {
                    File meshFile = io.getFiles().getFile(PathUtil.contentPath(meshPath) + ".mesh.json");
                    if (!(meshFile instanceof JsonFile)) {
                        LOGGER.error("{} :: mesh not found: {}", filePath, rawMeshPath);
                        continue;
                    }
                    JsonModel baseMesh = JsonModel.deserialize(((JsonFile) meshFile).getContent());
                    Map<String, String> baseTextureMap = baseMesh.getTextureMap();
                    processTextureMap(baseTextureMap, resourcePath);
                    JsonObject elementsObject = GsonHelper.getObject(rootJson, "Elements");
                    Map<String, JsonModel> combinationElements = new HashMap<>();
                    for (Map.Entry<String, JsonElement> elementEntry : elementsObject.entrySet()) {
                        JsonElement jsonElement = elementEntry.getValue();
                        JsonModel elementMesh = parseGeneratingMesh(jsonElement, resourcePath, io);
                        combinationElements.put(elementEntry.getKey(), elementMesh);
                    }
                    for (List<String> combination : CombinationUtil.generateAllCombinations(combinationElements.keySet())) {
                        String combinationStr = String.join("&", combination.stream().sorted().toArray(String[]::new));
                        String hex = Integer.toHexString(combinationStr.hashCode());
                        Path generatedPath = PathUtil.append(resourcePath, '-' + hex);
                        Map<String, String> textureMap = new HashMap<>(baseTextureMap);
                        List<ModelElement> modelElements = baseMesh.getElements();
                        for (String combinationElement : combination) {
                            JsonModel combMesh = combinationElements.get(combinationElement);
                            for (Map.Entry<String, String> textureEntry : combMesh.getTextureMap().entrySet()) {
                                textureMap.put(combinationElement + '.' + textureEntry.getKey(), textureEntry.getValue());
                            }
                            for (ModelElement modelElement : combMesh.getElements()) {
                                Map<Direction, ModelElementFace> faces = new EnumMap<>(modelElement.faces);
                                for (Map.Entry<Direction, ModelElementFace> faceEntry : faces.entrySet()) {
                                    ModelElementFace face = faceEntry.getValue();
                                    String texture = '#' + combinationElement + '.' + face.textureId.substring(1);
                                    faceEntry.setValue(new ModelElementFace(face.cullFace, face.tintIndex, texture, face.textureData));
                                }
                                modelElements.add(new ModelElement(modelElement.from, modelElement.to, faces,
                                        modelElement.rotation, modelElement.shade));
                            }
                        }
                        JsonModel generatedMesh = new JsonModel(baseMesh.getParent(), baseMesh.getTextureSize(),
                                textureMap, modelElements, baseMesh.useAmbientOcclusion(), baseMesh.getGuiLight(),
                                baseMesh.getTransformations(), baseMesh.getOverrides());
                        io.getFiles().addFile(new JsonFile(PathUtil.contentPath(generatedPath) + ".mesh.json", generatedMesh.serialize()));
                        meshesToOverride.put(generatedPath, new HashSet<>(targetItems));
                    }
                    builder = new CombinedItemModelBuilder(resourcePath).setMesh(meshPath).setElements(combinationElements.keySet());
                } else if (type.equals("SlotItemModel")) {
                    File meshFile = io.getFiles().getFile(PathUtil.contentPath(meshPath) + ".mesh.json");
                    if (!(meshFile instanceof JsonFile)) {
                        LOGGER.error("{} :: mesh not found: {}", filePath, rawMeshPath);
                        continue;
                    }
                    JsonModel baseMesh = JsonModel.deserialize(((JsonFile) meshFile).getContent());
                    Map<String, String> rootTextureMap = baseMesh.getTextureMap();
                    processTextureMap(rootTextureMap, meshPath);
                    JsonObject slotsJson = GsonHelper.getObject(rootJson, "Slots");
                    Map<String, Map<String, JsonModel>> slots = new HashMap<>();
                    for (Map.Entry<String, JsonElement> slotEntry : slotsJson.entrySet()) {
                        JsonObject slotJson = slotEntry.getValue().getAsJsonObject();
                        Map<String, JsonModel> slotElements = new HashMap<>();
                        for (Map.Entry<String, JsonElement> elementEntry : slotJson.entrySet()) {
                            JsonElement elementJson = elementEntry.getValue();
                            JsonModel elementMesh = parseGeneratingMesh(elementJson, resourcePath, io);
                            slotElements.put(elementEntry.getKey(), elementMesh);
                        }
                        slots.put(slotEntry.getKey(), slotElements);
                    }
                    LinkedHashMap<String, List<String>> slotsMap = new LinkedHashMap<>();
                    for (Map.Entry<String, Map<String, JsonModel>> slot : slots.entrySet()) {
                        slotsMap.put(slot.getKey(), new ArrayList<>(slot.getValue().keySet()));
                    }
                    for (Map<String, String> combination : CombinationUtil.generateSlotCombinations(slotsMap)) {
                        StringBuilder sb = new StringBuilder();
                        AtomicBoolean empty = new AtomicBoolean(true);
                        Map<String, String> textureMap = new HashMap<>(rootTextureMap);
                        List<ModelElement> modelElements = baseMesh.getElements();
                        combination.keySet().stream().sorted().forEach(slotName -> {
                            if (sb.length() > 0) {
                                sb.append('&');
                            }
                            sb.append(slotName).append(':');
                            String elementName = combination.get(slotName);
                            if (elementName != null) {
                                sb.append(elementName);
                                empty.set(false);
                                JsonModel elementMesh = slots.get(slotName).get(elementName);
                                for (Map.Entry<String, String> textureEntry : elementMesh.getTextureMap().entrySet()) {
                                    textureMap.put(slotName + '.' + textureEntry.getKey(), textureEntry.getValue());
                                }
                                for (ModelElement modelElement : elementMesh.getElements()) {
                                    Map<Direction, ModelElementFace> faces = new EnumMap<>(modelElement.faces);
                                    for (Map.Entry<Direction, ModelElementFace> faceEntry : faces.entrySet()) {
                                        ModelElementFace face = faceEntry.getValue();
                                        String texture = '#' + slotName + '.' + face.textureId.substring(1);
                                        faceEntry.setValue(new ModelElementFace(face.cullFace, face.tintIndex, texture, face.textureData));
                                    }
                                    modelElements.add(new ModelElement(modelElement.from, modelElement.to, faces,
                                            modelElement.rotation, modelElement.shade));
                                }
                            }
                        });
                        if (empty.get()) continue;
                        String hex = SlotItemModel.hash(sb.toString());
                        Path generatedPath = PathUtil.append(resourcePath, '-' + hex);
                        JsonModel generatedMesh = new JsonModel(baseMesh.getParent(), baseMesh.getTextureSize(),
                                textureMap, modelElements, baseMesh.useAmbientOcclusion(), baseMesh.getGuiLight(),
                                baseMesh.getTransformations(), baseMesh.getOverrides());
                        io.getFiles().addFile(new JsonFile(PathUtil.contentPath(generatedPath) + ".mesh.json", generatedMesh.serialize()));
                        meshesToOverride.put(generatedPath, new HashSet<>(targetItems));
                    }
                    Map<String, SlotItemModel.Slot> resultSlots = new HashMap<>();
                    for (Map.Entry<String, List<String>> slot : slotsMap.entrySet()) {
                        SlotItemModel.Slot resultSlot = new SlotItemModel.Slot(slot.getKey(), new HashSet<>(slot.getValue()));
                        resultSlots.put(resultSlot.getName(), resultSlot);
                    }
                    builder = new SlotItemModelBuilder(resourcePath).setMesh(meshPath).setSlots(resultSlots);
                } else {
                    LOGGER.error("{} :: Unknown model type: '{}'", filePath, type);
                    continue;
                }
                Set<Key> toOverride = meshesToOverride.computeIfAbsent(meshPath, key -> new HashSet<>());
                toOverride.addAll(targetItems);
                builder.setTargetItems(targetItems);
                io.getAssets().putItemModel(builder);
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".mesh.json")) continue;
                io.getFiles().removeFile(filePath);
                if (!(file instanceof JsonFile)) {
                    LOGGER.error("{} :: mesh file is not Json", filePath);
                    continue;
                }
                Path resourcePath = PathUtil.resourcePath(filePath, ".mesh.json");
                JsonModel jsonModel = JsonModel.deserialize(((JsonFile) file).getContent());
                String parent = jsonModel.getParent();
                if (parent != null && !parent.isEmpty()) {
                    parent = PathUtil.resolve(parent, resourcePath).toString();
                }
                Map<String, String> textureMap = jsonModel.getTextureMap();
                processTextureMap(textureMap, resourcePath);
                jsonModel = new JsonModel(parent, jsonModel.getTextureSize(), textureMap, jsonModel.getElements(),
                        jsonModel.useAmbientOcclusion(), jsonModel.getGuiLight(), jsonModel.getTransformations(),
                        jsonModel.getOverrides());
                JsonObject resultJson = jsonModel.serialize();
                io.getFiles().addFile(new JsonFile("assets/" + resourcePath.namespace() + "/models/" + resourcePath.value() + ".json", resultJson));
                Set<Key> toOverride = meshesToOverride.get(resourcePath);
                if (toOverride != null) {
                    for (Key itemId : toOverride) {
                        meshesMapBuilder.getMapper(itemId).add(resourcePath);
                    }
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        // overriding default models (custom_model_data)
        for (MeshesMapBuilder.ItemEntry itemEntry : meshesMapBuilder.getMappers()) {
            if (getProject().getBuildConfiguration().isGenerateItemModels()) {
                String pathBase = "include/assets/propack/items/" + itemEntry.getItemId().value() + ".";
                for (Map.Entry<Integer, Path> mapping : itemEntry.getMap().entrySet()) {
                    String finalPath = pathBase + Integer.toHexString(mapping.getKey()) + ".json";
                    JsonObject rootJson = new JsonObject();
                    JsonObject modelJson = new JsonObject();
                    modelJson.addProperty("type", "minecraft:model");
                    modelJson.addProperty("model", mapping.getValue().toString());
                    rootJson.add("model", modelJson);
                    io.getFiles().addFile(new JsonFile(finalPath, rootJson));
                }
            }
            String path = "include/assets/" + itemEntry.getItemId().namespace() + "/models/item/" + itemEntry.getItemId().value() + ".json";
            File file = io.getFiles().getFile(path);
            if (!(file instanceof JsonFile)) {
                LOGGER.error("Default model not found: {}", path);
                continue;
            }
            JsonObject jsonObject = ((JsonFile) file).getContent();
            JsonArray overrides = GsonHelper.getArray(jsonObject, "overrides", new JsonArray());
            for (Map.Entry<Integer, Path> mapping : itemEntry.getMap().entrySet()) {
                overrides.add(new JsonObjectBuilder()
                        .put("predicate", new JsonObjectBuilder().put("custom_model_data", mapping.getKey()).get())
                        .put("model", mapping.getValue().toString())
                        .get());
            }
            jsonObject.add("overrides", overrides);
        }
    }

    private void processTextureMap(@NotNull Map<String, String> textureMap, @NotNull Path resourcePath) {
        for (Map.Entry<String, String> textureEntry : textureMap.entrySet()) {
            String texture = textureEntry.getValue();
            if (texture.startsWith("#")) continue; // TODO: Maybe rework that?
            // BlockBench mesh export workaround
            if (!texture.contains(":") && !texture.contains("/") && !texture.startsWith(".")) { // TODO: improve
                texture = "./" + texture;
            }
            textureEntry.setValue(PathUtil.resolve(texture, resourcePath).toString());
        }
    }

    private Set<Key> parseTarget(JsonObject model) {
        Set<Key> output = new HashSet<>();
        if (GsonHelper.hasString(model, "Target")) {
            String target = GsonHelper.getString(model, "Target");
            output.add(Key.of(target));
        } else if (GsonHelper.hasArray(model, "Target")) {
            JsonArray targets = GsonHelper.getArray(model, "Target");
            if (targets.isEmpty()) {
                throw new JsonParseException("Target cannot be empty");
            }
            for (JsonElement target : targets) {
                output.add(Key.of(target.getAsString()));
            }
        } else {
            throw new JsonParseException("Missing 'Target'");
        }
        return output;
    }

    private JsonModel parseGeneratingMesh(JsonElement jsonElement, Path resourcePath, TaskIO io) {
        Path elementMeshPath;
        Vec3f offset = null;
        Vec3f scaleOrigin = null;
        float scaleSize = 0.0F;
        if (GsonHelper.isString(jsonElement)) {
            elementMeshPath = PathUtil.resolve(jsonElement.getAsString(), resourcePath);
        } else {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            elementMeshPath = PathUtil.resolve(GsonHelper.getString(jsonObject, "Mesh"), resourcePath);
            offset = Util.parseVec3f(jsonObject, "Offset", ImmVec3f.ZERO);
            JsonObject scaleObject = GsonHelper.getObject(jsonObject, "Scale", null);
            if (scaleObject != null) {
                scaleOrigin = Util.parseVec3f(scaleObject, "Origin", ImmVec3f.ZERO);
                scaleSize = GsonHelper.getFloat(scaleObject, "Size");
            }
        }
        File elementMeshFile = io.getFiles().getFile(PathUtil.contentPath(elementMeshPath) + ".mesh.json");
        if (!(elementMeshFile instanceof JsonFile)) {
            throw new IllegalArgumentException("Mesh not found: " + elementMeshPath);
        }
        JsonModel elementMesh = JsonModel.deserialize(((JsonFile) elementMeshFile).getContent());
        Map<String, String> textureMap = elementMesh.getTextureMap();
        processTextureMap(textureMap, elementMeshPath);
        List<ModelElement> modelElements = elementMesh.getElements();
        if (offset != null || scaleOrigin != null) {
            for (ModelElement cube : modelElements) {
                if (offset != null) {
                    move(cube, offset);
                }
                if (scaleOrigin != null) {
                    scale(cube, scaleOrigin, scaleSize);
                }
            }
        }
        return new JsonModel(elementMesh.getParent(), elementMesh.getTextureSize(),
                textureMap, modelElements, elementMesh.useAmbientOcclusion(), elementMesh.getGuiLight(),
                elementMesh.getTransformations(), elementMesh.getOverrides());
    }

    private static void move(ModelElement cube, Vec3f offset) {
        cube.from = cube.from.add(offset);
        cube.to = cube.to.add(offset);
        if (cube.rotation != null) {
            cube.rotation.origin = cube.rotation.origin.add(offset);
        }
    }

    private static void scale(ModelElement cube, Vec3f origin, float size) {
        cube.from = cube.from.subtract(origin).multiply(size).add(origin);
        cube.to = cube.to.subtract(origin).multiply(size).add(origin);
        if (cube.rotation != null) {
            cube.rotation.origin = cube.rotation.origin.subtract(origin).multiply(size).add(origin);
        }
    }
}

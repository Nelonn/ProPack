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

package me.nelonn.propack.core.builder.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.nelonn.flint.path.Identifier;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.asset.SlotItemModel;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.file.JsonFile;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.core.builder.MeshMappingBuilder;
import me.nelonn.propack.core.builder.asset.CombinedItemModelBuilder;
import me.nelonn.propack.core.builder.asset.DefaultItemModelBuilder;
import me.nelonn.propack.core.builder.asset.ItemModelBuilder;
import me.nelonn.propack.core.builder.asset.SlotItemModelBuilder;
import me.nelonn.propack.core.builder.json.mesh.JsonModel;
import me.nelonn.propack.core.builder.json.mesh.ModelElement;
import me.nelonn.propack.core.builder.json.mesh.ModelElementFace;
import me.nelonn.propack.builder.task.AbstractTask;
import me.nelonn.propack.builder.task.FileProcessingException;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.core.util.*;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessModelsTask extends AbstractTask {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final TaskBootstrap BOOTSTRAP = ProcessModelsTask::new;
    public static final Extra<MeshMappingBuilder> EXTRA_MESH_MAPPING_BUILDER = new Extra<>(MeshMappingBuilder.class, "propack.process_models.mesh_mapping_builder");

    public ProcessModelsTask(@NotNull Project project) {
        super("processModels", project);
    }

    @Override
    public void run(@NotNull TaskIO io) {
        MeshMappingBuilder meshMappingBuilder = new MeshMappingBuilder();
        io.getExtras().put(EXTRA_MESH_MAPPING_BUILDER, meshMappingBuilder);
        Map<Path, Set<Identifier>> meshesToOverride = new HashMap<>();
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".model.json") || !(file instanceof JsonFile)) continue;
                io.getFiles().removeFile(filePath);
                JsonObject rootJson = ((JsonFile) file).getContent();
                Path resourcePath = PathUtil.resourcePath(filePath, ".model.json");
                String type = GsonHelper.getString(rootJson, "Type");
                String mesh = GsonHelper.getString(rootJson, "Mesh");
                Path meshPath = PathUtil.resolve(mesh, resourcePath);
                Set<Identifier> targetItems = parseTarget(rootJson);
                ItemModelBuilder builder;
                if (type.equals("DefaultItemModel")) {
                    builder = new DefaultItemModelBuilder(resourcePath).setMesh(meshPath);
                } else if (type.equals("CombinedItemModel")) {
                    File meshFile = io.getFiles().getFile(PathUtil.contentPath(meshPath) + ".mesh.json");
                    if (!(meshFile instanceof JsonFile)) {
                        throw new IllegalArgumentException("Mesh not found: " + meshPath);
                    }
                    JsonModel baseMesh = JsonModel.deserialize(((JsonFile) meshFile).getContent());
                    Map<String, String> baseTextureMap = baseMesh.getTextureMap();
                    for (Map.Entry<String, String> textureEntry : baseTextureMap.entrySet()) {
                        String texture = textureEntry.getValue();
                        if (texture.startsWith("#")) continue;
                        textureEntry.setValue(PathUtil.resolve(texture, resourcePath).toString());
                    }
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
                        throw new IllegalArgumentException("Mesh not found: " + meshPath);
                    }
                    JsonModel baseMesh = JsonModel.deserialize(((JsonFile) meshFile).getContent());
                    Map<String, String> baseTextureMap = baseMesh.getTextureMap();
                    for (Map.Entry<String, String> textureEntry : baseTextureMap.entrySet()) {
                        String texture = textureEntry.getValue();
                        if (texture.startsWith("#")) continue;
                        textureEntry.setValue(PathUtil.resolve(texture, resourcePath).toString());
                    }
                    JsonObject slotsObject = GsonHelper.getObject(rootJson, "Slots");
                    Map<String, Map<String, JsonModel>> slots = new HashMap<>();
                    for (Map.Entry<String, JsonElement> slotEntry : slotsObject.entrySet()) {
                        JsonObject slotObject = slotEntry.getValue().getAsJsonObject();
                        Map<String, JsonModel> slotElements = new HashMap<>();
                        for (Map.Entry<String, JsonElement> elementEntry : slotObject.entrySet()) {
                            JsonElement jsonElement = elementEntry.getValue();
                            JsonModel elementMesh = parseGeneratingMesh(jsonElement, resourcePath, io);
                            slotElements.put(elementEntry.getKey(), elementMesh);
                        }
                        slots.put(slotEntry.getKey(), slotElements);
                    }
                    Map<String, List<String>> slotsMap = new HashMap<>();
                    for (Map.Entry<String, Map<String, JsonModel>> slot : slots.entrySet()) {
                        slotsMap.put(slot.getKey(), new ArrayList<>(slot.getValue().keySet()));
                    }
                    for (Map<String, String> combination : CombinationUtil.generateSlotCombinations(slotsMap)) {
                        StringBuilder sb = new StringBuilder();
                        AtomicBoolean empty = new AtomicBoolean(true);
                        Map<String, String> textureMap = new HashMap<>(baseTextureMap);
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
                        String hex = Integer.toHexString(sb.toString().hashCode());
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
                    throw new IllegalArgumentException("Unknown model type: " + type);
                }
                Set<Identifier> toOverride = meshesToOverride.computeIfAbsent(meshPath, key -> new HashSet<>());
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
                if (!filePath.startsWith("content/") || !filePath.endsWith(".mesh.json") || !(file instanceof JsonFile)) continue;
                io.getFiles().removeFile(filePath);
                Path resourcePath = PathUtil.resourcePath(filePath, ".mesh.json");
                JsonModel jsonModel = JsonModel.deserialize(((JsonFile) file).getContent());
                String parent = jsonModel.getParent();
                if (parent != null && !parent.isEmpty()) {
                    parent = PathUtil.resolve(parent, resourcePath).toString();
                }
                Map<String, String> textureMap = jsonModel.getTextureMap();
                for (Map.Entry<String, String> textureEntry : textureMap.entrySet()) {
                    String texture = textureEntry.getValue();
                    if (texture.startsWith("#")) continue;
                    textureEntry.setValue(PathUtil.resolve(texture, resourcePath).toString());
                }
                jsonModel = new JsonModel(parent, jsonModel.getTextureSize(), textureMap, jsonModel.getElements(),
                        jsonModel.useAmbientOcclusion(), jsonModel.getGuiLight(), jsonModel.getTransformations(),
                        jsonModel.getOverrides());
                JsonObject resultJson = jsonModel.serialize();
                io.getFiles().addFile(new JsonFile("assets/" + resourcePath.getNamespace() + "/models/" + resourcePath.getValue() + ".json", resultJson));
                Set<Identifier> toOverride = meshesToOverride.get(resourcePath);
                if (toOverride != null) {
                    for (Identifier itemId : toOverride) {
                        meshMappingBuilder.getMapper(itemId).add(resourcePath);
                    }
                }
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        // overriding default models (custom_model_data)
        for (MeshMappingBuilder.ItemEntry itemEntry : meshMappingBuilder.getMappers()) {
            String path = "include/assets/minecraft/models/item/" + itemEntry.getItemId().getValue() + ".json";
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

    private Set<Identifier> parseTarget(JsonObject model) {
        Set<Identifier> output = new HashSet<>();
        if (GsonHelper.hasString(model, "Target")) {
            String target = GsonHelper.getString(model, "Target");
            output.add(Identifier.of(target));
        } else if (GsonHelper.hasArray(model, "Target")) {
            JsonArray targets = GsonHelper.getArray(model, "Target");
            if (targets.isEmpty()) {
                throw new JsonParseException("Target cannot be empty");
            }
            for (JsonElement target : targets) {
                output.add(Identifier.of(target.getAsString()));
            }
        } else {
            throw new JsonParseException("Missing 'Target'");
        }
        return output;
    }

    private JsonModel parseGeneratingMesh(JsonElement jsonElement, Path resourcePath, TaskIO io) {
        Path elementMeshPath;
        Vec3f offset;
        if (GsonHelper.isString(jsonElement)) {
            elementMeshPath = PathUtil.resolve(jsonElement.getAsString(), resourcePath);
            offset = Vec3f.ZERO;
        } else {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            elementMeshPath = PathUtil.resolve(GsonHelper.getString(jsonObject, "Mesh"), resourcePath);
            offset = Util.parseVec3f(jsonObject, "Offset", Vec3f.ZERO);
        }
        File elementMeshFile = io.getFiles().getFile(PathUtil.contentPath(elementMeshPath) + ".mesh.json");
        if (!(elementMeshFile instanceof JsonFile)) {
            throw new IllegalArgumentException("Mesh not found: " + elementMeshPath);
        }
        JsonModel elementMesh = JsonModel.deserialize(((JsonFile) elementMeshFile).getContent());
        Map<String, String> textureMap = elementMesh.getTextureMap();
        for (Map.Entry<String, String> textureEntry : textureMap.entrySet()) {
            String texture = textureEntry.getValue();
            if (texture.startsWith("#")) continue;
            textureEntry.setValue(PathUtil.resolve(texture, elementMeshPath).toString());
        }
        List<ModelElement> modelElements = elementMesh.getElements();
        for (ModelElement modelElement : modelElements) {
            modelElement.from = modelElement.from.add(offset);
            modelElement.to = modelElement.to.add(offset);
        }
        return new JsonModel(elementMesh.getParent(), elementMesh.getTextureSize(),
                textureMap, modelElements, elementMesh.useAmbientOcclusion(), elementMesh.getGuiLight(),
                elementMesh.getTransformations(), elementMesh.getOverrides());
    }
}

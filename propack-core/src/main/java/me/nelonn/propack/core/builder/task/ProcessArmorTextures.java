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

package me.nelonn.propack.core.builder.task;

import com.google.gson.JsonObject;
import me.nelonn.flint.path.Path;
import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.file.File;
import me.nelonn.propack.builder.file.JsonFile;
import me.nelonn.propack.builder.file.RealFile;
import me.nelonn.propack.builder.file.TextFile;
import me.nelonn.propack.builder.task.TaskIO;
import me.nelonn.propack.builder.util.Extra;
import me.nelonn.propack.core.builder.asset.ArmorTextureBuilder;
import me.nelonn.propack.builder.task.AbstractTask;
import me.nelonn.propack.builder.task.FileProcessingException;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.core.util.GsonHelper;
import me.nelonn.propack.core.util.PathUtil;
import me.nelonn.propack.core.util.Util;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcessArmorTextures extends AbstractTask {
    public static final TaskBootstrap BOOTSTRAP = ProcessArmorTextures::new;
    public static final Extra<Integer> EXTRA_ARMOR_RESOLUTION = new Extra<>(Integer.class, "propack.process_armor_textures.armor_resolution");
    private static final String LEATHER_LAYER = "assets/minecraft/textures/models/armor/leather_layer_";
    private static final String INCLUDED_LEATHER_LAYER = "include/" + LEATHER_LAYER;
    private static final int DEFAULT_RESOLUTION = 16;
    private static final int HEIGHT_RATIO = 2;
    private static final int WIDTH_RATIO = 4;

    public ProcessArmorTextures(@NotNull Project project) {
        super("processArmorTextures", project);
    }

    @Override
    public void run(@NotNull TaskIO io) throws Exception {
        Map<Color, Armor> armors = new HashMap<>();
        for (File file : io.getFiles()) {
            try {
                String filePath = file.getPath();
                if (!filePath.startsWith("content/") || !filePath.endsWith(".armor.json") || !(file instanceof JsonFile)) continue;
                io.getFiles().removeFile(filePath);
                JsonObject jsonObject = ((JsonFile) file).getContent();
                Path resourcePath = PathUtil.resourcePath(filePath, ".armor.json");
                Color color;
                if (GsonHelper.hasString(jsonObject, "Color")) {
                    String hex = jsonObject.get("Color").getAsString();
                    color = Util.hexToRGB(hex);
                } else if (GsonHelper.hasJsonObject(jsonObject, "Color")) {
                    JsonObject rgbObject = jsonObject.getAsJsonObject("Color");
                    int r = GsonHelper.getShort(rgbObject, "r");
                    int g = GsonHelper.getShort(rgbObject, "g");
                    int b = GsonHelper.getShort(rgbObject, "b");
                    color = new Color(r, g, b);
                } else {
                    throw new IllegalArgumentException("Color not defined");
                }
                if (color.equals(Color.WHITE)) {
                    throw new IllegalArgumentException(color + " is not allowed");
                }
                Armor.Layer layer1 = loadConfiguration(io, jsonObject, "Layer1", resourcePath);
                Armor.Layer layer2 = loadConfiguration(io, jsonObject, "Layer2", resourcePath);
                if (layer1 == null && layer2 == null) {
                    throw new IllegalArgumentException("The armor must have at least 1 layer");
                }
                Armor armor = new Armor(color, layer1, layer2);
                armors.put(color, armor);
                io.getAssets().putArmorTexture(new ArmorTextureBuilder(resourcePath).setColor(color)
                        .setHasLayer1(layer1 != null).setHasLayer2(layer2 != null));
            } catch (Exception e) {
                throw new FileProcessingException(file.getPath(), e);
            }
        }
        int resolution = DEFAULT_RESOLUTION;
        if (!armors.isEmpty()) {
            int maxHeight = DEFAULT_RESOLUTION * HEIGHT_RATIO;
            for (Armor armor : armors.values()) {
                if (armor.getLayer1() != null) {
                    BufferedImage image = armor.getLayer1().getImage();
                    resolution = Math.max(image.getWidth() / WIDTH_RATIO, resolution);
                    maxHeight = Math.max(image.getHeight(), maxHeight);
                }
                if (armor.getLayer2() != null) {
                    BufferedImage image = armor.getLayer1().getImage();
                    resolution = Math.max(image.getWidth() / WIDTH_RATIO, resolution);
                    maxHeight = Math.max(image.getHeight(), maxHeight);
                }
            }
            compileLayer(io, armors, 1, resolution, maxHeight);
            compileLayer(io, armors, 2, resolution, maxHeight);
        }
        io.getExtras().put(EXTRA_ARMOR_RESOLUTION, resolution);
        File file = io.getFiles().getFile("include/assets/minecraft/shaders/core/rendertype_armor_cutout_no_cull.fsh");
        if (file instanceof TextFile) {
            TextFile textFile = (TextFile) file;
            textFile.setContent(textFile.getContent().replace("<#ARMOR_RESOLUTION#>", String.valueOf(resolution)));
        }
    }

    private Armor.Layer loadConfiguration(TaskIO io, JsonObject root, String layerKey, Path contentPath) throws IOException {
        String layerImagePath;
        boolean saveImage = false;
        int frames = 1;
        int speed = 24;
        boolean interpolation = false;
        int emissivity = 0;
        if (GsonHelper.hasString(root, layerKey)) {
            layerImagePath = root.get(layerKey).getAsString();
        } else if (GsonHelper.hasJsonObject(root, layerKey)) {
            JsonObject jsonObject = root.getAsJsonObject(layerKey);
            layerImagePath = GsonHelper.getString(jsonObject, "Image");
            saveImage = GsonHelper.getBoolean(jsonObject, "SaveImage", saveImage);
            frames = GsonHelper.getInt(jsonObject, "Frames", frames);
            speed = GsonHelper.getInt(jsonObject, "Speed", speed);
            interpolation = GsonHelper.getBoolean(jsonObject, "Interpolation", interpolation);
            emissivity = GsonHelper.getInt(jsonObject, "Emissivity", emissivity);
        } else {
            return null;
        }
        Path path = PathUtil.resolve(layerImagePath, contentPath);
        String pathValue = path.getValue();
        if (!pathValue.endsWith(".png")) {
            pathValue += ".png";
        }
        File pngFile = io.getFiles().getFile(PathUtil.join("content", path.getNamespace(), pathValue));
        if (pngFile == null) {
            String assetsPath = PathUtil.join("assets", path.getNamespace(), "textures", pathValue);
            pngFile = io.getFiles().getFile(assetsPath);
            if (pngFile == null) {
                pngFile = io.getFiles().getFile("include/" + assetsPath);
                if (pngFile == null) {
                    throw new IllegalArgumentException("File not found " + path);
                }
            }
        }
        try (InputStream inputStream = pngFile.openInputStream()) {
            BufferedImage layerImage = ImageIO.read(inputStream);
            if (!saveImage) {
                io.getFiles().removeFile(pngFile.getPath());
            }
            return new Armor.Layer(layerImage, frames, speed, interpolation, emissivity);
        }
    }

    private void compileLayer(TaskIO io, Map<Color, Armor> armors, int layerId, int resolution, int maxHeight) throws IOException {
        File layerDefaultFile = io.getFiles().getFile(INCLUDED_LEATHER_LAYER + layerId + ".png");
        if (layerDefaultFile == null) {
            throw new IllegalArgumentException("'" + INCLUDED_LEATHER_LAYER + layerId + ".png' not found");
        }
        BufferedImage leatherLayer = ImageIO.read(layerDefaultFile.openInputStream());
        List<Armor> specificLayerArmors = armors.values().stream()
                .filter(layerId == 1 ? armor -> armor.getLayer1() != null : armor -> armor.getLayer2() != null)
                .collect(Collectors.toList());
        BufferedImage resultLayer = new BufferedImage((specificLayerArmors.size() + 1) * resolution * WIDTH_RATIO,
                maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resultLayer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        addImage(g2, 0, leatherLayer, resolution);
        setPixel(g2, 0, 1, Color.WHITE);
        int i = 1;
        for (Armor armor : specificLayerArmors) {
            Armor.Layer layer = layerId == 1 ? armor.getLayer1() : armor.getLayer2();
            assert layer != null;
            addImage(g2, i, layer.getImage(), resolution);
            int x = resolution * WIDTH_RATIO * i;
            setPixel(g2, x, 0, armor.getColor());
            if (layer.isAnimated()) {
                setPixel(g2, x + 1, 0, new Color(layer.getFrames(), layer.getSpeed(), layer.isInterpolation() ? 1 : 0));
            }
            if (layer.getEmissivity() > 0) {
                setPixel(g2, x + 2, 0, new Color(layer.getEmissivity(), 0, 0));
            }
            i++;
        }
        g2.dispose();
        java.io.File tempLayerFile = new java.io.File(io.getTempDirectory(), LEATHER_LAYER + layerId + ".png");
        tempLayerFile.getParentFile().mkdirs();
        ImageIO.write(resultLayer, "png", tempLayerFile);
        io.getFiles().addFile(new RealFile(LEATHER_LAYER + layerId + ".png", tempLayerFile));
        io.getFiles().removeFile(layerDefaultFile.getPath());
    }

    private void addImage(Graphics2D g2, int index, BufferedImage image, int resolution) {
        int width = resolution * WIDTH_RATIO;
        g2.drawImage(image, width * index, 0, width, image.getHeight() * (resolution / DEFAULT_RESOLUTION), null);
    }

    private void setPixel(Graphics2D g2, int x, int y, Color color) {
        g2.setColor(color);
        g2.drawLine(x, y, x, y);
    }

    public static class Armor {
        private final Color color;
        private final Layer layer1;
        private final Layer layer2;

        public Armor(Color color, Layer layer1, Layer layer2) {
            this.color = color;
            this.layer1 = layer1;
            this.layer2 = layer2;
        }

        public Color getColor() {
            return color;
        }

        public Layer getLayer1() {
            return layer1;
        }

        public Layer getLayer2() {
            return layer2;
        }

        public static class Layer {
            private final BufferedImage image;
            private final int frames;
            private final int speed;
            private final boolean interpolation;
            private final int emissivity;

            public Layer(BufferedImage image, int frames, int speed, boolean interpolation, int emissivity) {
                this.image = image;
                this.frames = Math.max(1, frames);
                this.speed = speed;
                this.interpolation = interpolation;
                this.emissivity = emissivity;
            }

            public BufferedImage getImage() {
                return image;
            }

            public int getFrames() {
                return frames;
            }

            public int getSpeed() {
                return speed;
            }

            public boolean isInterpolation() {
                return interpolation;
            }

            public int getEmissivity() {
                return emissivity;
            }

            public boolean isAnimated() {
                return frames > 1;
            }
        }
    }

}

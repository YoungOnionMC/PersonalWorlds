package personalworlds.gui;

import static personalworlds.world.Enums.DaylightCycle.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CategoryList;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import personalworlds.PWConfig;
import personalworlds.packet.Packets;
import personalworlds.world.DimensionConfig;

public class PWGuiMUI {

    private int targetDim;
    private int dimID;
    private BlockPos blockPos;
    private final IDrawable checkmark = UITexture.builder().imageSize(16, 16).location("personalworlds", "checkmark")
            .build();
    private final IDrawable crossmark = UITexture.builder().imageSize(16, 16).location("personalworlds", "crossmark")
            .build();
    private final IDrawable moon = UITexture.builder().imageSize(16, 16)
            .uv((float) 80 / 256, (float) 32 / 256, (float) (80 + 16) / 256, (float) (32 + 16) / 256)
            .location("personalworlds", "widgets")
            .build();
    private final IDrawable sun = UITexture.builder().imageSize(16, 16)
            .uv((float) 96 / 256, (float) 32 / 256, (float) (96 + 16) / 256, (float) (32 + 16) / 256)
            .location("personalworlds", "widgets")
            .build();
    private final IDrawable sun_moon = UITexture.builder().imageSize(16, 16)
            .uv((float) 112 / 256, (float) 32 / 256, (float) (112 + 16) / 256, (float) (32 + 16) / 256)
            .location("personalworlds", "widgets")
            .build();
    private List<String> layers = new ArrayList<>();
    private ModularPanel panel;
    private DimensionConfig dimensionConfig;
    private ListWidget layersWidget;
    private ParentWidget<?> skyWidget = new ParentWidget<>()
            .size(80, 70)
            .top(60).left(80);
    private int skyR;
    private int skyG;
    private int skyB;
    private final CategoryList biomesWidget = new CategoryList()
            .bottom(35).left(12)
            .size(90, 20);
    private final CategoryList presetsWidget = new CategoryList().background(GuiTextures.MC_BUTTON)
            .bottom(35).right(15)
            .size(90, 20);
    private boolean firstDraw = true;
    private final IStringValue<String> name;

    public PWGuiMUI(int targetDim, int dimID, int x, int y, int z, String name) {
        this.targetDim = targetDim;
        this.dimID = dimID;
        this.blockPos = new BlockPos(x, y, z);
        this.name = new StringValue(name);
    }

    public ModularScreen createGUI() {
        if (targetDim == 0) {
            dimensionConfig = dimID == 0 ? new DimensionConfig(0) : DimensionConfig.getForDimension(dimID, true);
        } else {
            dimensionConfig = DimensionConfig.getForDimension(targetDim, true);
        }
        TextFieldWidget nameWidget = new TextFieldWidget()
                .top(17).left(7)
                .size(100, 20);
        skyR = ((dimensionConfig.getSkyColor() >> 16) & 0xFF);
        skyG = ((dimensionConfig.getSkyColor() >> 8) & 0xFF);
        skyB = ((dimensionConfig.getSkyColor()) & 0xFF);
        final ArrayList<IWidget> blockList = new ArrayList<>();
        if (dimensionConfig.allowGenerationChanges()) {
            for (IBlockState blockState : PWConfig.getAllowedBlocks()) {
                Block block = blockState.getBlock();
                int itemMeta = block.damageDropped(blockState);
                int meta = block.getMetaFromState(blockState);
                ItemStack stack = new ItemStack(block, 1, itemMeta);
                blockList.add(new ButtonWidget<>().size(22, 22)
                        .overlay(new ItemDrawable(stack))
                        .addTooltipLine(stack.getDisplayName())
                        .tooltipScale(0.8F)
                        .onMousePressed(i -> {
                            layers.add(new FlatLayerInfo(3, 1, block, meta).toString());
                            redrawLayers();
                            return true;
                        }));
            }
        }
        ModularPanel panel = ModularPanel.defaultPanel("PWGUI");
        this.panel = panel;
        panel.size(350, 250);
        panel.child(IKey.str("Portal Name").asWidget()
                .top(7).left(7));
        panel.child(nameWidget
                .value(name));
        panel.child(new ButtonWidget<>()
                .overlay(IKey.str("Done"))
                .size(60, 20)
                .bottom(9).right(9)
                .onMousePressed(i -> {
                    Packets.INSTANCE.sendChangeWorldSettings(dimID, blockPos, name.getStringValue(), dimensionConfig)
                            .sendToServer();
                    panel.closeIfOpen();
                    return true;
                }));
        panel.child(new ButtonWidget<>()
                .overlay(IKey.str("Cancel"))
                .size(60, 20)
                .bottom(9).left(9)
                .onMousePressed(i -> {
                    panel.closeIfOpen();
                    return true;
                }));
        panel.child(IKey.str("Trees").asWidget()
                .bottom(170).left(40));
        panel.child(IKey.str("Vegetation").asWidget()
                .bottom(145).left(40));
        panel.child(IKey.str("Clouds").asWidget()
                .bottom(170).left(120));
        panel.child(IKey.str("Spawn peaceful").asWidget()
                .bottom(170).left(200));
        panel.child(IKey.str("Spawn hostile").asWidget()
                .bottom(145).left(200));
        panel.child(IKey.str("Weather").asWidget()
                .bottom(145).left(120));
        panel.child(new SliderWidget()
                .size(125, 17)
                .top(40).left(7)
                .value(new DoubleValue(dimensionConfig.getStarVisibility()))
                .onUpdateListener(widget -> {
                    dimensionConfig.setStarVisibility((float) widget.getSliderValue());
                    widget.overlay(IKey.str(
                            String.format("Star Brightness: %.0f", dimensionConfig.getStarVisibility() * 100) + "%")
                            .color(0xFFFFFF));
                })
                .bounds(0F, 1F)
                .background(GuiTextures.MC_BUTTON_DISABLED));
        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .bottom(75).left(14)
                .overlay(crossmark)
                .background(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON :
                        GuiTextures.MC_BUTTON_DISABLED)
                .hoverBackground(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON_HOVERED :
                        GuiTextures.MC_BUTTON_DISABLED)
                .onMousePressed(i -> {
                    if (dimensionConfig.allowGenerationChanges()) {
                        dimensionConfig.setGeneratingTrees(!dimensionConfig.generateTrees());
                    }
                    return true;
                })
                .onUpdateListener(widget -> {
                    widget.overlay(dimensionConfig.generateTrees() ? checkmark : crossmark);
                }));
        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .bottom(100).left(14)
                .overlay(crossmark)
                .background(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON :
                        GuiTextures.MC_BUTTON_DISABLED)
                .hoverBackground(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON_HOVERED :
                        GuiTextures.MC_BUTTON_DISABLED)
                .onMousePressed(i -> {
                    if (dimensionConfig.allowGenerationChanges()) {
                        dimensionConfig.setGeneratingVegetation(!dimensionConfig.generateVegetation());
                    }
                    return true;
                })
                .onUpdateListener(widget -> {
                    widget.overlay(dimensionConfig.generateVegetation() ? checkmark : crossmark);
                }));
        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .bottom(75).left(176)
                .overlay(crossmark)
                .background(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON :
                        GuiTextures.MC_BUTTON_DISABLED)
                .hoverBackground(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON_HOVERED :
                        GuiTextures.MC_BUTTON_DISABLED)
                .onMousePressed(i -> {
                    if (dimensionConfig.allowGenerationChanges()) {
                        dimensionConfig.setSpawnPassiveMobs(!dimensionConfig.spawnPassiveMobs());
                    }
                    return true;
                })
                .onUpdateListener(widget -> {
                    widget.overlay(dimensionConfig.spawnPassiveMobs() ? checkmark : crossmark);
                }));
        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .bottom(100).left(176)
                .overlay(crossmark)
                .background(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON :
                        GuiTextures.MC_BUTTON_DISABLED)
                .hoverBackground(dimensionConfig.allowGenerationChanges() ? GuiTextures.MC_BUTTON_HOVERED :
                        GuiTextures.MC_BUTTON_DISABLED)
                .onMousePressed(i -> {
                    if (dimensionConfig.allowGenerationChanges()) {
                        dimensionConfig.setSpawnMonsters(!dimensionConfig.spawnMonsters());
                    }
                    return true;
                })
                .onUpdateListener(widget -> {
                    widget.overlay(dimensionConfig.spawnMonsters() ? checkmark : crossmark);
                }));
        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .bottom(100).left(95)
                .overlay(crossmark)
                .onMousePressed(i -> {
                    dimensionConfig.enableWeather(!dimensionConfig.weatherEnabled());
                    return true;
                })
                .onUpdateListener(widget -> {
                    widget.overlay(dimensionConfig.weatherEnabled() ? checkmark : crossmark);
                }));
        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .bottom(75).left(95)
                .overlay(crossmark)
                .onMousePressed(i -> {
                    dimensionConfig.enableClouds(!dimensionConfig.cloudsEnabled());
                    return true;
                })
                .onUpdateListener(widget -> {
                    widget.overlay(dimensionConfig.cloudsEnabled() ? checkmark : crossmark);
                }));
        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .top(39).left(142)
                .overlay(sun)
                .onMousePressed(i -> {
                    switch (dimensionConfig.getDaylightCycle()) {
                        case SUN -> dimensionConfig.setDaylightCycle(MOON);
                        case MOON -> dimensionConfig.setDaylightCycle(CYCLE);
                        case CYCLE -> dimensionConfig.setDaylightCycle(SUN);
                    }
                    return true;
                })
                .onUpdateListener(widget -> {
                    switch (dimensionConfig.getDaylightCycle()) {
                        case SUN -> widget.overlay(sun);
                        case MOON -> widget.overlay(moon);
                        case CYCLE -> widget.overlay(sun_moon);
                    }
                }));

        panel.child(new SliderWidget()
                .size(70, 15)
                .top(60).left(7)
                .background(GuiTextures.MC_BUTTON_DISABLED)
                .value(new DoubleValue(skyR))
                .bounds(0, 255)
                .onUpdateListener(widget -> {
                    skyR = (int) widget.getSliderValue();
                    widget.overlay(IKey.str(
                            String.format("Red: %s", skyR))
                            .color(0xFFFFFF));
                }));
        panel.child(new SliderWidget()
                .size(70, 15)
                .top(80).left(7)
                .background(GuiTextures.MC_BUTTON_DISABLED)
                .value(new DoubleValue(skyG))
                .bounds(0, 255)
                .onUpdateListener(widget -> {
                    skyG = (int) widget.getSliderValue();
                    widget.overlay(IKey.str(
                            String.format("Green: %s", skyG))
                            .color(0xFFFFFF));
                }));
        panel.child(new SliderWidget()
                .size(70, 15)
                .top(100).left(7)
                .background(GuiTextures.MC_BUTTON_DISABLED)
                .value(new DoubleValue(skyB))
                .bounds(0, 255)
                .onUpdateListener(widget -> {
                    skyB = (int) widget.getSliderValue();
                    widget.overlay(IKey.str(
                            String.format("Blue: %s", skyB))
                            .color(0xFFFFFF));
                }));
        panel.child(skyWidget
                .onUpdateListener(widget -> {
                    dimensionConfig.setSkyColor((skyR << 16) | (skyG << 8) | skyB);
                    widget.overlay(new Rectangle().setColor(0xFF000000 | dimensionConfig.getSkyColor()));
                })
                .child(new Widget<>()
                        .align(Alignment.TopRight)
                        .size(14, 14)
                        .onUpdateListener(widget -> {
                            widget.overlay(new Star(dimensionConfig.getStarVisibility()));
                        }))
                .child(new Widget<>()
                        .align(Alignment.TopLeft)
                        .size(14, 14)
                        .onUpdateListener(widget -> {
                            widget.overlay(new Star(dimensionConfig.getStarVisibility()));
                        }))
                .child(new Widget<>()
                        .align(Alignment.BottomCenter)
                        .size(14, 14)
                        .onUpdateListener(widget -> {
                            widget.overlay(new Star(dimensionConfig.getStarVisibility()));
                        })));
        if (dimensionConfig.allowGenerationChanges()) {
            panel.child(IKey.str("Layers").asWidget()
                    .top(7).right(320));
            panel.child(IKey.str("Biome").asWidget()
                    .bottom(190).left(45));
            panel.child(IKey.str("Presets").asWidget()
                    .bottom(190).left(270));
            panel.child(new ListWidget<>(blockList)
                    .top(20).right(50)
                    .size(22, 150));
            redrawBiomeList();
            redrawPresets();
        }
        this.firstDraw = false;
        return new ModularScreen(panel);
    }

    private void redrawBiomeList() {
        if (!firstDraw) {
            panel.getChildren().removeIf(widget -> widget.equals(biomesWidget));
        }
        biomesWidget.getChildren().clear();
        for (Biome biome : PWConfig.getAllowedBiomes()) {
            if (biome == dimensionConfig.getBiome()) {
                biomesWidget.background(GuiTextures.MC_BUTTON)
                        .overlay(IKey.str(biome.getBiomeName()).color(0xFFFFFF));
            } else {
                biomesWidget.child(new ButtonWidget<>().size(90, 20)
                        .overlay(IKey.str(biome.getBiomeName()))
                        .onMousePressed(mouse -> {
                            dimensionConfig.setBiome(biome);
                            redrawBiomeList();
                            return true;
                        }));
            }
        }
        panel.child(biomesWidget);
        if (!firstDraw) {
            WidgetTree.resize(panel);
            WidgetTree.resize(biomesWidget);
        }
    }

    private void redrawPresets() {
        boolean preset = false;
        if (!firstDraw) {
            panel.getChildren().removeIf(widget -> widget.equals(presetsWidget));
        }
        presetsWidget.getChildren().clear();
        for (Map.Entry<String, String> entry : PWConfig.getPresets().entrySet()) {
            if (layers.isEmpty() && !preset) {
                presetsWidget.overlay(IKey.str("Void").color(0xFFFFFF));
                preset = true;
            }
            if (layers.equals(fromPreset(entry.getValue()))) {
                presetsWidget.overlay(IKey.str(entry.getKey()).color(0xFFFFFF));
                preset = true;
            } else {
                presetsWidget.child(new ButtonWidget<>()
                        .size(90, 20)
                        .overlay(IKey.str(entry.getKey()))
                        .onMousePressed(mouse -> {
                            this.layers = fromPreset(entry.getValue());
                            redrawLayers();
                            return true;
                        }));
            }
        }
        if (!preset) {
            presetsWidget.overlay(IKey.str("Custom").color(0xFFFFFF));
        }
        panel.child(presetsWidget);
        if (!firstDraw) {
            WidgetTree.resize(panel);
            WidgetTree.resize(presetsWidget);
        }
    }

    private void redrawLayers() {
        if (layersWidget != null) {
            panel.getChildren().removeIf(widget -> widget.equals(layersWidget));
        }
        ArrayList<IWidget> layerWidget = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            FlatLayerInfo layerInfo = DimensionConfig.LayerFromString(layers.get(i));
            AtomicInteger layerCount = new AtomicInteger(layerInfo.getLayerCount());
            IBlockState blockState = layerInfo.getLayerMaterial();
            Block block = layerInfo.getLayerMaterial().getBlock();
            int itemMeta = block.damageDropped(blockState);
            int meta = block.getMetaFromState(blockState);
            ItemStack stack = new ItemStack(block, 1, itemMeta);
            boolean arrowDown = i != layers.size() - 1 && layers.size() != 1;
            boolean arrowUp = i > 0;
            AtomicInteger finalI = new AtomicInteger(i);
            layerWidget.add(i, new ParentWidget<>().size(22, 22)
                    .overlay(new ItemDrawable(stack))
                    .addTooltipLine(stack.getDisplayName())
                    .tooltipScale(0.8F)
                    .child(IKey.str(Integer.toString(layerCount.get())).color(0xFFFFF).asWidget()
                            .align(Alignment.BottomCenter))
                    .child(new ButtonWidget<>().size(6, 6)
                            .align(Alignment.TopLeft)
                            .overlay(GuiTextures.ADD)
                            .addTooltipLine("Increase")
                            .tooltipScale(0.8F)
                            .onMousePressed(mouse -> {
                                FlatLayerInfo newLayer = new FlatLayerInfo(3, layerCount.incrementAndGet(), block,
                                        meta);
                                layers.set(finalI.get(), newLayer.toString());
                                redrawLayers();
                                return true;
                            }))
                    .child(new ButtonWidget<>().size(6, 6)
                            .align(Alignment.BottomLeft)
                            .overlay(GuiTextures.REMOVE)
                            .addTooltipLine("Decrease")
                            .tooltipScale(0.8F)
                            .onMousePressed(mouse -> {
                                if (layerCount.get() == 1) {
                                    return true;
                                }
                                FlatLayerInfo newLayer = new FlatLayerInfo(3, layerCount.decrementAndGet(), block,
                                        meta);
                                layers.set(finalI.get(), newLayer.toString());
                                redrawLayers();
                                return true;
                            }))
                    .child(new ButtonWidget<>().size(6, 6)
                            .align(Alignment.CenterLeft)
                            .overlay(GuiTextures.CROSS_TINY)
                            .addTooltipLine("Remove")
                            .tooltipScale(0.8F)
                            .onMousePressed(mouse -> {
                                layers.remove(finalI.get());
                                redrawLayers();
                                return true;
                            }))
                    .childIf(arrowUp, new ButtonWidget<>().size(6, 6)
                            .align(Alignment.TopRight)
                            .overlay(GuiTextures.MOVE_UP)
                            .addTooltipLine("Move up")
                            .tooltipScale(0.8F)
                            .onMousePressed(mouse -> {
                                Collections.swap(layers, finalI.getAndDecrement(), finalI.get());
                                redrawLayers();
                                return true;
                            }))
                    .childIf(arrowDown, new ButtonWidget<>().size(6, 6)
                            .align(Alignment.BottomRight)
                            .overlay(GuiTextures.MOVE_DOWN)
                            .addTooltipLine("Move down")
                            .tooltipScale(0.8F)
                            .onMousePressed(mouse -> {
                                Collections.swap(layers, finalI.getAndIncrement(), finalI.get());
                                redrawLayers();
                                return true;
                            })));

        }
        dimensionConfig.setLayers(toPreset(layers));
        layersWidget = new ListWidget<>(layerWidget)
                .top(20).right(20)
                .size(22, 150);
        panel.child(layersWidget);
        WidgetTree.resize(panel);
        WidgetTree.resize(layersWidget);
        redrawPresets();
    }

    private String toPreset(List<String> layerList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i != layerList.size(); i++) {
            sb.append(layerList.get(i));
            if (i != layerList.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private List<String> fromPreset(String preset) {
        ArrayList<String> layers = new ArrayList<>();
        if (preset.contains(",")) {
            String[] stringArray = preset.split(",");
            for (int i = stringArray.length - 1; i > -1; --i) {
                layers.add(stringArray[i]);
            }
        } else {
            layers.add(preset);
        }
        return layers;
    }

    private static class Star implements IDrawable {

        private final float brightness;

        public Star(float brightness) {
            this.brightness = brightness;
        }

        @SideOnly(Side.CLIENT)
        @Override
        public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
            UITexture.builder().imageSize(16, 16)
                    .uv((float) 32 / 256, (float) 32 / 256, (float) (32 + 16) / 256, (float) (32 + 16) / 256)
                    .location("personalworlds", "widgets")
                    .build().draw(x0, y0, width, height);
        }
    }
}

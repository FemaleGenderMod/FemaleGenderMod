/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.Configuration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2i;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WildfireBreastUVEditorScreen extends BaseWildfireScreen {

    private static final Text TITLE = Text.translatable("wildfire_gender.uv_editor");

    private int positionIncrementValue = 1;

    private static final Identifier TEXTURE_ADD = Identifier.of(WildfireGender.MODID, "textures/gui/widgets/add.png");
    private static final Identifier TEXTURE_SUBTRACT = Identifier.of(WildfireGender.MODID, "textures/gui/widgets/subtract.png");

    private final int[] FACE_COLORS = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF00FFFF, 0xFFFF00FF };
    private final int[] FADED_FACE_COLORS = new int[] { 0x33FF0000, 0x3300FF00, 0x330000FF, 0x3300FFFF, 0x33FF00FF };
    private final String[] SHORT_FACE_NAMES = new String[] { "E", "W", "D", "U", "N" };
    private final String[] FACE_NAMES_LEFT = new String[] {
            "wildfire_gender.uv_editor.faces.inner",
            "wildfire_gender.uv_editor.faces.outer",
            "wildfire_gender.uv_editor.faces.top",
            "wildfire_gender.uv_editor.faces.bottom",
            "wildfire_gender.uv_editor.faces.front",
    };
    private final String[] FACE_NAMES_RIGHT = new String[] {
            "wildfire_gender.uv_editor.faces.outer",
            "wildfire_gender.uv_editor.faces.inner",
            "wildfire_gender.uv_editor.faces.top",
            "wildfire_gender.uv_editor.faces.bottom",
            "wildfire_gender.uv_editor.faces.front",
    };


    private int[][] selectedUVs;


    private ClickableWidget[] positionWidgets = new ClickableWidget[0];

    private enum BreastTypes {
        LEFT, RIGHT, LEFT_OVERLAY, RIGHT_OVERLAY
    }

    private enum UVFaces {
        NONE, EAST, WEST, DOWN, UP, NORTH
    }
    private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
    private UVFaces selectedFace = UVFaces.NONE; // -1 means none selected

    //Positions & Widths
    private int sidebarWidth = 260;
    private Vector2i winElementPos;
    private Vector2i uvWindowPos;
    private int textureDrawWidth;
    private float uvWindowScaleFactor;

    private ClickableWidget btnLeftBreast, btnRightBreast, btnLeftBreastOverlay, btnRightBreastOverlay;

    public WildfireBreastUVEditorScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.uv_editor"), parent, uuid);
    }

    @Override
    public void init() {
        if(client == null) return;
        
        textureDrawWidth = 196;
        int textureSourceWidth = 64;
        uvWindowScaleFactor = (float) textureDrawWidth / (float) textureSourceWidth;
        uvWindowPos = new Vector2i(5, this.height / 2 - textureDrawWidth / 2);

        sidebarWidth = 180;
        winElementPos = new Vector2i(this.width - sidebarWidth + 7, 32);

        int x = this.width - sidebarWidth;
        int w = this.width - (this.width - sidebarWidth);
        int y = 0;

        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.reset_defaults_all"))
                .position(x + 5, y + 5)
                .size(this.width - x - 10, 20)
                .onPress(button -> {
                    if(getPlayer() == null) return;

                    getPlayer().updateLeftBreastUVLayout(Configuration.LEFT_BREAST_UV_LAYOUT.getDefault());
                    getPlayer().updateRightBreastUVLayout(Configuration.RIGHT_BREAST_UV_LAYOUT.getDefault());

                    getPlayer().updateLeftBreastOverlayUVLayout(Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT.getDefault());
                    getPlayer().updateRightBreastOverlayUVLayout(Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT.getDefault());

                    getPlayer().updateLeftBreastArmorUVLayout(Configuration.LEFT_BREAST_ARMOR_UV_LAYOUT.getDefault());
                    getPlayer().updateRightBreastArmorUVLayout(Configuration.RIGHT_BREAST_ARMOR_UV_LAYOUT.getDefault());

                    getPlayer().save();
                }));

        btnLeftBreast = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.left_breast"))
                .position(winElementPos.x(), winElementPos.y() + 13)
                .size((w / 2) / 2 - 5, 15)
                .active(selectedBreastIndex != BreastTypes.LEFT)
                .onPress(button -> selectBreastUVMap(BreastTypes.LEFT)));

        btnRightBreast = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast"))
                .position(winElementPos.x() + (w / 2) / 2 - 3, winElementPos.y() + 13)
                .size((w / 2) / 2 - 6, 15)
                .active(selectedBreastIndex != BreastTypes.RIGHT)
                .onPress(button -> selectBreastUVMap(BreastTypes.RIGHT)));

        btnLeftBreastOverlay = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.left_breast_overlay"))
                .position(winElementPos.x(), winElementPos.y() + 44)
                .size((w / 2) / 2 - 5, 15)
                .active(selectedBreastIndex != BreastTypes.LEFT_OVERLAY)
                .onPress(button -> selectBreastUVMap(BreastTypes.LEFT_OVERLAY)));

        btnRightBreastOverlay = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast_overlay"))
                .position(winElementPos.x() + (w / 2) / 2 - 3, winElementPos.y() + 44)
                .size((w / 2) / 2 - 6, 15)
                .active(selectedBreastIndex != BreastTypes.RIGHT_OVERLAY)
                .onPress(button -> selectBreastUVMap(BreastTypes.RIGHT_OVERLAY)));

        //Position stuff
        if(selectedFace != UVFaces.NONE) {
            int uvPositionWindowX = this.width - 130 + 5;

            positionWidgets = new ClickableWidget[8];

            int buttonArrayY = 52;

            for (int i = 0; i < 8; i++) {
                boolean isAdd = (i % 2 == 1);
                int uvIndex = i / 2;
                int delta = isAdd ? 1 : -1;

                int xOffset = isAdd ? 106 : 92;
                int yOffset = (i / 2) * 14;

                positionWidgets[i] = addButton(builder -> builder
                        .renderer((button, ctx, mouseX, mouseY, partialTicks) -> {
                            Formatting colorVal = positionIncrementValue == 10 ? Formatting.AQUA :
                                    (positionIncrementValue == 20 ? Formatting.BLUE : Formatting.WHITE);
                            ctx.drawTexture(RenderPipelines.GUI_TEXTURED,
                                    isAdd ? TEXTURE_ADD : TEXTURE_SUBTRACT,
                                    button.getX() + button.getWidth() / 2 - 3,
                                    button.getY() + button.getHeight() / 2 - 3,
                                    0,0,6,6,6,6,6,6,
                                    ColorHelper.fullAlpha(colorVal.getColorValue()));
                        })
                        .message(() -> Text.literal(isAdd ? "Add" : "Remove"))
                        .position(uvPositionWindowX + xOffset, y + buttonArrayY + yOffset)
                        .size(12, 12)
                        .onPress(button -> {
                            selectedUVs[selectedFace.ordinal()][uvIndex] += delta * positionIncrementValue;
                            if (uvIndex == 0) selectedUVs[selectedFace.ordinal()][2] += delta * positionIncrementValue;
                            if (uvIndex == 1) selectedUVs[selectedFace.ordinal()][3] += delta * positionIncrementValue;

                            getPlayer().save();
                        })
                );
            }
        }
    }

    private void selectBreastUVMap(BreastTypes breast) {
        selectedBreastIndex = breast;
        selectedFace = UVFaces.NONE;
        updateBreastButtonStates();
        clearAndInit();
    }
    private void updateBreastButtonStates() {
        btnLeftBreast.active = selectedBreastIndex != BreastTypes.LEFT;
        btnRightBreast.active = selectedBreastIndex != BreastTypes.RIGHT;
        btnLeftBreastOverlay.active = selectedBreastIndex != BreastTypes.LEFT_OVERLAY;
        btnRightBreastOverlay.active = selectedBreastIndex != BreastTypes.RIGHT_OVERLAY;
    }


    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        //super.renderBackground(ctx, mouseX, mouseY, delta);
        this.renderInGameBackground(ctx);
        //ctx.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 190) / 2, (this.height - 107) / 2, 0, 0, 190, 107, 512, 512);
        int w = this.width - (this.width - sidebarWidth) - 10;

        ctx.fill(this.width - sidebarWidth, 0, this.width, this.height, 0xCC000000);
        ctx.fill(this.width - sidebarWidth + 5, 30, this.width - w / 2 - 5, 93, 0x66000000);
        ctx.fill(this.width - w / 2, 30, this.width - 5, 128, 0x66000000);

        ctx.fill(uvWindowPos.x() - 2, uvWindowPos.y() - 2, uvWindowPos.x() + textureDrawWidth + 2, uvWindowPos.y() + textureDrawWidth + 2, 0xCC000000);
        ctx.fill(uvWindowPos.x(), uvWindowPos.y(), uvWindowPos.x() + textureDrawWidth, uvWindowPos.y() + textureDrawWidth, 0xFFFFFFFF);
    }


    @Override
    public void tick() {
        if(getPlayer() == null) return;

        selectedUVs = switch (selectedBreastIndex) {
            case BreastTypes.RIGHT -> getPlayer().getRightBreastUVLayout();
            case BreastTypes.LEFT_OVERLAY -> getPlayer().getLeftBreastOverlayUVLayout();
            case BreastTypes.RIGHT_OVERLAY -> getPlayer().getRightBreastOverlayUVLayout();
            default -> getPlayer().getLeftBreastUVLayout();
        };
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (client == null || client.world == null) return;

        if(client.player != null && getPlayer() != null && selectedUVs != null) {

            ctx.drawTexture(RenderPipelines.GUI_TEXTURED, client.player.getSkin().body().id(),
                    uvWindowPos.x(), uvWindowPos.y(),
                    0, 0, textureDrawWidth, textureDrawWidth, textureDrawWidth, textureDrawWidth);

            //Other faces
            int[][][] ALL_UVS = new int[][][] {
                    getPlayer().getLeftBreastUVLayout(),
                    getPlayer().getRightBreastUVLayout(),
                    getPlayer().getLeftBreastOverlayUVLayout(),
                    getPlayer().getRightBreastOverlayUVLayout()
            };

            for(int[][] eachBreast : ALL_UVS) {
                drawFaceBorders(ctx, eachBreast, mouseX, mouseY, true);
            }

            drawFaceBorders(ctx, selectedUVs, mouseX, mouseY, false);
        }

        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.selection.layer_body"),  winElementPos.x() + 42, winElementPos.y() + 2, 0xFFFFFFFF);
        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.selection.layer_jacket"),  winElementPos.x() + 42, winElementPos.y() + 32, 0xFFFFFFFF);

        int positionBoxX = this.width - sidebarWidth / 4;
        int positionBoxW = this.width - (this.width - sidebarWidth);

        //Coordinate selector
        if(selectedFace == UVFaces.NONE) {
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.no_face_selected"), positionBoxX, 60, 70, 0xFF888888);
        } else {
            String fullFaceName = "N/A";
            if(selectedBreastIndex.ordinal() % 2 == 0) {
                fullFaceName = FACE_NAMES_LEFT[selectedFace.ordinal()];
            } else {
                fullFaceName = FACE_NAMES_RIGHT[selectedFace.ordinal()];
            }

            GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable(fullFaceName).formatted(Formatting.GOLD), positionBoxX, 37, 0xFFFFFFFF);

            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.xpos"), positionBoxX - 35, 55, 0xFFFFFFFF, false);
            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.ypos"), positionBoxX - 35, 55 + 14, 0xFFFFFFFF, false);
            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.width"), positionBoxX - 35, 55 + (14*2), 0xFFFFFFFF, false);
            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.height"), positionBoxX - 35, 55 + (14*3), 0xFFFFFFFF, false);

            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(positionBoxX, 115);
            ctx.getMatrices().scale(0.75f);
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.increment_tip.line1").formatted(Formatting.AQUA), 0, -6, 120, 0xFF888888);
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.increment_tip.line2").formatted(Formatting.BLUE), 0, 6, 120, 0xFF888888);
            ctx.getMatrices().popMatrix();
        }

        int modelScale = 120;
        if(MinecraftClient.getInstance().getWindow().getWidth() < 1920) {
            modelScale = 60;
        } else if(MinecraftClient.getInstance().getWindow().getWidth() >= 2560) {
            modelScale = 200;
        }
        InventoryScreen.drawEntity(ctx, this.width / 2 - modelScale, this.height / 2 - modelScale, this.width / 2 + modelScale, this.height / 2 + modelScale, modelScale, 0.0625f, mouseX, mouseY, client.player);

        GuiUtils.drawCenteredText(ctx, textRenderer, TITLE, this.width / 2, 20, 0xFFFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }


    private void drawFaceBorders(DrawContext ctx, int[][] uvList, int mouseX, int mouseY, boolean faded) {

        int faceIndex = 0;
        //selected faces
        for (int[] faceUV : uvList) {

            int borderColor = (faceIndex == selectedFace.ordinal()) ? 0xFFFFFFFF : FACE_COLORS[faceIndex];

            if(faded) {
                borderColor = FADED_FACE_COLORS[faceIndex];
            }

            final String faceName = SHORT_FACE_NAMES[faceIndex];
            String fullFaceName = "N/A";
            if(selectedBreastIndex.ordinal() % 2 == 0) {
                fullFaceName = FACE_NAMES_LEFT[faceIndex];
            } else {
                fullFaceName = FACE_NAMES_RIGHT[faceIndex];
            }
            int u1 = faceUV[0];
            int v1 = faceUV[1];
            int u2 = faceUV[2];
            int v2 = faceUV[3];

            if(!(u1 == 0 && v1 == 0 && u2 == 0 && v2 == 0)) {
                int rectX1 = (int) (uvWindowPos.x() + (float) (u1) * uvWindowScaleFactor);
                int rectY1 = (int) (uvWindowPos.y() + (float) (v1 - 1) * uvWindowScaleFactor);
                int rectX2 = (int) (uvWindowPos.x() + (float) (u2) * uvWindowScaleFactor);
                int rectY2 = (int) (uvWindowPos.y() + (float) (v2 - 1) * uvWindowScaleFactor);

                if(mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
                    List<OrderedText> array = new ArrayList<>();
                    array.add(Text.translatable(fullFaceName).append(" (").append(faceName).append(")").formatted(Formatting.GOLD).asOrderedText());
                    array.add(Text.empty().append("[" + u1 + ", " + v1 + ", " + u2 + ", " + v2 + "]").formatted(Formatting.AQUA).asOrderedText());
                    ctx.drawTooltip(array, mouseX, mouseY);
                }

                int borderThickness = 1;
                ctx.fill(rectX1, rectY1, rectX2, rectY1 + borderThickness, borderColor);
                ctx.fill(rectX1, rectY2 - borderThickness, rectX2, rectY2, borderColor);
                ctx.fill(rectX1, rectY1, rectX1 + borderThickness, rectY2, borderColor);
                ctx.fill(rectX2 - borderThickness, rectY1, rectX2, rectY2, borderColor);

                int centerX = (rectX1 + rectX2) / 2;
                int centerY = (rectY1 + rectY2) / 2;
                int textWidth = textRenderer.getWidth(faceName);
                int textHeight = textRenderer.fontHeight;

                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(centerX, centerY);
                ctx.getMatrices().scale(0.6f);

                ctx.drawText(textRenderer, faceName, -textWidth / 2, -textHeight / 2, 0xFFFFFFFF, true);

                ctx.getMatrices().popMatrix();

            }
            faceIndex++;
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {

        int faceIndex = 0;

        for (int[] faceUV : selectedUVs) {
            int u1 = faceUV[0];
            int v1 = faceUV[1];
            int u2 = faceUV[2];
            int v2 = faceUV[3];

            if(!(u1 == 0 && v1 == 0 && u2 == 0 && v2 == 0)) {
                int rectX1 = (int) (uvWindowPos.x() + (float) (u1) * uvWindowScaleFactor);
                int rectY1 = (int) (uvWindowPos.y() + (float) (v1 - 1) * uvWindowScaleFactor);
                int rectX2 = (int) (uvWindowPos.x() + (float) (u2) * uvWindowScaleFactor);
                int rectY2 = (int) (uvWindowPos.y() + (float) (v2 - 1) * uvWindowScaleFactor);

                if(click.x() >= rectX1 && click.x() <= rectX2 && click.y() >= rectY1 && click.y() <= rectY2) {
                    if(click.button() == 0) {

                        if(selectedFace.ordinal() != faceIndex) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            selectedFace = UVFaces.values()[faceIndex]; // store which rect was clicked
                            clearAndInit();
                        }
                    } else if(click.button() == 1 && selectedFace != UVFaces.NONE) {
                        selectedFace = UVFaces.NONE;
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        clearAndInit();
                    }
                    return true;
                }
            }
            faceIndex++;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if(input.hasShift() && input.hasCtrl()) {
            positionIncrementValue = 20;
        } else if(input.hasShift()) {
            positionIncrementValue = 10;
        }

        return super.keyPressed(input);
    }
    @Override
    public boolean keyReleased(KeyInput input) {
        if(!input.hasShift()) {
            positionIncrementValue = 1;
        } else {
            positionIncrementValue = 10;
        }
        return super.keyPressed(input);
    }

}

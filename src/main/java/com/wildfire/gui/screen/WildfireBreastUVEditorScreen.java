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
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WildfireBreastUVEditorScreen extends BaseWildfireScreen {

    private static final Text TITLE = Text.translatable("wildfire_gender.uv_editor");

    private int positionIncrementValue = 1;

    private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/breast_uv_editor.png");

    public WildfireBreastUVEditorScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.uv_editor"), parent, uuid);
    }

    private ClickableWidget[] positionWidgets = new ClickableWidget[0];

    private int selectedBreastIndex = 0;
    private int selectedFaceIndex = -1; // -1 means none selected

    private Perspective prevPerspective;
    @Override
    public void init() {
        if(prevPerspective == null) {
            prevPerspective = client.options.getPerspective();
        }
        client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);

        final var ref = new Object() {
            ClickableWidget leftBreast, rightBreast, leftBreastOverlay, rightBreastOverlay;
        };

        int x = this.width - 260;
        int w = this.width - (this.width - 260);
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

        //Breast Buttons
        int elementXPos = this.width - 253;
        int elementYPos = 32;


        ref.leftBreast = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.left_breast"))
                .position(elementXPos, elementYPos + 13)
                .size((w / 2) / 2 - 5, 15)
                .active(selectedBreastIndex != 1)
                .onPress(button -> {
                    selectedBreastIndex = 1;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 1;
                    ref.rightBreast.active = selectedBreastIndex != 0;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 3;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 2;
                    clearAndInit();
                }));

        ref.rightBreast = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast"))
                .position(elementXPos + (w / 2) / 2 - 3, elementYPos + 13)
                .size((w / 2) / 2 - 6, 15)
                .active(selectedBreastIndex != 0)
                .onPress(button -> {
                    selectedBreastIndex = 0;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 1;
                    ref.rightBreast.active = selectedBreastIndex != 0;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 3;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 2;
                    clearAndInit();
                }));

        ref.leftBreastOverlay = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.left_breast_overlay"))
                .position(elementXPos, elementYPos + 44)
                .size((w / 2) / 2 - 5, 15)
                .active(selectedBreastIndex != 3)
                .onPress(button -> {
                    selectedBreastIndex = 3;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 1;
                    ref.rightBreast.active = selectedBreastIndex != 0;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 3;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 2;
                    clearAndInit();
                }));

        ref.rightBreastOverlay = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast_overlay"))
                .position(elementXPos + (w / 2) / 2 - 3, elementYPos + 44)
                .size((w / 2) / 2 - 5, 15)
                .active(selectedBreastIndex != 2)
                .onPress(button -> {
                    selectedBreastIndex = 2;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 1;
                    ref.rightBreast.active = selectedBreastIndex != 0;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 3;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 2;
                    clearAndInit();
                }));

        //Position stuff
        if(selectedFaceIndex != -1) {
            int positionBoxX = this.width - 130 + 5;
            int positionBoxW = this.width - (this.width - 260);

            positionWidgets = new ClickableWidget[8];

            int buttonArrayY = 52;
            positionWidgets[0] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.remove_1"))
                    .position(positionBoxX + 58, y + buttonArrayY)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][0]-=positionIncrementValue;
                        selectedUVs[selectedFaceIndex][2]-=positionIncrementValue;
                    }));
            positionWidgets[1] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.add_1"))
                    .position(positionBoxX + 88, y + buttonArrayY)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][0]+=positionIncrementValue;
                        selectedUVs[selectedFaceIndex][2]+=positionIncrementValue;
                    }));

            positionWidgets[2] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.remove_1"))
                    .position(positionBoxX + 58, y + buttonArrayY + 20)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][1]-=positionIncrementValue;
                        selectedUVs[selectedFaceIndex][3]-=positionIncrementValue;
                    }));

            positionWidgets[3] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.add_1"))
                    .position(positionBoxX + 88, y + buttonArrayY + 20)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][1]+=positionIncrementValue;
                        selectedUVs[selectedFaceIndex][3]+=positionIncrementValue;
                    }));

            positionWidgets[4] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.remove_1"))
                    .position(positionBoxX + 58, y + buttonArrayY + 60)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][2]-=positionIncrementValue;
                    }));
            positionWidgets[5] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.add_1"))
                    .position(positionBoxX + 88, y + buttonArrayY + 60)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][2]+=positionIncrementValue;
                    }));

            positionWidgets[6] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.remove_1"))
                    .position(positionBoxX + 58, y + buttonArrayY + 80)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][3]-=positionIncrementValue;
                    }));

            positionWidgets[7] = addButton(builder -> builder
                    .message(() -> Text.translatable("wildfire_gender.uv_editor.add_1"))
                    .position(positionBoxX + 88, y + buttonArrayY + 80)
                    .size(30, 20)
                    .onPress(button -> {
                        selectedUVs[selectedFaceIndex][3]+=positionIncrementValue;
                    }));



        }
        super.init();
    }

    private void updatePositionWidgets() {
        String addKey = "wildfire_gender.uv_editor.add_1";
        String removeKey = "wildfire_gender.uv_editor.remove_1";

        if (positionIncrementValue == 10) {
            addKey = "wildfire_gender.uv_editor.add_10";
            removeKey = "wildfire_gender.uv_editor.remove_10";
        } else if (positionIncrementValue == 20) {
            addKey = "wildfire_gender.uv_editor.add_20";
            removeKey = "wildfire_gender.uv_editor.remove_20";
        }

        Text addText = Text.translatable(addKey);
        Text removeText = Text.translatable(removeKey);

        for (int i = 0; i < positionWidgets.length; i++) {
            ClickableWidget widget = positionWidgets[i];
            widget.setMessage(i % 2 == 0 ? removeText : addText);
        }

    }


    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        //this.renderInGameBackground(ctx);
        //ctx.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 190) / 2, (this.height - 107) / 2, 0, 0, 190, 107, 512, 512);
        int w = this.width - (this.width - 260) - 10;

        ctx.fill(this.width - 260, 0, this.width, this.height, 0xCC000000);
        ctx.fill(this.width - 255, 30, this.width - w / 2 - 5, 200, 0x66000000);
        ctx.fill(this.width - w / 2, 30, this.width - 5, 200, 0x66000000);
    }

    private int[][] selectedUVs;

    @Override
    public void tick() {
        selectedUVs = switch (selectedBreastIndex) {
            case 0 -> getPlayer().getLeftBreastUVLayout();
            case 1 -> getPlayer().getRightBreastUVLayout();
            case 2 -> getPlayer().getLeftBreastOverlayUVLayout();
            case 3 -> getPlayer().getRightBreastOverlayUVLayout();
            default -> getPlayer().getLeftBreastUVLayout();
        };
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (client == null || client.world == null) return;

        //TODO: Localize this
        final String[] FACE_NAMES_LEFT = new String[] {
                "Inner Breast",
                "Outer Breast",
                "Top Breast",
                "Under Breast",
                "Front Breast"
        };
        final String[] FACE_NAMES_RIGHT = new String[] {
                "Outer Breast",
                "Inner Breast",
                "Top Breast",
                "Under Breast",
                "Front Breast"
        };

        int x = this.width / 2;
        int y = this.height / 2;

        final int textureDrawWidth = 196;
        final int textureSourceWidth = 64;
        final float scaleFactor = (float) textureDrawWidth / (float) textureSourceWidth;

        int screenXBase = this.width - 230;
        int screenYBase = this.height - 230;

        if(client.player != null && selectedUVs != null) {

            ctx.drawText(textRenderer, Text.literal("Elements"), 0, 0, 0xFFFFFFFF, false);

            ctx.drawTexture(RenderPipelines.GUI_TEXTURED, client.player.getSkin().body().id(),
                    screenXBase, screenYBase,
                    0, 0, textureDrawWidth, textureDrawWidth, textureDrawWidth, textureDrawWidth);


            final int borderThickness = 1;

            final int[] FACE_COLORS = new int[]{
                    0xFFFF0000,
                    0xFF00FF00,
                    0xFF0000FF,
                    0xFF00FFFF,
                    0xFFFF00FF
            };
            final int[] FADED_FACE_COLORS = new int[]{
                    0x33FF0000,
                    0x3300FF00,
                    0x330000FF,
                    0x3300FFFF,
                    0x33FF00FF
            };

            final String[] SHORT_FACE_NAMES = new String[]{
                    "E",
                    "W",
                    "D",
                    "U",
                    "N"
            };

            int faceIndex = 0;

            //Other faces
            int[][][] ALL_UVS = new int[][][] {
                    getPlayer().getLeftBreastUVLayout(),
                    getPlayer().getRightBreastUVLayout(),
                    getPlayer().getLeftBreastOverlayUVLayout(),
                    getPlayer().getRightBreastOverlayUVLayout()
            };

            int allFaceIndex = 0;
            for(int[][] eachBreast : ALL_UVS) {
                allFaceIndex = 0;
                for (int[] faceUV : eachBreast) {

                    int borderColor = FADED_FACE_COLORS[allFaceIndex];

                    final String faceName = SHORT_FACE_NAMES[allFaceIndex];
                    String fullFaceName = "N/A";
                    if (selectedBreastIndex % 2 == 0) {
                        fullFaceName = FACE_NAMES_LEFT[allFaceIndex];
                    } else {
                        fullFaceName = FACE_NAMES_RIGHT[allFaceIndex];
                    }
                    int u1 = faceUV[0];
                    int v1 = faceUV[1];
                    int u2 = faceUV[2];
                    int v2 = faceUV[3];

                    if (!(u1 == 0 && v1 == 0 && u2 == 0 && v2 == 0)) {
                        int rectX1 = (int) (screenXBase + (float) (u1) * scaleFactor);
                        int rectY1 = (int) (screenYBase + (float) (v1 - 1) * scaleFactor);
                        int rectX2 = (int) (screenXBase + (float) (u2) * scaleFactor);
                        int rectY2 = (int) (screenYBase + (float) (v2 - 1) * scaleFactor);

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

                        ctx.drawText(textRenderer, faceName, -textWidth / 2, -textHeight / 2, 0x33FFFFFF, true);

                        ctx.getMatrices().popMatrix();

                    }
                    allFaceIndex++;
                }
            }

            //selected faces
            for (int[] faceUV : selectedUVs) {

                int borderColor = (faceIndex == selectedFaceIndex) ? 0xFFFFFFFF : FACE_COLORS[faceIndex];

                final String faceName = SHORT_FACE_NAMES[faceIndex];
                String fullFaceName = "N/A";
                if(selectedBreastIndex % 2 == 0) {
                    fullFaceName = FACE_NAMES_LEFT[faceIndex];
                } else {
                    fullFaceName = FACE_NAMES_RIGHT[faceIndex];
                }
                int u1 = faceUV[0];
                int v1 = faceUV[1];
                int u2 = faceUV[2];
                int v2 = faceUV[3];

                if(!(u1 == 0 && v1 == 0 && u2 == 0 && v2 == 0)) {
                    int rectX1 = (int) (screenXBase + (float) (u1) * scaleFactor);
                    int rectY1 = (int) (screenYBase + (float) (v1 - 1) * scaleFactor);
                    int rectX2 = (int) (screenXBase + (float) (u2) * scaleFactor);
                    int rectY2 = (int) (screenYBase + (float) (v2 - 1) * scaleFactor);

                    if(mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
                        List<OrderedText> array = new ArrayList<>();
                        array.add(Text.literal(fullFaceName).append(" (").append(faceName).append(")").formatted(Formatting.GOLD).asOrderedText());
                        array.add(Text.empty().append("[" + u1 + ", " + v1 + ", " + u2 + ", " + v2 + "]").formatted(Formatting.AQUA).asOrderedText());
                        ctx.drawTooltip(array, mouseX, mouseY);
                    }

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

        int elementXPos = this.width - 253;
        int elementYPos = 32;

        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.selection.layer_body"),  elementXPos + 62, elementYPos + 2, 0xFFFFFFFF);
        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.selection.layer_jacket"),  elementXPos + 62, elementYPos + 32, 0xFFFFFFFF);


        int positionBoxX = this.width - 260 / 4;
        int positionBoxW = this.width - (this.width - 260);

        //Coordinate selector
        if(selectedFaceIndex == -1) {
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.no_face_selected"), positionBoxX, 105, 120, 0xFFFFFFFF);
        } else {
            String fullFaceName = "N/A";
            if(selectedBreastIndex % 2 == 0) {
                fullFaceName = FACE_NAMES_LEFT[selectedFaceIndex];
            } else {
                fullFaceName = FACE_NAMES_RIGHT[selectedFaceIndex];
            }

            GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable(fullFaceName).formatted(Formatting.GOLD), positionBoxX, 37, 0xFFFFFFFF);

            /*int mouseA = (int) ((mouseX-screenXBase) / scaleFactor);
            int mouseB = (int) ((mouseY-screenYBase) / scaleFactor);

            ctx.drawTooltip(Text.literal("A: " + mouseA + ", B: " + mouseB + " X: " + mouseX + " Y: " + mouseY), mouseX, mouseY);*/
            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.xpos"), positionBoxX - 55, 58, 0xFFFFFFFF, false);
            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.ypos"), positionBoxX - 55, 78, 0xFFFFFFFF, false);
            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.width"), positionBoxX - 55, 118, 0xFFFFFFFF, false);
            ctx.drawText(textRenderer, Text.translatable("wildfire_gender.uv_editor.height"), positionBoxX - 55, 138, 0xFFFFFFFF, false);

            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(positionBoxX, 184);
            ctx.getMatrices().scale(0.75f);
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.increment_tip.line1"), 0, -6, 160, 0xFF888888);
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.increment_tip.line2"), 0, 6, 160, 0xFF888888);
            ctx.getMatrices().popMatrix();
        }


        GuiUtils.drawCenteredText(ctx, textRenderer, TITLE, this.width / 2, 20, 0xFFFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {

        //TODO: Make these common to the class instead of duplicating?
        int screenXBase = this.width - 230;
        int screenYBase = this.height - 230;
        final int textureDrawWidth = 196;
        final int textureSourceWidth = 64;
        final float scaleFactor = (float) textureDrawWidth / (float) textureSourceWidth;

        int faceIndex = 0;

        for (int[] faceUV : selectedUVs) {
            int u1 = faceUV[0];
            int v1 = faceUV[1];
            int u2 = faceUV[2];
            int v2 = faceUV[3];

            if(!(u1 == 0 && v1 == 0 && u2 == 0 && v2 == 0)) {
                int rectX1 = (int) (screenXBase + (float) (u1) * scaleFactor);
                int rectY1 = (int) (screenYBase + (float) (v1 - 1) * scaleFactor);
                int rectX2 = (int) (screenXBase + (float) (u2) * scaleFactor);
                int rectY2 = (int) (screenYBase + (float) (v2 - 1) * scaleFactor);

                if(click.x() >= rectX1 && click.x() <= rectX2 && click.y() >= rectY1 && click.y() <= rectY2) {
                    if(click.button() == 0) {

                        if(selectedFaceIndex != faceIndex) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            selectedFaceIndex = faceIndex; // store which rect was clicked
                            clearAndInit();
                        }
                    } else if(click.button() == 1 && selectedFaceIndex != -1) {
                        selectedFaceIndex = -1;
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
            updatePositionWidgets();
        } else if(input.hasShift()) {
            positionIncrementValue = 10;
            updatePositionWidgets();
        }

        return super.keyPressed(input);
    }
    @Override
    public boolean keyReleased(KeyInput input) {
        positionIncrementValue = 1;
        updatePositionWidgets();
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        client.options.setPerspective(prevPerspective);
        prevPerspective = null;
        super.close();
    }
}

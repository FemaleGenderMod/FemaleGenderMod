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
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class WildfireBreastUVEditorScreen extends BaseWildfireScreen {

    private static final Text TITLE = Text.translatable("wildfire_gender.uv_editor");

    private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/breast_uv_editor.png");

    public WildfireBreastUVEditorScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.uv_editor"), parent, uuid);
    }

    private int selectedBreastIndex = 0;
    private int selectedFaceIndex = -1; // -1 means none selected

    @Override
    public void init() {

        final var ref = new Object() {
            ClickableWidget leftBreast, rightBreast, leftBreastOverlay, rightBreastOverlay;
        };

        int x = this.width - 260;
        int w = this.width - (this.width - 260);
        int y = 0;

        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.reset_defaults"))
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
                .position(elementXPos, elementYPos)
                .size(w / 2 - 9, 20)
                .active(selectedBreastIndex != 0)
                .onPress(button -> {
                    selectedBreastIndex = 0;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 0;
                    ref.rightBreast.active = selectedBreastIndex != 1;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 2;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 3;
                    clearAndInit();
                }));

        ref.rightBreast = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast"))
                .position(elementXPos, elementYPos + 22)
                .size(w / 2 - 9, 20)
                .active(selectedBreastIndex != 1)
                .onPress(button -> {
                    selectedBreastIndex = 1;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 0;
                    ref.rightBreast.active = selectedBreastIndex != 1;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 2;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 3;
                    clearAndInit();
                }));

        ref.leftBreastOverlay = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.left_breast_overlay"))
                .position(elementXPos, elementYPos + 44)
                .size(w / 2 - 9, 20)
                .active(selectedBreastIndex != 2)
                .onPress(button -> {
                    selectedBreastIndex = 2;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 0;
                    ref.rightBreast.active = selectedBreastIndex != 1;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 2;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 3;
                    clearAndInit();
                }));

        ref.rightBreastOverlay = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast_overlay"))
                .position(elementXPos, elementYPos + 66)
                .size(w / 2 - 9, 20)
                .active(selectedBreastIndex != 3)
                .onPress(button -> {
                    selectedBreastIndex = 3;
                    selectedFaceIndex = -1;
                    ref.leftBreast.active = selectedBreastIndex != 0;
                    ref.rightBreast.active = selectedBreastIndex != 1;
                    ref.leftBreastOverlay.active = selectedBreastIndex != 2;
                    ref.rightBreastOverlay.active = selectedBreastIndex != 3;
                    clearAndInit();
                }));

        super.init();
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

            final String[] FACE_NAMES = new String[]{
                    "E",
                    "W",
                    "D",
                    "U",
                    "N"
            };

            int faceIndex = 0;

            for (int[] faceUV : selectedUVs) {

                int borderColor = (faceIndex == selectedFaceIndex) ? 0xFFFFFFFF : FACE_COLORS[faceIndex];

                final String faceName = FACE_NAMES[faceIndex];

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
                        ctx.drawTooltip(Text.literal(faceName), mouseX, mouseY);
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

                    faceIndex++;
                }
            }

        }

        int positionBoxX = this.width - 260 / 4;
        int positionBoxW = this.width - (this.width - 260);

        //Coordinate selector
        if(selectedFaceIndex == -1) {
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.no_face_selected"), positionBoxX, 105, 120, 0xFFFFFFFF);
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

            if (!(u1 == 0 && v1 == 0 && u2 == 0 && v2 == 0)) {
                int rectX1 = (int) (screenXBase + (float) (u1) * scaleFactor);
                int rectY1 = (int) (screenYBase + (float) (v1 - 1) * scaleFactor);
                int rectX2 = (int) (screenXBase + (float) (u2) * scaleFactor);
                int rectY2 = (int) (screenYBase + (float) (v2 - 1) * scaleFactor);

                if(click.button() == 0 &&
                        click.x() >= rectX1 && click.x() <= rectX2 && click.y() >= rectY1 && click.y() <= rectY2) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    selectedFaceIndex = faceIndex; // store which rect was clicked
                    clearAndInit();
                    return true;
                }
                faceIndex++;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public void close() {
        super.close();
    }
}

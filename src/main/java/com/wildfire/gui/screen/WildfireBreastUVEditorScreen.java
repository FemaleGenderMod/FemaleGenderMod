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
import com.wildfire.main.uvs.BreastTypes;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2i;

import java.util.*;

public class WildfireBreastUVEditorScreen extends BaseWildfireScreen {

    private static final Text TITLE = Text.translatable("wildfire_gender.uv_editor");

    private static final Identifier TEXTURE_ADD = Identifier.of(WildfireGender.MODID, "textures/gui/widgets/add.png");
    private static final Identifier TEXTURE_SUBTRACT = Identifier.of(WildfireGender.MODID, "textures/gui/widgets/subtract.png");

    private UVLayout selectedUVs = null;
    private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
    private UVDirection selectedDirection = null;

    //Positions & Widths
    private Vector2i winElementPos;
    private Vector2i uvWindowPos;

    private static final int sidebarWidth = 180;
    private static final int textureDrawWidth = 196;
    private static final int textureSourceWidth = 64;
    private static final float uvWindowScaleFactor = (float) textureDrawWidth / (float) textureSourceWidth;

    public WildfireBreastUVEditorScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.uv_editor"), parent, uuid);
    }

    @Override
    public void init() {
        if(client == null) return;

        uvWindowPos = new Vector2i(5, this.height / 2 - textureDrawWidth / 2);
        winElementPos = new Vector2i(this.width - sidebarWidth + 7, 32);

        int x = this.width - sidebarWidth;
        int w = this.width - (this.width - sidebarWidth);
        int y = 0;

        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.reset_defaults_all"))
                .position(x + 5, y + 5)
                .size(this.width - x - 10, 20)
                .onPress(button -> {
                    var player = Objects.requireNonNull(getPlayer(), "getPlayer()");

                    player.updateLeftBreastUVLayout(Configuration.LEFT_BREAST_UV_LAYOUT.getDefault());
                    player.updateRightBreastUVLayout(Configuration.RIGHT_BREAST_UV_LAYOUT.getDefault());

                    player.updateLeftBreastOverlayUVLayout(Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT.getDefault());
                    player.updateRightBreastOverlayUVLayout(Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT.getDefault());

                    player.save();
                }));

        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.left_breast"))
                .position(winElementPos.x(), winElementPos.y() + 13)
                .size((w / 2) / 2 - 5, 15)
                .active(selectedBreastIndex != BreastTypes.LEFT)
                .onPress(button -> selectBreastUVMap(BreastTypes.LEFT)));

        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast"))
                .position(winElementPos.x() + (w / 2) / 2 - 3, winElementPos.y() + 13)
                .size((w / 2) / 2 - 6, 15)
                .active(selectedBreastIndex != BreastTypes.RIGHT)
                .onPress(button -> selectBreastUVMap(BreastTypes.RIGHT)));

        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.left_breast_overlay"))
                .position(winElementPos.x(), winElementPos.y() + 44)
                .size((w / 2) / 2 - 5, 15)
                .active(selectedBreastIndex != BreastTypes.LEFT_OVERLAY)
                .onPress(button -> selectBreastUVMap(BreastTypes.LEFT_OVERLAY)));

        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.uv_editor.selection.right_breast_overlay"))
                .position(winElementPos.x() + (w / 2) / 2 - 3, winElementPos.y() + 44)
                .size((w / 2) / 2 - 6, 15)
                .active(selectedBreastIndex != BreastTypes.RIGHT_OVERLAY)
                .onPress(button -> selectBreastUVMap(BreastTypes.RIGHT_OVERLAY)));

        //Position stuff
        if(selectedDirection != null) {
            int uvPositionWindowX = this.width - 130 + 5;

            int buttonArrayY = 52;

            for (int i = 0; i < 8; i++) {
                boolean isAdd = (i % 2 == 1);
                int uvIndex = i / 2;
                int delta = isAdd ? 1 : -1;

                int xOffset = isAdd ? 106 : 92;
                int yOffset = (i / 2) * 14;

                addButton(builder -> builder
                        .renderer((button, ctx, mouseX, mouseY, partialTicks) -> {
                            int increment = getPositionIncrement();
                            Formatting colorVal = increment == 10 ? Formatting.AQUA :
                                    (increment == 20 ? Formatting.BLUE : Formatting.WHITE);
                            ctx.drawTexture(RenderPipelines.GUI_TEXTURED,
                                    isAdd ? TEXTURE_ADD : TEXTURE_SUBTRACT,
                                    button.getX() + button.getWidth() / 2 - 3,
                                    button.getY() + button.getHeight() / 2 - 3,
                                    0,0,6,6,6,6,6,6,
                                    ColorHelper.fullAlpha(Objects.requireNonNull(colorVal.getColorValue())));
                        })
                        .message(() -> isAdd ? Text.translatable("wildfire_gender.uv_editor.add") : Text.translatable("wildfire_gender.uv_editor.remove"))
                        .position(uvPositionWindowX + xOffset, y + buttonArrayY + yOffset)
                        .size(12, 12)
                        .onPress(button -> {
                            if(selectedDirection == null) return;
                            final var player = Objects.requireNonNull(getPlayer(), "getPlayer()");

                            UVQuad quad = selectedUVs.getAllSides().get(selectedDirection);
                            int increment = getPositionIncrement();
                            int toAdd = delta * increment;

                            if(uvIndex == 0) {
                                quad = quad.addX1(toAdd).addX2(toAdd);
                            } else if(uvIndex == 1) {
                                quad = quad.addY1(toAdd).addY2(toAdd);
                            } else if(uvIndex == 2) {
                                quad = quad.addX2(toAdd);
                            } else {
                                quad = quad.addY2(toAdd);
                            }

                            selectedUVs.put(selectedDirection, quad);
                            player.save();
                        })
                );
            }
        }
    }

    private void selectBreastUVMap(BreastTypes breast) {
        selectedBreastIndex = breast;
        selectedDirection = null;
        clearAndInit();
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
        var player = getPlayer();
        if(player == null) return;

        selectedUVs = switch (selectedBreastIndex) {
            case BreastTypes.RIGHT -> player.getRightBreastUVLayout();
            case BreastTypes.LEFT_OVERLAY -> player.getLeftBreastOverlayUVLayout();
            case BreastTypes.RIGHT_OVERLAY -> player.getRightBreastOverlayUVLayout();
            default -> player.getLeftBreastUVLayout();
        };
    }

    // TODO this should be broken up into smaller methods
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if(client == null || client.world == null || client.player == null) return;
        var player = getPlayer();

        if(player != null && selectedUVs != null) {

            //noinspection SuspiciousNameCombination
            ctx.drawTexture(RenderPipelines.GUI_TEXTURED, client.player.getSkin().body().id(),
                    uvWindowPos.x(), uvWindowPos.y(),
                    0, 0, textureDrawWidth, textureDrawWidth, textureDrawWidth, textureDrawWidth);

            //Other faces
            UVLayout[] ALL_UVS = new UVLayout[] {
                    player.getLeftBreastUVLayout(),
                    player.getRightBreastUVLayout(),
                    player.getLeftBreastOverlayUVLayout(),
                    player.getRightBreastOverlayUVLayout()
            };

            for(UVLayout eachBreast : ALL_UVS) {
                drawFaceBorders(ctx, eachBreast, mouseX, mouseY, true);
            }

            drawFaceBorders(ctx, selectedUVs, mouseX, mouseY, false);
        }

        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.selection.layer_body"),  winElementPos.x() + 42, winElementPos.y() + 2, 0xFFFFFFFF);
        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.selection.layer_jacket"),  winElementPos.x() + 42, winElementPos.y() + 32, 0xFFFFFFFF);

        int positionBoxX = this.width - sidebarWidth / 4;

        //Coordinate selector
        if(selectedDirection == null) {
            GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.uv_editor.no_face_selected"), positionBoxX, 60, 70, 0xFF888888);
        } else {

            GuiUtils.drawCenteredText(ctx, textRenderer, Text.empty().append(selectedDirection.getDirectionText(selectedBreastIndex)).formatted(Formatting.GOLD), positionBoxX, 37, 0xFFFFFFFF);

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

    private void drawFaceBorders(DrawContext ctx, UVLayout uvList, int mouseX, int mouseY, boolean faded) {

        //selected faces

        for (Map.Entry<UVDirection, UVQuad> entry : uvList.getAllSides().entrySet()) {
            UVDirection direction = entry.getKey();
            UVQuad quad = entry.getValue();


            int borderColor = (selectedDirection == direction && !faded) ? 0xFFFFFFFF : direction.getFaceColor(faded);

            final String faceName = direction.getShortName();

            if(!(quad.x1() == 0 && quad.y1() == 0 && quad.x2() == 0 && quad.y2() == 0)) {
                int rectX1 = (int) (uvWindowPos.x() + (float) (quad.x1()) * uvWindowScaleFactor);
                int rectY1 = (int) (uvWindowPos.y() + (float) (quad.y1() - 1) * uvWindowScaleFactor);
                int rectX2 = (int) (uvWindowPos.x() + (float) (quad.x2()) * uvWindowScaleFactor);
                int rectY2 = (int) (uvWindowPos.y() + (float) (quad.y2() - 1) * uvWindowScaleFactor);

                if(mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
                    List<OrderedText> array = new ArrayList<>();
                    array.add(Text.empty().append(direction.getDirectionText(selectedBreastIndex)).append(" (").append(faceName).append(")").formatted(Formatting.GOLD).asOrderedText());
                    array.add(Text.empty().append("[" + quad.x1() + ", " + quad.y1() + ", " + quad.x2() + ", " + quad.y2() + "]").formatted(Formatting.AQUA).asOrderedText());
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
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if(selectedUVs == null) return super.mouseClicked(click, doubled);

        for (Map.Entry<UVDirection, UVQuad> entry : selectedUVs.getAllSides().entrySet()) {
            UVDirection direction = entry.getKey();
            UVQuad quad = entry.getValue();

            if(!(quad.x1() == 0 && quad.y1() == 0 && quad.x2() == 0 && quad.y2() == 0)) {
                int rectX1 = (int) (uvWindowPos.x() + (float) (quad.x1()) * uvWindowScaleFactor);
                int rectY1 = (int) (uvWindowPos.y() + (float) (quad.y1() - 1) * uvWindowScaleFactor);
                int rectX2 = (int) (uvWindowPos.x() + (float) (quad.x2()) * uvWindowScaleFactor);
                int rectY2 = (int) (uvWindowPos.y() + (float) (quad.y2() - 1) * uvWindowScaleFactor);

                if(click.x() >= rectX1 && click.x() <= rectX2 && click.y() >= rectY1 && click.y() <= rectY2) {
                    if(click.button() == 0) {

                        if(selectedDirection != direction) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            selectedDirection = direction; // store which rect was clicked
                            clearAndInit();
                        }
                    } else if(click.button() == 1 && selectedDirection != null) {
                        selectedDirection = null;
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        clearAndInit();
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    private int getPositionIncrement() {
        // this should only ever be null before #init() is called, and never afterward
        Objects.requireNonNull(client);
        if (client.isShiftPressed() && client.isCtrlPressed()) return 20;
        if (client.isShiftPressed()) return 10;
        return 1;
    }

}

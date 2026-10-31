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

package com.wildfire.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.wildfire.api.uvs.BreastTypes;
import com.wildfire.api.uvs.UVDirection;
import com.wildfire.api.uvs.UVLayout;
import com.wildfire.api.uvs.UVQuad;
import com.wildfire.client.gui.IFancyFontRenderer.TextAlignment;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.entities.avatars.AbstractAvatarConfigHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

public class WildfireBreastUVEditorScreen extends BaseWildfireScreen {

    private static final ResourceLocation TEXTURE_ADD = WildfireGender.id("widgets/add");
    private static final ResourceLocation TEXTURE_SUBTRACT = WildfireGender.id("widgets/subtract");

    private @Nullable UVLayout selectedUVs = null;
    private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
    private @Nullable UVDirection selectedDirection = null;

    //Positions & Widths
    @UnknownNullability("null until #init() is run")
    private Vector2i winElementPos, uvWindowPos;

    private static final int sidebarWidth = 180;
    private static final int textureDrawWidth = 196;
    private static final int textureSourceWidth = 64;
    private static final float uvWindowScaleFactor = textureDrawWidth / (float) textureSourceWidth;

    public WildfireBreastUVEditorScreen(Screen parent, AbstractAvatarConfigHolder config) {
        super(WildfireLang.UV_EDITOR.translate(), parent, config);
    }

    @Override
    public void init() {
        super.init();
        uvWindowPos = new Vector2i(5, this.height / 2 - textureDrawWidth / 2);
        winElementPos = new Vector2i(this.width - sidebarWidth + 7, 32);

        int x = this.width - sidebarWidth;
        int y = 0;

        addButton(builder -> builder
                .message(WildfireLang.UV_EDITOR_RESET_ALL::translate)
                .position(x + 5, y + 5)
                .size(this.width - x - 10, 20)
                .onPress(ignored -> {
                    if (config.uvs().reset()) {
                        save();
                    }
                }));

        addButton(builder -> builder
                .message(WildfireLang.UV_EDITOR_LB::translate)
                .position(winElementPos.x(), winElementPos.y() + 13)
                .size(sidebarWidth / 4 - 5, 15)
                .active(selectedBreastIndex != BreastTypes.LEFT)
                .onPress(ignored -> selectBreastUVMap(BreastTypes.LEFT)));

        addButton(builder -> builder
                .message(WildfireLang.UV_EDITOR_RB::translate)
                .position(winElementPos.x() + sidebarWidth / 4 - 3, winElementPos.y() + 13)
                .size(sidebarWidth / 4 - 6, 15)
                .active(selectedBreastIndex != BreastTypes.RIGHT)
                .onPress(ignored -> selectBreastUVMap(BreastTypes.RIGHT)));

        addButton(builder -> builder
                .message(WildfireLang.UV_EDITOR_LB_OVERLAY::translate)
                .position(winElementPos.x(), winElementPos.y() + 44)
                .size(sidebarWidth / 4 - 5, 15)
                .active(selectedBreastIndex != BreastTypes.LEFT_OVERLAY)
                .onPress(ignored -> selectBreastUVMap(BreastTypes.LEFT_OVERLAY)));

        addButton(builder -> builder
                .message(WildfireLang.UV_EDITOR_RB_OVERLAY::translate)
                .position(winElementPos.x() + sidebarWidth / 4 - 3, winElementPos.y() + 44)
                .size(sidebarWidth / 4 - 6, 15)
                .active(selectedBreastIndex != BreastTypes.RIGHT_OVERLAY)
                .onPress(ignored -> selectBreastUVMap(BreastTypes.RIGHT_OVERLAY)));

        //Position stuff
        if(selectedDirection != null) {
            int uvPositionWindowX = this.width - 130 + 5;

            int buttonArrayY = 52;

            for (int i = 0; i < 8; i++) {
                boolean isAdd = i % 2 == 1;
                int uvIndex = i / 2;
                int delta = isAdd ? 1 : -1;

                int xOffset = isAdd ? 106 : 92;
                int yOffset = (i / 2) * 14;

                addButton(builder -> builder
                        .renderer((button, ctx, ignoredMouseX, ignoredMouseY, ignoredPartialTicks) -> {
                            int increment = getPositionIncrement();
                            int color = switch (increment) {
                                case 10 -> ChatFormatting.AQUA.getColor();
                                case 20 -> ChatFormatting.BLUE.getColor();
                                default -> ChatFormatting.WHITE.getColor();
                            };
                            //Note: 1.21.1's blitSprite has no color overload, so the tint is applied via #setColor instead
                            ctx.setColor(FastColor.ARGB32.red(color) / 255.0F, FastColor.ARGB32.green(color) / 255.0F, FastColor.ARGB32.blue(color) / 255.0F, 1.0F);
                            ctx.blitSprite(isAdd ? TEXTURE_ADD : TEXTURE_SUBTRACT,
                                    button.getX() + button.getWidth() / 2 - 3,
                                    button.getY() + button.getHeight() / 2 - 3,
                                6, 6);
                            ctx.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                        })
                        .message((isAdd ? WildfireLang.UV_EDITOR_ADD : WildfireLang.UV_EDITOR_REMOVE)::translate)
                        .position(uvPositionWindowX + xOffset, y + buttonArrayY + yOffset)
                        .size(12, 12)
                        .onPress(ignored -> {
                            if(selectedDirection == null || selectedUVs == null) return;

                            UVQuad quad = selectedUVs.getAllSides().get(selectedDirection);
                            assert quad != null; // TODO can this assumption ever be broken without the user meddling with the config?
                            int increment = getPositionIncrement();
                            int toAdd = delta * increment;

                            quad = switch (uvIndex) {
                                case 0 -> quad.addX1(toAdd).addX2(toAdd);
                                case 1 -> quad.addY1(toAdd).addY2(toAdd);
                                case 2 -> quad.addX2(toAdd);
                                default -> quad.addY2(toAdd);
                            };

                            selectedUVs.put(selectedDirection, quad);
                            save();
                        })
                );
            }
        }
    }

    private void selectBreastUVMap(BreastTypes breast) {
        selectedBreastIndex = breast;
        selectedDirection = null;
        rebuildWidgets();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        int w = this.width - (this.width - sidebarWidth) - 10;

        graphics.fill(this.width - sidebarWidth, 0, this.width, this.height, 0xCC000000);
        graphics.fill(this.width - sidebarWidth + 5, 30, this.width - w / 2 - 5, 93, 0x66000000);
        graphics.fill(this.width - w / 2, 30, this.width - 5, 128, 0x66000000);

        graphics.fill(uvWindowPos.x() - 2, uvWindowPos.y() - 2, uvWindowPos.x() + textureDrawWidth + 2, uvWindowPos.y() + textureDrawWidth + 2, 0xCC000000);
        graphics.fill(uvWindowPos.x(), uvWindowPos.y(), uvWindowPos.x() + textureDrawWidth, uvWindowPos.y() + textureDrawWidth, 0xFFFFFF);
    }


    @Override
    public void tick() {
        super.tick();

        selectedUVs = switch (selectedBreastIndex) {
            case RIGHT -> config.uvs().skin().right().get();
            case LEFT_OVERLAY -> config.uvs().overlay().left().get();
            case RIGHT_OVERLAY -> config.uvs().overlay().right().get();
            default -> config.uvs().skin().left().get();
        };
    }

    // TODO this should be broken up into smaller methods
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        LivingEntity entity = getEntity();
        if(!(entity instanceof AbstractClientPlayer avatar)) {
            return;
        }

        if(selectedUVs != null) {
            //noinspection SuspiciousNameCombination
            graphics.blit(avatar.getSkin().texture(),
                    uvWindowPos.x(), uvWindowPos.y(),
                    0, 0, textureDrawWidth, textureDrawWidth, textureDrawWidth, textureDrawWidth);

            //Other faces
            for(UVLayout eachBreast : config.uvs()) {
                drawFaceBorders(graphics, eachBreast, mouseX, mouseY, true);
            }

            drawFaceBorders(graphics, selectedUVs, mouseX, mouseY, false);
        }

        drawScrollingString(graphics, WildfireLang.UV_EDITOR_BODY_LAYER.translate(), winElementPos.x(), winElementPos.y() + 2, TextAlignment.CENTER, 0xFFFFFF,
            sidebarWidth / 2 - 9, 0, false);
        drawScrollingString(graphics, WildfireLang.UV_EDITOR_JACKET_LAYER.translate(), winElementPos.x(), winElementPos.y() + 32, TextAlignment.CENTER, 0xFFFFFF,
            sidebarWidth / 2 - 9, 0, false);

        int positionBoxX = this.width - sidebarWidth / 4;

        //Coordinate selector
        if(selectedDirection == null) {
            drawCenteredTextWrapped(graphics, WildfireLang.UV_EDITOR_NO_FACE.translate(), positionBoxX, 60, 70, 0xFF888888);
        } else {
            drawScrollingString(graphics, selectedDirection.getDirectionText(selectedBreastIndex).withColor(ChatFormatting.GOLD.getColor()), positionBoxX - 40, 37,
                TextAlignment.CENTER, 0xFFFFFF, 80, 2, false);

            drawScrollingString(graphics, WildfireLang.UV_EDITOR_X_POS.translate(), positionBoxX - 35, 55, TextAlignment.LEFT, 0xFFFFFF, 45, 0, false);
            drawScrollingString(graphics, WildfireLang.UV_EDITOR_Y_POS.translate(), positionBoxX - 35, 55 + 14, TextAlignment.LEFT, 0xFFFFFF, 45, 0, false);
            drawScrollingString(graphics, WildfireLang.UV_EDITOR_WIDTH.translate(), positionBoxX - 35, 55 + 2 * 14, TextAlignment.LEFT, 0xFFFFFF, 45, 0, false);
            drawScrollingString(graphics, WildfireLang.UV_EDITOR_HEIGHT.translate(), positionBoxX - 35, 55 + 3 * 14, TextAlignment.LEFT, 0xFFFFFF, 45, 0, false);

            drawScaledScrollingString(graphics, WildfireLang.UV_EDITOR_INCREMENT.line(1).withColor(ChatFormatting.AQUA.getColor()), positionBoxX - 40, 109,
                TextAlignment.LEFT, 0xFFFFFF, 80, 2, false, 0.75F);
            drawScaledScrollingString(graphics, WildfireLang.UV_EDITOR_INCREMENT.line(2).withColor(ChatFormatting.BLUE.getColor()), positionBoxX - 40, 119,
                TextAlignment.LEFT, 0xFFFFFF, 80, 2, false, 0.75F);
        }

        int modelScale = 120;
        if(Minecraft.getInstance().getWindow().getScreenWidth() < 1920) {
            modelScale = 60;
        } else if(Minecraft.getInstance().getWindow().getScreenWidth() >= 2560) {
            modelScale = 200;
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, this.width / 2 - modelScale, this.height / 2 - modelScale, this.width / 2 + modelScale,
            this.height / 2 + modelScale, modelScale, getEntityScale(entity, 0, false), mouseX, mouseY, entity);
        drawScrollingString(graphics, getTitle(), uvWindowPos.x(), 20, TextAlignment.CENTER, 0xFFFFFF, textureDrawWidth, 2, false);
    }

    private void drawFaceBorders(GuiGraphics graphics, UVLayout uvList, int mouseX, int mouseY, boolean faded) {

        //selected faces

        for (Map.Entry<UVDirection, UVQuad> entry : uvList.getAllSides().entrySet()) {
            UVDirection direction = entry.getKey();
            UVQuad quad = entry.getValue();


            int borderColor = (selectedDirection == direction && !faded) ? 0xFFFFFF : direction.getFaceColor(faded);

            final Component faceName = direction.getShortName();

            if(!(quad.x1() == 0 && quad.y1() == 0 && quad.x2() == 0 && quad.y2() == 0)) {
                int rectX1 = (int) (uvWindowPos.x() + quad.x1() * uvWindowScaleFactor);
                int rectY1 = (int) (uvWindowPos.y() + (quad.y1() - 1) * uvWindowScaleFactor);
                int rectX2 = (int) (uvWindowPos.x() + quad.x2() * uvWindowScaleFactor);
                int rectY2 = (int) (uvWindowPos.y() + (quad.y2() - 1) * uvWindowScaleFactor);

                if(mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
                    List<FormattedCharSequence> array = new ArrayList<>();
                    array.add(WildfireLang.UV_SELECTED_DIRECTION.translateColored(ChatFormatting.GOLD, direction.getDirectionText(selectedBreastIndex), faceName).getVisualOrderText());
                    array.add(WildfireLang.UV_QUAD.translateColored(ChatFormatting.AQUA, quad.x1(), quad.y1(), quad.x2(), quad.y2()).getVisualOrderText());
                    setTooltipForNextRenderPass(array);
                }

                int borderThickness = 1;
                graphics.fill(rectX1, rectY1, rectX2, rectY1 + borderThickness, borderColor);
                graphics.fill(rectX1, rectY2 - borderThickness, rectX2, rectY2, borderColor);
                graphics.fill(rectX1, rectY1, rectX1 + borderThickness, rectY2, borderColor);
                graphics.fill(rectX2 - borderThickness, rectY1, rectX2, rectY2, borderColor);

                //TODO: Improve the usability of the UI by not rendering face names if there is a face in front of them that is in the active layer?
                drawScaledScrollingString(graphics, faceName, rectX1 + borderThickness, rectY1 + borderThickness, rectX2 - borderThickness, rectY2 - borderThickness,
                    TextAlignment.CENTER, 0xFFFFFF, true, 0.6F);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(selectedUVs == null) return super.mouseClicked(mouseX, mouseY, button);

        for (Map.Entry<UVDirection, UVQuad> entry : selectedUVs.getAllSides().entrySet()) {
            UVDirection direction = entry.getKey();
            UVQuad quad = entry.getValue();

            if(!(quad.x1() == 0 && quad.y1() == 0 && quad.x2() == 0 && quad.y2() == 0)) {
                int rectX1 = (int) (uvWindowPos.x() + quad.x1() * uvWindowScaleFactor);
                int rectY1 = (int) (uvWindowPos.y() + (quad.y1() - 1) * uvWindowScaleFactor);
                int rectX2 = (int) (uvWindowPos.x() + quad.x2() * uvWindowScaleFactor);
                int rectY2 = (int) (uvWindowPos.y() + (quad.y2() - 1) * uvWindowScaleFactor);

                if(mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
                    if(button == InputConstants.MOUSE_BUTTON_LEFT) {

                        if(selectedDirection != direction) {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            selectedDirection = direction; // store which rect was clicked
                            rebuildWidgets();
                        }
                    } else if(button == InputConstants.MOUSE_BUTTON_RIGHT && selectedDirection != null) {
                        selectedDirection = null;
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        rebuildWidgets();
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getPositionIncrement() {
        // this should only ever be null before #init() is called, and never afterward
        Objects.requireNonNull(minecraft);
        if (Screen.hasShiftDown() && Screen.hasControlDown()) return 20;
        if (Screen.hasShiftDown()) return 10;
        return 1;
    }

}

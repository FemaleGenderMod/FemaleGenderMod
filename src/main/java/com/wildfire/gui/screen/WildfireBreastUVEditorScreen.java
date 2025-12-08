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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector2i;

import java.util.*;

public class WildfireBreastUVEditorScreen extends BaseWildfireScreen {

	private static final Component TITLE = Component.translatable("wildfire_gender.uv_editor");

	private static final Identifier TEXTURE_ADD = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/widgets/add.png");
	private static final Identifier TEXTURE_SUBTRACT = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/widgets/subtract.png");

	private @Nullable UVLayout selectedUVs = null;
	private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
	private @Nullable UVDirection selectedDirection = null;

	//Positions & Widths
	private @UnknownNullability Vector2i winElementPos, uvWindowPos;

	private static final int sidebarWidth = 180;
	private static final int textureDrawWidth = 196;
	private static final int textureSourceWidth = 64;
	private static final float uvWindowScaleFactor = (float) textureDrawWidth / (float) textureSourceWidth;

	public WildfireBreastUVEditorScreen(Screen parent, UUID uuid) {
		super(Component.translatable("wildfire_gender.uv_editor"), parent, uuid);
	}

	@Override
	public void init() {
		uvWindowPos = new Vector2i(5, this.height / 2 - textureDrawWidth / 2);
		winElementPos = new Vector2i(this.width - sidebarWidth + 7, 32);

		int x = this.width - sidebarWidth;
		int w = this.width - (this.width - sidebarWidth);
		int y = 0;

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.uv_editor.reset_defaults_all"))
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
				.message(() -> Component.translatable("wildfire_gender.uv_editor.selection.left_breast"))
				.position(winElementPos.x(), winElementPos.y() + 13)
				.size((w / 2) / 2 - 5, 15)
				.active(selectedBreastIndex != BreastTypes.LEFT)
				.onPress(button -> selectBreastUVMap(BreastTypes.LEFT)));

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.uv_editor.selection.right_breast"))
				.position(winElementPos.x() + (w / 2) / 2 - 3, winElementPos.y() + 13)
				.size((w / 2) / 2 - 6, 15)
				.active(selectedBreastIndex != BreastTypes.RIGHT)
				.onPress(button -> selectBreastUVMap(BreastTypes.RIGHT)));

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.uv_editor.selection.left_breast_overlay"))
				.position(winElementPos.x(), winElementPos.y() + 44)
				.size((w / 2) / 2 - 5, 15)
				.active(selectedBreastIndex != BreastTypes.LEFT_OVERLAY)
				.onPress(button -> selectBreastUVMap(BreastTypes.LEFT_OVERLAY)));

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.uv_editor.selection.right_breast_overlay"))
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
							ChatFormatting colorVal = increment == 10 ? ChatFormatting.AQUA :
									(increment == 20 ? ChatFormatting.BLUE : ChatFormatting.WHITE);
							ctx.blit(RenderPipelines.GUI_TEXTURED,
									isAdd ? TEXTURE_ADD : TEXTURE_SUBTRACT,
									button.getX() + button.getWidth() / 2 - 3,
									button.getY() + button.getHeight() / 2 - 3,
									0,0,6,6,6,6,6,6,
									ARGB.opaque(Objects.requireNonNull(colorVal.getColor())));
						})
						.message(() -> isAdd ? Component.translatable("wildfire_gender.uv_editor.add") : Component.translatable("wildfire_gender.uv_editor.remove"))
						.position(uvPositionWindowX + xOffset, y + buttonArrayY + yOffset)
						.size(12, 12)
						.onPress(button -> {
							if(selectedDirection == null || selectedUVs == null) return;
							final var player = Objects.requireNonNull(getPlayer(), "getPlayer()");

							UVQuad quad = selectedUVs.getAllSides().get(selectedDirection);
							assert quad != null; // TODO can this assumption ever be broken without the user meddling with the config?
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
		rebuildWidgets();
	}

	@Override
	public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		//super.renderBackground(ctx, mouseX, mouseY, delta);
		this.renderTransparentBackground(ctx);
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
	public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		if(minecraft.level == null || minecraft.player == null) return;
		var player = getPlayer();

		if(player != null && selectedUVs != null) {

			//noinspection SuspiciousNameCombination
			ctx.blit(RenderPipelines.GUI_TEXTURED, minecraft.player.getSkin().body().id(),
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

		GuiUtils.drawCenteredText(ctx, font, Component.translatable("wildfire_gender.uv_editor.selection.layer_body"),  winElementPos.x() + 42, winElementPos.y() + 2, 0xFFFFFFFF);
		GuiUtils.drawCenteredText(ctx, font, Component.translatable("wildfire_gender.uv_editor.selection.layer_jacket"),  winElementPos.x() + 42, winElementPos.y() + 32, 0xFFFFFFFF);

		int positionBoxX = this.width - sidebarWidth / 4;

		//Coordinate selector
		if(selectedDirection == null) {
			GuiUtils.drawCenteredTextWrapped(ctx, font, Component.translatable("wildfire_gender.uv_editor.no_face_selected"), positionBoxX, 60, 70, 0xFF888888);
		} else {

			GuiUtils.drawCenteredText(ctx, font, Component.empty().append(selectedDirection.getDirectionText(selectedBreastIndex)).withStyle(ChatFormatting.GOLD), positionBoxX, 37, 0xFFFFFFFF);

			ctx.drawString(font, Component.translatable("wildfire_gender.uv_editor.xpos"), positionBoxX - 35, 55, 0xFFFFFFFF, false);
			ctx.drawString(font, Component.translatable("wildfire_gender.uv_editor.ypos"), positionBoxX - 35, 55 + 14, 0xFFFFFFFF, false);
			ctx.drawString(font, Component.translatable("wildfire_gender.uv_editor.width"), positionBoxX - 35, 55 + (14*2), 0xFFFFFFFF, false);
			ctx.drawString(font, Component.translatable("wildfire_gender.uv_editor.height"), positionBoxX - 35, 55 + (14*3), 0xFFFFFFFF, false);

			ctx.pose().pushMatrix();
			ctx.pose().translate(positionBoxX, 115);
			ctx.pose().scale(0.75f);
			GuiUtils.drawCenteredTextWrapped(ctx, font, Component.translatable("wildfire_gender.uv_editor.increment_tip.line1").withStyle(ChatFormatting.AQUA), 0, -6, 120, 0xFF888888);
			GuiUtils.drawCenteredTextWrapped(ctx, font, Component.translatable("wildfire_gender.uv_editor.increment_tip.line2").withStyle(ChatFormatting.BLUE), 0, 6, 120, 0xFF888888);
			ctx.pose().popMatrix();
		}

		int modelScale = 120;
		if(Minecraft.getInstance().getWindow().getScreenWidth() < 1920) {
			modelScale = 60;
		} else if(Minecraft.getInstance().getWindow().getScreenWidth() >= 2560) {
			modelScale = 200;
		}

		InventoryScreen.renderEntityInInventoryFollowsMouse(ctx, this.width / 2 - modelScale, this.height / 2 - modelScale, this.width / 2 + modelScale, this.height / 2 + modelScale, modelScale, 0.0625f, mouseX, mouseY, minecraft.player);
		GuiUtils.drawCenteredText(ctx, font, TITLE, this.width / 2, 20, 0xFFFFFFFF);

		super.render(ctx, mouseX, mouseY, delta);
	}

	private void drawFaceBorders(GuiGraphics ctx, UVLayout uvList, int mouseX, int mouseY, boolean faded) {

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
					List<FormattedCharSequence> array = new ArrayList<>();
					array.add(Component.empty().append(direction.getDirectionText(selectedBreastIndex)).append(" (").append(faceName).append(")").withStyle(ChatFormatting.GOLD).getVisualOrderText());
					array.add(Component.empty().append("[" + quad.x1() + ", " + quad.y1() + ", " + quad.x2() + ", " + quad.y2() + "]").withStyle(ChatFormatting.AQUA).getVisualOrderText());
					ctx.setTooltipForNextFrame(array, mouseX, mouseY);
				}

				int borderThickness = 1;
				ctx.fill(rectX1, rectY1, rectX2, rectY1 + borderThickness, borderColor);
				ctx.fill(rectX1, rectY2 - borderThickness, rectX2, rectY2, borderColor);
				ctx.fill(rectX1, rectY1, rectX1 + borderThickness, rectY2, borderColor);
				ctx.fill(rectX2 - borderThickness, rectY1, rectX2, rectY2, borderColor);

				int centerX = (rectX1 + rectX2) / 2;
				int centerY = (rectY1 + rectY2) / 2;
				int textWidth = font.width(faceName);
				int textHeight = font.lineHeight;

				ctx.pose().pushMatrix();
				ctx.pose().translate(centerX, centerY);
				ctx.pose().scale(0.6f);

				ctx.drawString(font, faceName, -textWidth / 2, -textHeight / 2, 0xFFFFFFFF, true);

				ctx.pose().popMatrix();

			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
							Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
							selectedDirection = direction; // store which rect was clicked
							rebuildWidgets();
						}
					} else if(click.button() == 1 && selectedDirection != null) {
						selectedDirection = null;
						Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
						rebuildWidgets();
					}
					return true;
				}
			}
		}

		return super.mouseClicked(click, doubled);
	}

	private int getPositionIncrement() {
		// this should only ever be null before #init() is called, and never afterward
		Objects.requireNonNull(minecraft);
		if (minecraft.hasShiftDown() && minecraft.hasControlDown()) return 20;
		if (minecraft.hasShiftDown()) return 10;
		return 1;
	}

}

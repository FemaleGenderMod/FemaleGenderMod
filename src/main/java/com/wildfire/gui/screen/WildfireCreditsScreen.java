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

import com.wildfire.gui.FakeGUIPlayer;
import com.wildfire.gui.GuiUtils;
import com.wildfire.main.GenderConfigs;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.contributors.Contributors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import java.util.*;

@Environment(EnvType.CLIENT)
public class WildfireCreditsScreen extends BaseWildfireScreen {

	private static final Identifier CREDIT_CONTAINER = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/credits/credit_container.png");
	private static final Identifier CREDIT_OUTLINE = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/credits/credit_outline.png");
	private static final Identifier BUTTON_CONTAINER = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/credits/button_container.png");
	private static final Identifier TAB_CONTAINER = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/credits/tab_container.png");

	//General contributor list
	private final FakeGUIPlayer[] C_GENERAL = Contributors.getContributors().entrySet().stream()
			.filter(it -> it.getValue().name() != null)
			.filter(it -> Boolean.TRUE.equals(it.getValue().showInCredits()))
			.filter(it -> it.getValue().getRole() != Contributor.Role.TRANSLATOR) // exclude translators
			.sorted(Comparator.comparing(it -> it.getValue().name()))
			.sorted(Comparator.comparing(it -> it.getValue().getRole()))
			.map(it -> new FakeGUIPlayer(it.getValue().name(), it.getKey(), GenderConfigs.DEFAULT_FEMALE))
			.toArray(FakeGUIPlayer[]::new);

	//Translator list
	private final FakeGUIPlayer[] C_TRANSLATORS = Contributors.getContributors().entrySet().stream()
			.filter(it -> it.getValue().name() != null)
			.filter(it -> Boolean.TRUE.equals(it.getValue().showInCredits()))
			.filter(it -> it.getValue().getRole() == Contributor.Role.TRANSLATOR) // only have translators
			.sorted(Comparator.comparing(it -> it.getValue().name()))
			.sorted(Comparator.comparing(it -> it.getValue().getRole()))
			.map(it -> new FakeGUIPlayer(it.getValue().name(), it.getKey(), GenderConfigs.DEFAULT_FEMALE))
			.toArray(FakeGUIPlayer[]::new);

	private final int boxesPerPage = 12;

	private enum Category {
		GENERAL, TRANSLATORS
	}
	private Category categoryTab = Category.GENERAL;
	private int creditsPage = 0;

	public WildfireCreditsScreen(Screen parent, UUID uuid) {
		super(Component.translatable("wildfire_gender.credits.title"), parent, uuid);
	}

	private int navigationY;
	@Override
	public void init() {

		final var ref = new Object() {
			@UnknownNullability
			AbstractWidget prevPage, nextPage, generalTab, translatorTab;
		};

		navigationY = this.height / 2 + 82;

		//category tab
		ref.generalTab = addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.credits.general"))
				.position(this.width / 2 - 89, navigationY + 34)
				.size(87, 13)
				.active(categoryTab == Category.TRANSLATORS)
				.onPress(button -> {
					categoryTab = Category.GENERAL;
					creditsPage = 0;
					ref.prevPage.active = false;
					ref.nextPage.active = creditsPage < getTotalPages()-1;
					ref.generalTab.active = false;
					ref.translatorTab.active = true;

				}));

		ref.translatorTab = addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.credits.translators"))
				.position(this.width / 2 + 2, navigationY + 34)
				.size(87, 13)
				.active(categoryTab == Category.GENERAL)
				.onPress(button -> {
					categoryTab = Category.TRANSLATORS;
					creditsPage = 0;
					ref.prevPage.active = false;
					ref.nextPage.active = creditsPage < getTotalPages()-1;
					ref.generalTab.active = true;
					ref.translatorTab.active = false;
				}));

		//page tab
		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.details.go_back"))
				.position(this.width / 2 - 25, navigationY + 6)
				.size(50, 13)
				.onPress(button -> onClose()));

		ref.nextPage = addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.details.next_page"))
				.position(this.width / 2 + 29, navigationY + 6)
				.size(60, 13)
				.active(creditsPage < getTotalPages()-1)
				.onPress(button -> {
					if(creditsPage < getTotalPages()-1) {
						creditsPage++;
					}
					ref.prevPage.active = creditsPage != 0;
					ref.nextPage.active = creditsPage < getTotalPages()-1;
				}));

		ref.prevPage = addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.details.prev_page"))
				.position(this.width / 2 - 89, navigationY + 6)
				.size(60, 13)
				.active(creditsPage != 0)
				.onPress(button -> {
					if(creditsPage > 0) {
						creditsPage--;
					}
					ref.prevPage.active = creditsPage != 0;
					ref.nextPage.active = creditsPage < getTotalPages();
				}));
	}

	@Override
	public void tick() {
		for(FakeGUIPlayer player : getActiveBoxes()) {
			player.tick();
		}
	}

	private int getTotalPages() {
		return (int) Math.ceil((double) getActiveBoxes().length / boxesPerPage);
	}

	private FakeGUIPlayer[] getActiveBoxes() {
		return categoryTab == Category.TRANSLATORS ? C_TRANSLATORS : C_GENERAL;
	}

	@Override
	public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(ctx);
	}

	@Override
	public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {

		Matrix3x2fStack mStack = ctx.pose();

		mStack.pushMatrix();
		GuiUtils.drawCenteredText(ctx, font, Component.translatable("wildfire_gender.credits.title"), width / 2, height / 2 - 100, ARGB.opaque(0xFFFFFF));
		GuiUtils.drawCenteredText(ctx, font, Component.translatable("wildfire_gender.credits.description"), width / 2, height / 2 - 85, ARGB.opaque(0x888888));
		mStack.popMatrix();

		ctx.blit(RenderPipelines.GUI_TEXTURED, BUTTON_CONTAINER, this.width / 2 - (190 / 2), navigationY, 0, 0, 190, 25, 190, 25);
		ctx.blit(RenderPipelines.GUI_TEXTURED, TAB_CONTAINER, this.width / 2 - (190 / 2), navigationY + 28, 0, 0, 190, 25, 190, 25);

		int columns = 6;
		int boxW = 60;
		int boxH = 74;

		int startIndex = creditsPage * boxesPerPage;
		int endIndex = Math.min(startIndex + boxesPerPage, getActiveBoxes().length);

		int startY = height / 2 - (2 * boxH) / 2 + 4;

		for (int i = startIndex; i < endIndex; i++) {
			var creditBox = getActiveBoxes()[i];

			int localIndex = i - startIndex;
			int col = localIndex % columns;
			int row = localIndex / columns;

			int remaining = Math.min(endIndex - startIndex - (row * columns), columns);
			int rowWidth = remaining * boxW;
			int startX = (width / 2) - (rowWidth / 2) + 4;

			int creditBoxX = startX + (col * boxW);
			int creditBoxY = startY + (row * boxH);

			ctx.blit(RenderPipelines.GUI_TEXTURED, CREDIT_CONTAINER, creditBoxX, creditBoxY, 0, 0, 52, 68, 52, 68);

			ctx.pose().pushMatrix();
			int color = ARGB.opaque(Objects.requireNonNull(creditBox.getRole()).getColor());
			ctx.blit(RenderPipelines.GUI_TEXTURED, CREDIT_OUTLINE, creditBoxX + 3, creditBoxY + 3, 0, 0, 46, 53, 46, 53, color);
			ctx.pose().popMatrix();

			int xP = creditBoxX + (52 / 2);
			int yP = creditBoxY + (68 / 2);
			ctx.enableScissor(xP - 21, yP - 79, xP + 21, yP + 20);
			GuiUtils.drawEntityOnScreen(ctx, xP - 38, yP - 29, xP + 38, yP + 59, 40, mouseX, mouseY + 35, creditBox.getEntity());
			ctx.disableScissor();

			mStack.pushMatrix();
			mStack.translate(xP, yP + 47);
			mStack.scale(new Vector2f(0.55f, 0.55f));
			mStack.translate(-xP, (-yP) - 47);
			GuiUtils.drawCenteredTextWrapped(ctx, font, Component.literal(creditBox.getName()), xP, yP + 7, (int) (50 * 1.45f), ARGB.opaque(0xFFFFFF));
			mStack.popMatrix();

			if (mouseX > xP - 24 && mouseX < xP + 23 && mouseY > yP + 22 && mouseY < yP + 31) {
				List<Component> txtList = new ArrayList<>();
				var role = creditBox.getRoleOrGeneric();
				txtList.add(role.withColor(Component.empty()
						.append(creditBox.getName())
						.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
						.append(role.shortName())));
				if (creditBox.getDescription() != null && !creditBox.getDescription().isEmpty()) {
					txtList.add(Component.literal(creditBox.getDescription()).withStyle(ChatFormatting.GRAY));
				}
				ctx.setComponentTooltipForNextFrame(font, txtList, mouseX, mouseY);
			}
		}

		//String pageInfo = (creditsPage) + " / " + (totalPages-1);
		//GuiUtils.drawCenteredText(ctx, textRenderer, Text.literal(pageInfo), width / 2, height / 2, ColorHelper.fullAlpha(0xFFFFFF));

		super.render(ctx, mouseX, mouseY, delta);
	}
}

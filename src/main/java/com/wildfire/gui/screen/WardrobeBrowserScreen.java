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
import com.wildfire.gui.SyncedPlayerList;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class WardrobeBrowserScreen extends BaseWildfireScreen {
	private static final Identifier BACKGROUND_MALE = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/wardrobe_bg_male.png");
	private static final Identifier BACKGROUND_FEMALE = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/wardrobe_bg_female.png");
	private static final Identifier BACKGROUND_OTHER = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/wardrobe_bg_other.png");

	private static final Identifier TXTR_RIBBON = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/bc_ribbon.png");
	private static final Identifier CLOUD_ICON = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/cloud.png");

	private static final boolean isBreastCancerAwarenessMonth = Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER;

	private final WidgetTooltipHolder contribTooltip = new WidgetTooltipHolder();

	public WardrobeBrowserScreen(@Nullable Screen parent, UUID uuid) {
		super(Component.translatable("wildfire_gender.wardrobe.title"), parent, uuid);
	}

	public static BaseWildfireScreen create(LocalPlayer player, @Nullable Screen parent) {
		if(ClientConfig.INSTANCE.get(ClientConfig.FIRST_TIME_LOAD) && CloudSync.isAvailable()) {
			return new WildfireFirstTimeSetupScreen(parent, player.getUUID());
		} else {
			return new WardrobeBrowserScreen(parent, player.getUUID());
		}
	}

	public static void open(Minecraft client, LocalPlayer player) {
		client.setScreen(create(player, null));
	}

	@Override
	public void init() {
		final var client = Objects.requireNonNull(this.minecraft, "client");
		int y = this.height / 2;
		PlayerConfig plr = Objects.requireNonNull(getPlayer(), "getPlayer()");

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.always_show_list", ClientConfig.INSTANCE.get(ClientConfig.ALWAYS_SHOW_LIST).text()))
				.tooltip(ClientConfig.INSTANCE.get(ClientConfig.ALWAYS_SHOW_LIST).tooltip())
				.position(126, 4)
				.size(185, 10)
				.onPress(button -> {
					var config = ClientConfig.INSTANCE;
					var newVal = config.get(ClientConfig.ALWAYS_SHOW_LIST).next();
					config.set(ClientConfig.ALWAYS_SHOW_LIST, newVal);
					config.save();
					button.updateMessage();
					button.setTooltip(newVal.tooltip());
				}));

		addButton(builder -> builder
				.message(() -> plr.getGender().getDisplayName())
				.position(this.width / 2 - 130, this.height / 2 + 33)
				.size(80, 15)
				.onPress(button -> {
					plr.updateGender(plr.getGender().next());
					plr.save();
					rebuildWidgets();
				}));

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.appearance_settings.title").append("..."))
				.position(this.width / 2 - 36, this.height / 2 - 63)
				.size(157, 20)
				.onPress(button -> {
					client.setScreen(new WildfireBreastCustomizationScreen(WardrobeBrowserScreen.this, this.playerUUID));
				})
				.active(plr.getGender().canHaveBreasts()));

		addButton(builder -> {
			builder.message(() -> Component.translatable("wildfire_gender.cloud_settings"));
			builder.position(this.width / 2 - 36, y + 30);
			builder.size(24, 18);
			builder.renderer((button, ctx, mouseX, mouseY, partialTicks) -> {
				ctx.blit(RenderPipelines.GUI_TEXTURED, CLOUD_ICON, button.getX() + 2, button.getY() + 2, 0, 0, 20, 14, 32, 26, 32, 26);
			});
			builder.onPress(button -> {
				client.setScreen(new WildfireCloudSyncScreen(this, this.playerUUID));
			});
			var cloudUnavailable = CloudSync.unavailableReason();
			if(cloudUnavailable != null) {
				builder.tooltip(Tooltip.create(cloudUnavailable.text()));
				builder.active(false);
			} else {
				builder.tooltip(Tooltip.create(Component.translatable("wildfire_gender.cloud.tooltip")));
			}
		});

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.credits.title").append("..."))
				.position(this.width / 2 + 2, this.height / 2 + 33)
				.size(78, 15)
				.onPress(button -> {
					client.setScreen(new WildfireCreditsScreen(WardrobeBrowserScreen.this, this.playerUUID));
				}));

		/*this.addDrawableChild(new WildfireButton(this.width / 2 + 111, y - 63, 9, 9, Text.literal("X"),
			button -> close(), text -> GuiUtils.doneNarrationText()));*/
	}

	@Override
	public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(ctx);

		PlayerConfig plr = getPlayer();
		if(plr == null) return;
		Identifier backgroundTexture = switch(plr.getGender()) {
			case Gender.MALE -> BACKGROUND_MALE;
			case Gender.FEMALE -> BACKGROUND_FEMALE;
			case Gender.OTHER -> BACKGROUND_OTHER;
		};

		ctx.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 268, 124, 512, 512);

		renderPlayerInFrame(ctx, this.width / 2 - 90, this.height / 2 + 18, mouseX, mouseY);
	}

	@Override
	public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int x = this.width / 2;
		int y = this.height / 2;
		ctx.drawString(font, getTitle(), x - font.width(getTitle()) / 2, y - 82, 0xFFFFFF, false);

		drawCreatorContributorText(ctx, mouseX, mouseY, y + 65 + (isBreastCancerAwarenessMonth ? 30 : 0));

		if(isBreastCancerAwarenessMonth) {
			int bcaY = y - 45;
			ctx.fill(x - 159, bcaY + 106, x + 159, bcaY + 136, 0x55000000);
			ctx.drawString(font, Component.translatable("wildfire_gender.cancer_awareness.title").withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC), this.width / 2 - 148, bcaY + 117, 0xFFFFFFFF);
			ctx.blit(RenderPipelines.GUI_TEXTURED, TXTR_RIBBON, x + 130, bcaY + 109, 0, 0, 26, 26, 20, 20, 20, 20);
		}

		SyncedPlayerList.drawSyncedPlayers(ctx, font);
	}

	private void drawCreatorContributorText(GuiGraphics ctx, int mouseX, int mouseY, int creatorY) {
		final var client = Objects.requireNonNull(this.minecraft);
		if(client.player == null || client.level == null) return;
		Map<UUID, PlayerInfo> entries = client.player.connection.getOnlinePlayers()
				.stream().collect(Collectors.toMap(entry -> entry.getProfile().id(), Function.identity()));

		final boolean withCreator = entries.containsKey(Contributors.CREATOR_UUID);
		final var foundContributors = Contributors.getContributorUUIDs().stream()
				.filter(it -> !it.equals(Contributors.CREATOR_UUID))
				.map(entries::get)
				.filter(Objects::nonNull)
				.toList();

		if(!withCreator && foundContributors.isEmpty()) {
			return;
		}

		final Component text;
		final var toList = new ArrayList<>(foundContributors);
		if(withCreator && !foundContributors.isEmpty()) {
			text = Component.translatable("wildfire_gender.label.with_both");
			toList.addFirst(entries.get(Contributors.CREATOR_UUID));
		} else if(withCreator) {
			text = Component.translatable("wildfire_gender.label.with_creator");
		} else {
			text = Component.translatable("wildfire_gender.label.with_contributor");
		}

		int textWidth = font.width(text);
		GuiUtils.drawCenteredTextWrapped(ctx, this.font, text, this.width / 2, creatorY, 300, ARGB.opaque(0xFF00FF));

		// Render a tooltip with the relevant player names when hovered over
		int lines = (int) Math.ceil(textWidth / 300.0);
		if(!toList.isEmpty()
				&& mouseX > this.width / 2 - textWidth / 2 && mouseX < this.width / 2 + textWidth / 2
				&& mouseY > creatorY - 2 && mouseY < creatorY + (9 * lines)) {
			var contributorNames = toList.stream()
					.map(entry -> PlayerTeam.formatNameForTeam(entry.getTeam(), Component.nullToEmpty(entry.getProfile().name())))
					.toList();

			contribTooltip.set(Tooltip.create(ComponentUtils.formatList(contributorNames, Component.literal("\n"))));
			contribTooltip.refreshTooltipForNextRenderPass(ctx, mouseX, mouseY, true, true, ScreenRectangle.empty());
		}
	}
}
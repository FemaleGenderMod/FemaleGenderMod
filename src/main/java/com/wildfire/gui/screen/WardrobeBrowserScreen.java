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
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.WildfireGender;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WardrobeBrowserScreen extends BaseWildfireScreen {
	private static final Identifier BACKGROUND_MALE = Identifier.of(WildfireGender.MODID, "textures/gui/wardrobe_bg_male.png");
	private static final Identifier BACKGROUND_FEMALE = Identifier.of(WildfireGender.MODID, "textures/gui/wardrobe_bg_female.png");
	private static final Identifier BACKGROUND_OTHER = Identifier.of(WildfireGender.MODID, "textures/gui/wardrobe_bg_other.png");

	private static final Identifier TXTR_RIBBON = Identifier.of(WildfireGender.MODID, "textures/bc_ribbon.png");
	private static final Identifier CLOUD_ICON = Identifier.of(WildfireGender.MODID, "textures/cloud.png");

	private static final boolean isBreastCancerAwarenessMonth = Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER;

	private final TooltipState contribTooltip = new TooltipState();

	public WardrobeBrowserScreen(Screen parent, UUID uuid) {
		super(Text.translatable("wildfire_gender.wardrobe.title"), parent, uuid);
	}

	public static BaseWildfireScreen create(ClientPlayerEntity player, @Nullable Screen parent) {
		if(ClientConfig.INSTANCE.get(ClientConfig.FIRST_TIME_LOAD) && CloudSync.isAvailable()) {
			return new WildfireFirstTimeSetupScreen(parent, player.getUuid());
		} else {
			return new WardrobeBrowserScreen(parent, player.getUuid());
		}
	}

	public static void open(MinecraftClient client, ClientPlayerEntity player) {
		client.setScreen(create(player, null));
	}

	@Override
	public void init() {
		final var client = Objects.requireNonNull(this.client, "client");
		int y = this.height / 2;
		PlayerConfig plr = Objects.requireNonNull(getPlayer(), "getPlayer()");

		addButton(builder -> builder
				.message(() -> Text.translatable("wildfire_gender.always_show_list", ClientConfig.INSTANCE.get(ClientConfig.ALWAYS_SHOW_LIST).text()))
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
					clearAndInit();
				}));

		addButton(builder -> builder
				.message(() -> Text.translatable("wildfire_gender.appearance_settings.title").append("..."))
				.position(this.width / 2 - 36, this.height / 2 - 63)
				.size(157, 20)
				.onPress(button -> {
					client.setScreen(new WildfireBreastCustomizationScreen(WardrobeBrowserScreen.this, this.playerUUID));
				})
				.active(plr.getGender().canHaveBreasts()));

		addButton(builder -> {
			builder.message(() -> Text.translatable("wildfire_gender.cloud_settings"));
			builder.position(this.width / 2 - 36, y + 30);
			builder.size(24, 18);
			builder.renderer((button, ctx, mouseX, mouseY, partialTicks) -> {
				ctx.drawTexture(RenderPipelines.GUI_TEXTURED, CLOUD_ICON, button.getX() + 2, button.getY() + 2, 0, 0, 20, 14, 32, 26, 32, 26);
			});
			builder.onPress(button -> {
				client.setScreen(new WildfireCloudSyncScreen(this, this.playerUUID));
			});
			var cloudUnavailable = CloudSync.unavailableReason();
			if(cloudUnavailable != null) {
				builder.tooltip(Tooltip.of(cloudUnavailable.text()));
				builder.active(false);
			} else {
				builder.tooltip(Tooltip.of(Text.translatable("wildfire_gender.cloud.tooltip")));
			}
		});

		addButton(builder -> builder
				.message(() -> Text.translatable("wildfire_gender.credits.title").append("..."))
				.position(this.width / 2 + 2, this.height / 2 + 33)
				.size(78, 15)
				.onPress(button -> {
					client.setScreen(new WildfireCreditsScreen(WardrobeBrowserScreen.this, this.playerUUID));
				}));

		/*this.addDrawableChild(new WildfireButton(this.width / 2 + 111, y - 63, 9, 9, Text.literal("X"),
			button -> close(), text -> GuiUtils.doneNarrationText()));*/
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(ctx);

		PlayerConfig plr = getPlayer();
		if(plr == null) return;
		Identifier backgroundTexture = switch(plr.getGender()) {
			case Gender.MALE -> BACKGROUND_MALE;
			case Gender.FEMALE -> BACKGROUND_FEMALE;
			case Gender.OTHER -> BACKGROUND_OTHER;
		};

		ctx.drawTexture(RenderPipelines.GUI_TEXTURED, backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 268, 124, 512, 512);

		renderPlayerInFrame(ctx, this.width / 2 - 90, this.height / 2 + 18, mouseX, mouseY);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int x = this.width / 2;
		int y = this.height / 2;
		ctx.drawText(textRenderer, getTitle(), x - textRenderer.getWidth(getTitle()) / 2, y - 82, 0xFFFFFF, false);

		drawCreatorContributorText(ctx, mouseX, mouseY, y + 65 + (isBreastCancerAwarenessMonth ? 30 : 0));

		if(isBreastCancerAwarenessMonth) {
			int bcaY = y - 45;
			ctx.fill(x - 159, bcaY + 106, x + 159, bcaY + 136, 0x55000000);
			ctx.drawTextWithShadow(textRenderer, Text.translatable("wildfire_gender.cancer_awareness.title").formatted(Formatting.BOLD, Formatting.ITALIC), this.width / 2 - 148, bcaY + 117, 0xFFFFFFFF);
			ctx.drawTexture(RenderPipelines.GUI_TEXTURED, TXTR_RIBBON, x + 130, bcaY + 109, 0, 0, 26, 26, 20, 20, 20, 20);
		}

		SyncedPlayerList.drawSyncedPlayers(ctx, textRenderer);
	}

	private void drawCreatorContributorText(DrawContext ctx, int mouseX, int mouseY, int creatorY) {
		final var client = Objects.requireNonNull(this.client);
		if(client.player == null || client.world == null) return;
		Map<UUID, PlayerListEntry> entries = client.player.networkHandler.getPlayerList()
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

		final Text text;
		final var toList = new ArrayList<>(foundContributors);
		if(withCreator && !foundContributors.isEmpty()) {
			text = Text.translatable("wildfire_gender.label.with_both");
			toList.addFirst(entries.get(Contributors.CREATOR_UUID));
		} else if(withCreator) {
			text = Text.translatable("wildfire_gender.label.with_creator");
		} else {
			text = Text.translatable("wildfire_gender.label.with_contributor");
		}

		int textWidth = textRenderer.getWidth(text);
		GuiUtils.drawCenteredTextWrapped(ctx, this.textRenderer, text, this.width / 2, creatorY, 300, ColorHelper.fullAlpha(0xFF00FF));

		// Render a tooltip with the relevant player names when hovered over
		int lines = (int) Math.ceil(textWidth / 300.0);
		if(!toList.isEmpty()
				&& mouseX > this.width / 2 - textWidth / 2 && mouseX < this.width / 2 + textWidth / 2
				&& mouseY > creatorY - 2 && mouseY < creatorY + (9 * lines)) {
			var contributorNames = toList.stream()
					.filter(Objects::nonNull)
					.map(entry -> Team.decorateName(entry.getScoreboardTeam(), Text.of(entry.getProfile().name())))
					.toList();

			contribTooltip.setTooltip(Tooltip.of(Texts.join(contributorNames, Text.literal("\n"))));
			contribTooltip.render(ctx, mouseX, mouseY, true, true, ScreenRect.empty());
		}
	}
}
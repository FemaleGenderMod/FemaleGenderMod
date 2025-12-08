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

import com.google.common.base.Suppliers;
import com.wildfire.gui.FakeGUIPlayer;
import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.GenderConfigs;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector2f;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class WildfireFirstTimeSetupScreen extends BaseWildfireScreen {

	private static final Component TITLE = Component.translatable("wildfire_gender.first_time_setup.title").withStyle(ChatFormatting.UNDERLINE);
	private static final Component DESCRIPTION = Component.translatable("wildfire_gender.first_time_setup.description");
	private static final Component NOTICE = Component.translatable("wildfire_gender.first_time_setup.notice");

	private static final Component ENABLE_CLOUD_SYNCING = Component.translatable("wildfire_gender.first_time_setup.enable").withStyle(ChatFormatting.GREEN);
	private static final Component DISABLE_CLOUD_SYNCING = Component.translatable("wildfire_gender.first_time_setup.disable").withStyle(ChatFormatting.RED);

	private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/first_time_bg.png");

	private static final UUID keiraUUID = UUID.fromString("372271ab-28f2-44bd-b585-95f43e010c22");

	private final Supplier<FakeGUIPlayer> fakeKeira = Suppliers.memoize(() -> new FakeGUIPlayer("KeiaraFGM", keiraUUID, GenderConfigs.DEFAULT_FEMALE));

	public WildfireFirstTimeSetupScreen(@Nullable Screen parent, UUID uuid) {
		super(Component.translatable("wildfire_gender.cloud_settings"), parent, uuid);
	}

	@Override
	public void init() {
		int x = this.width / 2;
		int y = this.height / 2;

		final var config = ClientConfig.INSTANCE;
		final var ref = new Object() {
			@UnknownNullability
			WildfireButton no;
		};

		addButton(builder -> builder
				.message(() -> ENABLE_CLOUD_SYNCING)
				.position(x + 3, y + 74)
				.size(128, 20)
				.onPress(button -> {
					config.set(ClientConfig.CLOUD_SYNC_ENABLED, true);
					config.set(ClientConfig.AUTOMATIC_CLOUD_SYNC, true);
					config.set(ClientConfig.FIRST_TIME_LOAD, false);

					button.active = false;
					button.setMessage(Component.literal("..."));
					ref.no.setActive(false);

					final var nextScreen = new WardrobeBrowserScreen(null, playerUUID);
					doInitialSync().thenRun(() -> minecraft.execute(() -> minecraft.setScreen(nextScreen)));
				})
				.tooltip(Tooltip.create(Component.empty()
						.append(Component.translatable("wildfire_gender.first_time_setup.enable.tooltip.line1"))
						.append("\n\n")
						.append(Component.translatable("wildfire_gender.first_time_setup.enable.tooltip.line2")))));

		ref.no = addButton(builder -> builder
				.message(() -> DISABLE_CLOUD_SYNCING)
				.position(x - 131, y + 74)
				.size(128, 20)
				.onPress(button -> {
					config.set(ClientConfig.CLOUD_SYNC_ENABLED, false);
					config.set(ClientConfig.AUTOMATIC_CLOUD_SYNC, false);
					config.set(ClientConfig.FIRST_TIME_LOAD, false);

					minecraft.setScreen(new WardrobeBrowserScreen(null, playerUUID));
				}));
	}

	private CompletableFuture<Void> doInitialSync() {
		var client = Objects.requireNonNull(this.minecraft);
		assert client.player != null;
		var clientUUID = client.player.getUUID();

		WildfireGender.CACHE.asMap().values()
			.removeIf(config -> config.syncStatus == PlayerConfig.SyncStatus.UNKNOWN);

		return CompletableFuture.runAsync(() -> {
			var clientConfig = WildfireGender.getOrAddPlayerById(clientUUID);
			if(!clientConfig.hasLocalConfig()) {
				try {
					// note that we wait for this to ensure that we don't have any inconsistencies with the synced
					// data once we open the main menu
					WildfireGenderClient.loadGenderInfo(clientUUID, false, true).join();
				} catch(CompletionException ignored) {
					// loadGenderInfo should log any errors for us
					return;
				} catch(Exception e) {
					WildfireGender.LOGGER.error("Failed to perform initial sync from the cloud", e);
					return;
				}
				clientConfig.save();
				// don't immediately re-sync the data we just got back to the cloud
				clientConfig.needsCloudSync = false;
			} else {
				// simply assume that the config is already loaded, so no need to wait.
				clientConfig.needsCloudSync = true;
			}
		});
	}

	@Override
	public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(ctx);
		ctx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 274) / 2, (this.height - 200) / 2, 0, 0, 274, 200, 512, 512);
	}

	@Override
	public void tick() {
		this.fakeKeira.get().tick();
	}

	@Override
	public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		if(minecraft.level == null) return;
		super.render(ctx, mouseX, mouseY, delta);

		var mStack = ctx.pose();

		int x = this.width / 2;
		int y = this.height / 2;

		GuiUtils.drawCenteredText(ctx, font, TITLE, x, y - 24, ARGB.opaque(4210752));

		GuiUtils.drawCenteredTextWrapped(ctx, font, Component.literal("Keira Emberlyn:").withStyle(ChatFormatting.LIGHT_PURPLE), x + 32, y - 10, (int) ((256-65)), ARGB.opaque(0xFFFFFF));

		//TODO: Vertical scroll bar for longer text?
		GuiUtils.drawCenteredTextWrapped(ctx, font, DESCRIPTION, x + 32, y + 2, (int) ((256-65)), ARGB.opaque(0xFFFFFF));

		mStack.pushMatrix();
		mStack.translate(x, y + 47);
		mStack.scale(new Vector2f(0.8f, 0.8f));
		mStack.translate(-x, (-y) - 47);
		GuiUtils.drawCenteredTextWrapped(ctx, font, NOTICE, x, y + 68, (int) ((256-10) * 1.2f), ARGB.opaque(4210752));
		mStack.popMatrix();

		var fakeKeira = this.fakeKeira.get().getEntity();
		GuiUtils.drawEntityOnScreen(ctx, x - 132, y - 13, x - 75, y + 60, 50, mouseX, mouseY, 0, 0.4f, fakeKeira);
	}

	@Override
	public void removed() {
		ClientConfig.INSTANCE.save();
	}
}
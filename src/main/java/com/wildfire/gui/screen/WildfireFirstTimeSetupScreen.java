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
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2f;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class WildfireFirstTimeSetupScreen extends BaseWildfireScreen {

	private static final Text TITLE = Text.translatable("wildfire_gender.first_time_setup.title").formatted(Formatting.UNDERLINE);
	private static final Text DESCRIPTION = Text.translatable("wildfire_gender.first_time_setup.description");
	private static final Text NOTICE = Text.translatable("wildfire_gender.first_time_setup.notice");

	private static final Text ENABLE_CLOUD_SYNCING = Text.translatable("wildfire_gender.first_time_setup.enable").formatted(Formatting.GREEN);
	private static final Text DISABLE_CLOUD_SYNCING = Text.translatable("wildfire_gender.first_time_setup.disable").formatted(Formatting.RED);

	private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/first_time_bg.png");

	private static final UUID keiraUUID = UUID.fromString("372271ab-28f2-44bd-b585-95f43e010c22");

	private final Supplier<FakeGUIPlayer> fakeKeira = Suppliers.memoize(() -> new FakeGUIPlayer("KeiaraFGM", keiraUUID, GenderConfigs.DEFAULT_FEMALE));

	public WildfireFirstTimeSetupScreen(Screen parent, UUID uuid) {
		super(Text.translatable("wildfire_gender.cloud_settings"), parent, uuid);
	}

	@Override
	public void init() {
		int x = this.width / 2;
		int y = this.height / 2;

		final var config = ClientConfig.INSTANCE;
		final var ref = new Object() {
			WildfireButton no = null;
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
					button.setMessage(Text.literal("..."));
					ref.no.setActive(false);

					final var nextScreen = new WardrobeBrowserScreen(null, playerUUID);
					doInitialSync().thenRun(() -> client.execute(() -> client.setScreen(nextScreen)));
				})
				.tooltip(Tooltip.of(Text.empty()
						.append(Text.translatable("wildfire_gender.first_time_setup.enable.tooltip.line1"))
						.append("\n\n")
						.append(Text.translatable("wildfire_gender.first_time_setup.enable.tooltip.line2")))));

		ref.no = addButton(builder -> builder
				.message(() -> DISABLE_CLOUD_SYNCING)
				.position(x - 131, y + 74)
				.size(128, 20)
				.onPress(button -> {
					config.set(ClientConfig.CLOUD_SYNC_ENABLED, false);
					config.set(ClientConfig.AUTOMATIC_CLOUD_SYNC, false);
					config.set(ClientConfig.FIRST_TIME_LOAD, false);

					client.setScreen(new WardrobeBrowserScreen(null, playerUUID));
				}));
	}

	private CompletableFuture<Void> doInitialSync() {
		var client = Objects.requireNonNull(this.client);
		var clientUUID = client.player.getUuid();

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
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(ctx);
		ctx.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 274) / 2, (this.height - 200) / 2, 0, 0, 274, 200, 512, 512);
	}

	@Override
	public void tick() {
		this.fakeKeira.get().tick();
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		if(client == null || client.world == null) return;
		super.render(ctx, mouseX, mouseY, delta);

		var mStack = ctx.getMatrices();

		int x = this.width / 2;
		int y = this.height / 2;

		GuiUtils.drawCenteredText(ctx, textRenderer, TITLE, x, y - 24, ColorHelper.fullAlpha(4210752));

		GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.literal("Keira Emberlyn:").formatted(Formatting.LIGHT_PURPLE), x + 32, y - 10, (int) ((256-65)), ColorHelper.fullAlpha(0xFFFFFF));

		//TODO: Vertical scroll bar for longer text?
		GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, DESCRIPTION, x + 32, y + 2, (int) ((256-65)), ColorHelper.fullAlpha(0xFFFFFF));

		mStack.pushMatrix();
		mStack.translate(x, y + 47);
		mStack.scale(new Vector2f(0.8f, 0.8f));
		mStack.translate(-x, (-y) - 47);
		GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, NOTICE, x, y + 68, (int) ((256-10) * 1.2f), ColorHelper.fullAlpha(4210752));
		mStack.popMatrix();

		var fakeKeira = this.fakeKeira.get().getEntity();
		GuiUtils.drawEntityOnScreenNoScissor(ctx, 0, 0.4f, x - 132, y - 13, x - 75, y + 60, 50, mouseX, mouseY, fakeKeira);
	}

	@Override
	public void removed() {
		ClientConfig.INSTANCE.save();
	}
}
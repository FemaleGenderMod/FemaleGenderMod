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
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.SyncLog;
import com.wildfire.main.cloud.SyncingTooFrequentlyException;
import com.wildfire.main.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Environment(EnvType.CLIENT)
public class WildfireCloudSyncScreen extends BaseWildfireScreen {

	private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/sync_bg_v2.png");

	protected WildfireCloudSyncScreen(Screen parent, UUID uuid) {
		super(Component.translatable("wildfire_gender.cloud_settings"), parent, uuid);
	}

	@Override
	public void init() {
		int x = this.width / 2;
		int y = this.height / 2;
		int yPos = y - 47;
		int xPos = x - 156 / 2 - 1;

		final var config = ClientConfig.INSTANCE;
		final var ref = new Object() {
			WildfireButton btnSyncNow, btnDelete, btnAutomaticSync;
		};

		addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.cloud.status", CloudSync.isEnabled() ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED))
				.position(xPos, yPos)
				.size(157, 20)
				.onPress(button -> {
					boolean enabled = config.toggle(ClientConfig.CLOUD_SYNC_ENABLED);
					boolean available = CloudSync.isAvailable();

					button.updateMessage();
					ref.btnAutomaticSync.setActive(enabled);
					ref.btnSyncNow.visible = enabled && available;
					ref.btnDelete.visible = !enabled && available;
					ref.btnAutomaticSync.updateMessage();
				}));

		ref.btnAutomaticSync = addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.cloud.automatic", CloudSync.isEnabled() ? (ClientConfig.INSTANCE.get(ClientConfig.AUTOMATIC_CLOUD_SYNC) ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED) : WildfireLocalization.OFF))
				.position(xPos, yPos + 20)
				.size(157, 20)
				.onPress(button -> {
					var newVal = !config.get(ClientConfig.AUTOMATIC_CLOUD_SYNC);
					config.set(ClientConfig.AUTOMATIC_CLOUD_SYNC, newVal);
					button.updateMessage();
				})
				.tooltip(Tooltip.create(Component.empty()
						.append(Component.translatable("wildfire_gender.cloud.automatic.tooltip.line1"))
						.append("\n\n")
						.append(Component.translatable("wildfire_gender.cloud.automatic.tooltip.line2"))))
				.active(CloudSync.isEnabled()));

		ref.btnSyncNow = addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.cloud.sync"))
				.position(xPos + 98, yPos + 42)
				.size(60, 15)
				.onPress(this::sync));
		ref.btnSyncNow.visible = CloudSync.isEnabled();

		ref.btnDelete = addButton(builder -> builder
				.message(() -> Component.translatable("wildfire_gender.cloud.delete").withStyle(ChatFormatting.RED))
				.position(xPos + 98, yPos + 42)
				.size(60, 15)
				.onPress(this::delete));
		ref.btnDelete.visible = !CloudSync.isEnabled();

		addButton(builder -> builder
				.message(() -> Component.literal("X"))
				.position(this.width / 2 + 73, yPos - 11)
				.size(9, 9)
				.onPress(button -> onClose())
				.narration(text -> GuiUtils.doneNarrationText()));

		/*this.addDrawableChild(btnHelp = new WildfireButton(this.width / 2 + 73 - 10, yPos - 11, 9, 9, Text.literal("?"),
				button -> {
					//client.setScreen(new WildfireCloudDetailsScreen(this, client.player.getUuid())); // Disabled for now. Not complete
					// BUTTON IS SUPPOSED TO DO NOTHING AT THE MOMENT
				}));*/
	}

	private void sync(Button button) {
		button.active = false;
		button.setMessage(Component.translatable("wildfire_gender.cloud.syncing"));
		CompletableFuture.runAsync(() -> {
			try {
				CloudSync.sync(Objects.requireNonNull(getPlayer())).join();
				button.setMessage(Component.translatable("wildfire_gender.cloud.syncing.success"));
			} catch(Exception e) {
				var actualException = e instanceof CompletionException ce ? ce.getCause() : e;
				if(actualException instanceof SyncingTooFrequentlyException) {
					WildfireGender.LOGGER.warn("Failed to sync settings as we've already synced too recently");
					SyncLog.add(WildfireLocalization.SYNC_LOG_SYNC_TOO_FREQUENTLY);
				} else {
					WildfireGender.LOGGER.error("Failed to sync settings", actualException);
				}
				button.setMessage(Component.translatable("wildfire_gender.cloud.syncing.fail"));
			}
		});
	}

	private void delete(Button widget) {
		widget.active = false;
		CompletableFuture.runAsync(() -> {
			try {
				CloudSync.deleteProfile(Objects.requireNonNull(getPlayer())).join();
				widget.setMessage(Component.translatable("wildfire_gender.cloud.deleted"));
			} catch(Exception e) {
				WildfireGender.LOGGER.error("Failed to delete cloud sync profile", e);
				widget.setMessage(Component.translatable("wildfire_gender.cloud.delete_failed"));
			}
		});
	}

	@Override
	public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(ctx);
		ctx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 172) / 2, (this.height - 124) / 2, 0, 0, 172, 144, 256, 256);
	}

	@Override
	public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		if(minecraft == null || minecraft.level == null) return;
		super.render(ctx, mouseX, mouseY, delta);

		int x = this.width / 2;
		int y = this.height / 2;
		y -= 47;

		GuiUtils.drawScrollableTextWithoutShadow(GuiUtils.Justify.LEFT, ctx, font, getTitle(),
				x - 79, y - 12, x - 79 + 141, y - 11 + 10, 4473924);
		GuiUtils.drawScrollableTextWithoutShadow(GuiUtils.Justify.LEFT, ctx, font, Component.translatable("wildfire_gender.cloud.status_log"),
				x - 79, y + 47, x - 79 + 95, y + 48 + 10, 4473924);

		for(int i = SyncLog.SYNC_LOG.size() - 1; i >= 0; i--) {
			int reverseIndex = SyncLog.SYNC_LOG.size() - 1 - i;
			var entry = SyncLog.SYNC_LOG.get(i);

			if(reverseIndex < 6) {
				int ey = y + 110 - (reverseIndex * 10);
				GuiUtils.drawScrollableTextWithoutShadow(GuiUtils.Justify.LEFT, ctx, font, entry.text(),
						x - 78, ey, x - 78 + 156, ey + 10, entry.color());
			}
		}
	}

	@Override
	public void onClose() {
		ClientConfig.INSTANCE.save();
		super.onClose();
	}
}
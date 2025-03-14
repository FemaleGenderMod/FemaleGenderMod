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

import com.wildfire.client.gui.WildfireButton;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.SyncLog;
import com.wildfire.main.cloud.SyncLog.Entry;
import com.wildfire.main.cloud.SyncingTooFrequentlyException;
import com.wildfire.main.config.GeneralClientConfig;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WildfireCloudSyncScreen extends BaseWildfireScreen {

    private static final ResourceLocation BACKGROUND = WildfireGender.rl("textures/gui/sync_bg_v2.png");

    private WildfireButton btnSyncNow, btnAutomaticSync;

    protected WildfireCloudSyncScreen(Screen parent, UUID uuid) {
        super(Component.translatable("wildfire_gender.cloud_settings"), parent, uuid);
    }

    @Override
    public void init() {
        int x = this.width / 2;
        int y = this.height / 2;
        int yPos = y - 47;
        int xPos = x - 156 / 2 - 1;

        addRenderableWidget(new WildfireButton(xPos, yPos, 157, 20, Component.translatable("wildfire_gender.cloud.status",
              CloudSync.isEnabled() ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED), button -> {
            boolean newValue = !GeneralClientConfig.INSTANCE.cloudSync.get();
            GeneralClientConfig.INSTANCE.cloudSync.set(newValue);
            GeneralClientConfig.INSTANCE.save();
            button.setMessage(Component.translatable("wildfire_gender.cloud.status", CloudSync.isEnabled() ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED));
            btnAutomaticSync.active = CloudSync.isEnabled();
            btnAutomaticSync.setMessage(Component.translatable("wildfire_gender.cloud.automatic", CloudSync.isEnabled() ? (GeneralClientConfig.INSTANCE.syncPlayerData.get() ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED) : WildfireLocalization.OFF));
            btnSyncNow.visible = newValue;
        }));

        btnAutomaticSync = addRenderableWidget(new WildfireButton(xPos, yPos + 20, 157, 20, Component.translatable("wildfire_gender.cloud.automatic",
              CloudSync.isEnabled() ? (GeneralClientConfig.INSTANCE.syncPlayerData.get() ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED) : WildfireLocalization.OFF), button -> {
            boolean newVal = !GeneralClientConfig.INSTANCE.syncPlayerData.get();
            GeneralClientConfig.INSTANCE.syncPlayerData.set(newVal);
            GeneralClientConfig.INSTANCE.save();
            button.setMessage(Component.translatable("wildfire_gender.cloud.automatic", newVal ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED));
        }));
        btnAutomaticSync.setTooltip(Tooltip.create(Component.empty()
              .append(Component.translatable("wildfire_gender.cloud.automatic.tooltip.line1"))
              .append("\n\n")
              .append(Component.translatable("wildfire_gender.cloud.automatic.tooltip.line2"))));
        btnAutomaticSync.active = CloudSync.isEnabled();

        btnSyncNow = addRenderableWidget(new WildfireButton(xPos + 98, yPos + 42, 60, 15, Component.translatable("wildfire_gender.cloud.sync"), this::sync));
        //btnSyncNow.setTooltip(Tooltip.of(Component.empty()
        //		.append(Component.literal("Sync Server data is cached for a minimum time of 30 minutes. If you do not see any changes please try to re-sync later."))));
        btnSyncNow.visible = GeneralClientConfig.INSTANCE.cloudSync.get();

        addRenderableWidget(new WildfireButton(this.width / 2 + 73, yPos - 11, 9, 9, Component.literal("X"),
              button -> onClose(), text -> AbstractWidget.wrapDefaultNarrationMessage(CommonComponents.GUI_DONE)));

		/*btnHelp = addRenderableWidget(new WildfireButton(this.width / 2 + 73 - 10, yPos - 11, 9, 9, Component.literal("?"),
				button -> {
					//client.setScreen(new WildfireCloudDetailsScreen(this, client.player.getUuid())); // Disabled for now. Not complete
					// BUTTON IS SUPPOSED TO DO NOTHING AT THE MOMENT
				}));*/

        super.init();
    }

    private void sync(Button button) {
        button.active = false;
        button.setMessage(Component.translatable("wildfire_gender.cloud.syncing"));
        CompletableFuture.runAsync(() -> {
            try {
                CloudSync.sync(Objects.requireNonNull(getPlayer())).join();
                button.setMessage(Component.translatable("wildfire_gender.cloud.syncing.success"));
            } catch (Exception e) {
                Throwable actualException = e instanceof CompletionException ce ? ce.getCause() : e;
                if (actualException instanceof SyncingTooFrequentlyException) {
                    WildfireGender.LOGGER.warn("Failed to sync settings as we've already synced too recently");
                    SyncLog.add(WildfireLocalization.SYNC_LOG_SYNC_TOO_FREQUENTLY);
                } else {
                    WildfireGender.LOGGER.error("Failed to sync settings", actualException);
                }
                button.setMessage(Component.translatable("wildfire_gender.cloud.syncing.fail"));
            }
        });
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(BACKGROUND, (this.width - 172) / 2, (this.height - 124) / 2, 0, 0, 172, 144, 256, 256);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2 - 47;

        //TODO - 1.21: Validate these are correct
        drawScrollingString(graphics, getTitle(), x - 79, y - 11, TextAlignment.LEFT, 0x444444, 141, 0, false);
        drawScrollingString(graphics, Component.translatable("wildfire_gender.cloud.status_log"), x - 79, y + 48, TextAlignment.LEFT, 0x444444,
              95, 0, false);
		/*GuiHelper.drawScrollableTextWithoutShadow(TextAlignment.LEFT, graphics, font, getTitle(),
				x - 79, y - 12, x - 79 + 141, y - 11 + 10, 0x444444);
		GuiHelper.drawScrollableTextWithoutShadow(TextAlignment.LEFT, graphics, font, Component.translatable("wildfire_gender.cloud.status_log"),
				x - 79, y + 47, x - 79 + 95, y + 48 + 10, 0x444444);*/

        int lineHeight = font().lineHeight;
        for (int i = SyncLog.SYNC_LOG.size() - 1; i >= 0; i--) {
            int reverseIndex = SyncLog.SYNC_LOG.size() - 1 - i;
            Entry entry = SyncLog.SYNC_LOG.get(i);

            if (reverseIndex < 6) {
                //Add an extra space between each element
                int ey = y + 110 - (lineHeight * reverseIndex + reverseIndex);
                //TODO - 1.21: Validate this is correct
                drawScrollingString(graphics, entry.text(), x - 78, ey, TextAlignment.LEFT, 0x444444, 156, 0, false);
				/*GuiHelper.drawScrollableTextWithoutShadow(TextAlignment.LEFT, graphics, font, entry.text(),
						x - 78, ey, x - 78 + 156, ey + 10, entry.color());*/
            }
        }
    }

    @Override
    public void onClose() {
        GeneralClientConfig.INSTANCE.save();
        super.onClose();
    }
}
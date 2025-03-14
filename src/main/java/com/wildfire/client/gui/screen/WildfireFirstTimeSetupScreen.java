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

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.client.WildfireGenderClient;
import com.wildfire.client.gui.GuiHelper;
import com.wildfire.client.gui.WildfireButton;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WildfireFirstTimeSetupScreen extends BaseWildfireScreen {

    //TODO: PROPER TRANSLATIONS

    private static final Component TITLE = Component.translatable("wildfire_gender.first_time_setup.title").withStyle(ChatFormatting.UNDERLINE);
    private static final Component DESCRIPTION = Component.translatable("wildfire_gender.first_time_setup.description");
    private static final Component NOTICE = Component.translatable("wildfire_gender.first_time_setup.notice");

    private static final Component ENABLE_CLOUD_SYNCING = Component.translatable("wildfire_gender.first_time_setup.enable").withStyle(ChatFormatting.GREEN);
    private static final Component DISABLE_CLOUD_SYNCING = Component.translatable("wildfire_gender.first_time_setup.disable").withStyle(ChatFormatting.RED);

    private static final ResourceLocation BACKGROUND = WildfireGender.rl("textures/gui/first_time_bg.png");

    private WildfireButton enableCloudSyncing, disableCloudSyncing;

    public WildfireFirstTimeSetupScreen(Screen parent, UUID uuid) {
        super(Component.translatable("wildfire_gender.cloud_settings"), parent, uuid);
    }

    @Override
    public void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        enableCloudSyncing = addRenderableWidget(new WildfireButton(x + 3, y + 74, 128, 20, ENABLE_CLOUD_SYNCING, button -> {
            //Enable both settings, they can always disable automatic later? TBD
            GeneralClientConfig.INSTANCE.cloudSync.set(true);
            GeneralClientConfig.INSTANCE.syncPlayerData.set(true);
            GeneralClientConfig.INSTANCE.firstTimeLoad.set(false);
            GeneralClientConfig.INSTANCE.save();

            button.active = false;
            button.setMessage(CommonComponents.ELLIPSIS);
            disableCloudSyncing.active = false;

            final WardrobeBrowserScreen nextScreen = new WardrobeBrowserScreen(null, minecraft.player.getUUID());
            doInitialSync().thenRun(() -> minecraft.execute(() -> minecraft.setScreen(nextScreen)));
        }));

        disableCloudSyncing = addRenderableWidget(new WildfireButton(x - 131, y + 74, 128, 20, DISABLE_CLOUD_SYNCING, button -> {
            GeneralClientConfig.INSTANCE.cloudSync.set(false);
            GeneralClientConfig.INSTANCE.syncPlayerData.set(false);
            GeneralClientConfig.INSTANCE.firstTimeLoad.set(false);
            GeneralClientConfig.INSTANCE.save();

            minecraft.setScreen(new WardrobeBrowserScreen(null, minecraft.player.getUUID()));
        }));

        super.init();
    }

    private CompletableFuture<Void> doInitialSync() {
        UUID clientUUID = minecraft.player.getUUID();
        return CompletableFuture.runAsync(() -> {
            PlayerConfig clientConfig = WildfireGender.getOrAddPlayerById(clientUUID);
            if (!clientConfig.hasLocalConfig()) {
                try {
                    // note that we wait for this to ensure that we don't have any inconsistencies with the synced
                    // data once we open the main menu
                    WildfireGenderClient.loadGenderInfo(clientUUID, false, true).join();
                } catch (CompletionException ignored) {
                    // loadGenderInfo should log any errors for us
                    return;
                } catch (Exception e) {
                    WildfireGender.LOGGER.error("Failed to perform initial sync from the cloud", e);
                    return;
                }
                PlayerConfig.saveGenderInfo(clientConfig);
                // don't immediately re-sync the data we just got back to the cloud
                clientConfig.needsCloudSync = false;
            } else {
                clientConfig.needsCloudSync = true;
            }
        });
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(BACKGROUND, (this.width - 274) / 2, (this.height - 200) / 2, 0, 0, 274, 200, 512, 512);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        PoseStack mStack = graphics.pose();

        int x = this.width / 2;
        int y = this.height / 2;

        drawCenteredText(graphics, TITLE, x, y - 24, 4210752);

        drawCenteredTextWrapped(graphics, Component.literal("Keira Emberlyn:").withStyle(ChatFormatting.LIGHT_PURPLE), x + 32, y - 10, (int) ((256 - 65)), 0xFFFFFF);

        //TODO: Vertical scroll bar for longer text?
        drawCenteredTextWrapped(graphics, DESCRIPTION, x + 32, y + 2, 256 - 65, 0xFFFFFF);

        mStack.pushPose();
        mStack.translate(x, y + 47, 0);
        mStack.scale(0.8f, 0.8f, 1);
        mStack.translate(-x, -y - 47, 0);
        drawCenteredTextWrapped(graphics, NOTICE, x, y + 68, (int) ((256 - 10) * 1.2f), 4210752);
        mStack.popPose();

        int keiraX = x - 133;
        int keiraY = y - 12;
        int keiraW = 60;
        int keiraH = (int) (keiraW * ((float) KEIRA_HEIGHT / KEIRA_WIDTH));

        graphics.blit(KEIRA_WAVE, keiraX, keiraY, 0, 0, keiraW, keiraH, KEIRA_WIDTH, KEIRA_HEIGHT, KEIRA_WIDTH, KEIRA_HEIGHT);

        /*mStack.pushPose();
        mStack.translate(keiraX + (keiraW / 2), keiraY + (keiraH / 2), 0);
        mStack.mulPose(new Quaternionf().rotateZ(-25 * Mth.DEG_TO_RAD));
        graphics.blit(KEIRA_LOOK, -keiraW / 2, -keiraH / 2, 0, 0, keiraW, keiraH, KEIRA_WIDTH, KEIRA_HEIGHT, KEIRA_WIDTH, KEIRA_HEIGHT);
        mStack.popPose();*/
    }

    @Override
    public void removed() {
        GeneralClientConfig.INSTANCE.save();
    }
}
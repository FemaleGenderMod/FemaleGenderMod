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
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.GenderConfigs;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.config.ClientConfigHolder;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.entitydata.PlayerConfigHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WildfireFirstTimeSetupScreen extends BaseWildfireScreen {

    private static final Component DESCRIPTION = WildfireLang.FIRST_TIME_DESCRIPTION.translate();
    private static final Component NOTICE = WildfireLang.FIRST_TIME_NOTICE.translate();
    private static final int SCREEN_WIDTH = 274;

    //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor' {
    private static final Component ENABLE_CLOUD_SYNCING = WildfireLang.FIRST_TIME_ENABLE.translateColored(TextColor.GREEN);
    private static final Component DISABLE_CLOUD_SYNCING = WildfireLang.FIRST_TIME_DISABLE.translateColored(TextColor.RED);
    //~}

    private static final Identifier BACKGROUND = WildfireGender.id("textures/gui/first_time_bg.png");

    private static final UUID keiraUUID = UUID.fromString("372271ab-28f2-44bd-b585-95f43e010c22");

    private final Supplier<FakeGUIPlayer> fakeKeira = Suppliers.memoize(() -> new FakeGUIPlayer("KeiaraFGM", keiraUUID, GenderConfigs.DEFAULT_FEMALE));

    public WildfireFirstTimeSetupScreen(@Nullable Screen parent, UUID uuid) {
        super(WildfireLang.FIRST_TIME_TITLE.translate().withStyle(style -> style.withUnderlined(true)), parent, uuid);
    }

    @Override
    public void init() {
        super.init();
        int x = this.width / 2;
        int y = this.height / 2;

        final var config = ClientConfigHolder.INSTANCE.config();
        final var ref = new Object() {
            @UnknownNullability
            WildfireButton no;
        };

        addButton(builder -> builder
                .message(() -> ENABLE_CLOUD_SYNCING)
                .position(x + 3, y + 74)
                .size(128, 20)
                .onPress(button -> {
                    config.cloudSyncEnabled = true;
                    config.automaticCloudSync = true;
                    config.firstTimeLoad = false;

                    button.active = false;
                    button.setMessage(CommonComponents.ELLIPSIS);
                    ref.no.setActive(false);

                    final var nextScreen = new WardrobeBrowserScreen(null, playerUUID);
                    //~ if >=26.2 'setScreen' -> 'gui.setScreen'
                    doInitialSync().thenRun(() -> minecraft.execute(() -> minecraft.gui.setScreen(nextScreen)));
                })
                .tooltip(Tooltip.create(WildfireLang.FIRST_TIME_ENABLE_TOOLTIP.line(1)
                        .append("\n\n")
                        .append(WildfireLang.FIRST_TIME_ENABLE_TOOLTIP.line(2)))));

        ref.no = addButton(builder -> builder
                .message(() -> DISABLE_CLOUD_SYNCING)
                .position(x - 131, y + 74)
                .size(128, 20)
                .onPress(_ -> {
                    config.cloudSyncEnabled = false;
                    config.automaticCloudSync = false;
                    config.firstTimeLoad = false;

                    //~ if >=26.2 'minecraft.setScreen' -> 'minecraft.gui.setScreen'
                    minecraft.gui.setScreen(new WardrobeBrowserScreen(null, playerUUID));
                }));
    }

    private CompletableFuture<Void> doInitialSync() {
        var client = Objects.requireNonNull(this.minecraft);
        assert client.player != null;
        var clientUUID = client.player.getUUID();

        WildfireGender.CACHE.asMap().values()
            .removeIf(config -> config.syncStatus == PlayerConfigHolder.SyncStatus.UNKNOWN);

        return CompletableFuture.runAsync(() -> {
            var clientConfig = WildfireGender.getOrAddPlayerById(clientUUID);
            if(!clientConfig.hasLocalConfig()) {
                try {
                    // note that we wait for this to ensure that we don't have any inconsistencies with the synced
                    // data once we open the main menu
                    WildfireGenderClient.loadGenderInfo(clientUUID, false, true).join();
                } catch(CompletionException _) {
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractTransparentBackground(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - SCREEN_WIDTH) / 2, (this.height - 200) / 2, 0, 0, SCREEN_WIDTH, 200, 512, 512);
    }

    @Override
    public void tick() {
        this.fakeKeira.get().tick();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;

        drawScrollingString(graphics, getTitle(), x - (SCREEN_WIDTH / 2), y - 24, TextAlignment.CENTER, CommonColors.DARK_GRAY, SCREEN_WIDTH, 6, false);

        //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor'
        drawScrollingString(graphics, WildfireLang.KEIRA.translateColored(TextColor.LIGHT_PURPLE), x - 63, y - 10, TextAlignment.CENTER, CommonColors.WHITE, 191, 0, false);

        //TODO: Vertical scroll bar for longer text?
        drawCenteredTextWrapped(graphics, DESCRIPTION, x + 32, y + 2, 256 - 65, CommonColors.WHITE);

        drawScaledScrollingString(graphics, NOTICE, x - (SCREEN_WIDTH / 2), y + 63, TextAlignment.CENTER, CommonColors.DARK_GRAY, SCREEN_WIDTH, 6, false, 0.8F);

        var fakeKeira = this.fakeKeira.get().getEntity();
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x - 132, y - 13, x - 75, y + 60, 50, ENTITY_SCALE + 0.4f, mouseX, mouseY, fakeKeira);
    }

    @Override
    public void removed() {
        ClientConfigHolder.INSTANCE.save();
    }
}

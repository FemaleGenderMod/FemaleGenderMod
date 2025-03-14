package com.wildfire.client.gui;

import com.wildfire.client.WildfireGenderClient;
import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.main.config.GeneralClientConfig;
import java.util.Objects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.jetbrains.annotations.NotNull;

public class SyncedPlayersLayer implements LayeredDraw.Layer {

    public static final SyncedPlayersLayer INSTANCE = new SyncedPlayersLayer();

    private SyncedPlayersLayer() {
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Font textRenderer = Objects.requireNonNull(Minecraft.getInstance().font, "textRenderer");
        if (Minecraft.getInstance().screen instanceof WardrobeBrowserScreen) {
            return;
        }

        /*if (Minecraft.getInstance().player != null) {
            PlayerConfig pCfg = WildfireGender.getPlayerById(Minecraft.getInstance().player.getUUID());
            if (pCfg != null) {
                graphics.drawString(textRenderer, "Physics Debug", 5, 5, 0xFFFFFF, true);
                graphics.drawString(textRenderer, "Position: " + pCfg.getLeftBreastPhysics().getPositionX() + "," + pCfg.getLeftBreastPhysics().getPositionY(), 5, 15, 0xFFFFFF, true);
                graphics.drawString(textRenderer, "Breast Size: " + pCfg.getLeftBreastPhysics().getBreastSize(delta.getGameTimeDeltaPartialTick(false)), 5, 35, 0xFFFFFF, true);
            }
        }*/
        boolean shouldShow = switch (GeneralClientConfig.INSTANCE.alwaysShowList.get()) {
            case MOD_UI_ONLY -> false;
            case TAB_LIST_OPEN -> Minecraft.getInstance().options.keyPlayerList.isDown();
            case ALWAYS -> true;
        };
        if (shouldShow) {
            GuiHelper.drawSyncedPlayers(graphics, textRenderer, WildfireGenderClient.collectPlayerEntries());
        }
    }
}
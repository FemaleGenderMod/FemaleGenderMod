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
import com.wildfire.gui.WildfireSlider;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class BaseWildfireScreen extends Screen {

    protected final UUID playerUUID;
    protected final @Nullable Screen parent;

    protected BaseWildfireScreen(Component title, @Nullable Screen parent, UUID uuid) {
        super(title);
        this.parent = parent;
        this.playerUUID = uuid;
    }

    protected WildfireButton addButton(Consumer<WildfireButton.Builder> builder) {
        var buttonBuilder = new WildfireButton.Builder();
        builder.accept(buttonBuilder);
        return addRenderableWidget(buttonBuilder.build());
    }

    protected WildfireSlider addSlider(Consumer<WildfireSlider.Builder> builder) {
        var sliderBuilder = new WildfireSlider.Builder();
        sliderBuilder.save(_ -> Objects.requireNonNull(getPlayer(), "getPlayer()").save());
        builder.accept(sliderBuilder);
        return addRenderableWidget(sliderBuilder.build());
    }

    public @Nullable PlayerConfig getPlayer() {
        return WildfireGender.getPlayerById(this.playerUUID);
    }

    protected void renderPlayerInFrame(GuiGraphicsExtractor graphics, int xP, int yP, int mouseX, int mouseY) {
        var player = minecraft.player;
        if(player == null) return;
        // This sucks. In order to position the player properly, we need to trick the player renderer into
        // thinking the area the player should be rendered is much taller than it actually is.
        graphics.enableScissor(xP - 38, yP - 79, xP + 38, yP + 9);
        GuiUtils.drawEntityOnScreen(graphics, xP - 38, yP - 79, xP + 38, yP + 69, 70, mouseX, mouseY + 35, player);
        graphics.disableScissor();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        //~ if >=26.2 'minecraft.setScreen' -> 'minecraft.gui.setScreen'
        minecraft.gui.setScreen(parent);
    }
}

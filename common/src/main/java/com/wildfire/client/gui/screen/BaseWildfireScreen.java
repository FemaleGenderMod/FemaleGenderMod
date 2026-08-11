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

import com.wildfire.client.gui.IFancyFontRenderer;
import com.wildfire.client.gui.WildfireButton;
import com.wildfire.client.gui.WildfireSlider;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.entitydata.PlayerConfigHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/// @apiNote Only use this on the client side
public abstract class BaseWildfireScreen extends Screen implements IFancyFontRenderer {

    private static final float ENTITY_SCALE = 0.0625F;

    protected final UUID playerUUID;
    protected final @Nullable Screen parent;

    private long lastMSInitialized;

    protected BaseWildfireScreen(Component title, @Nullable Screen parent, UUID uuid) {
        super(title);
        this.parent = parent;
        this.playerUUID = uuid;
    }

    protected WildfireButton addButton(Consumer<WildfireButton.Builder> builder) {
        var buttonBuilder = new WildfireButton.Builder();
        builder.accept(buttonBuilder);
        return addRenderableWidget(buttonBuilder.build(lastMSInitialized));
    }

    protected WildfireSlider addSlider(Consumer<WildfireSlider.Builder> builder) {
        var sliderBuilder = new WildfireSlider.Builder();
        sliderBuilder.save(_ -> Objects.requireNonNull(getPlayer(), "getPlayer()").save());
        builder.accept(sliderBuilder);
        return addRenderableWidget(sliderBuilder.build(lastMSInitialized));
    }

    public @Nullable PlayerConfigHolder getPlayer() {
        return WildfireGender.getPlayerById(this.playerUUID);
    }

    protected void renderPlayerInFrame(GuiGraphicsExtractor graphics, int xP, int yP, int mouseX, int mouseY) {
        var player = minecraft.player;
        if(player == null) return;
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, xP - 38, yP - 79, xP + 38, yP + 9, 70, getEntityScale(player, 0.4F), mouseX, mouseY, player);
    }

    @Override
    protected void init() {
        super.init();
        lastMSInitialized = Util.getMillis();
    }

    @Override
    public long getTimeOpened() {
        return lastMSInitialized;
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

    protected static float getEntityScale(LivingEntity entity, float shift) {
        return getEntityScale(entity, shift, true);
    }

    protected static float getEntityScale(LivingEntity entity, float shift, boolean adjustByHeight) {
        float scale = ENTITY_SCALE;
        //Note: While we could reimplement the default values that get set for state.isUpsideDown,
        // it is easier and more mod compatible to just extract the render state an extra time
        if (InventoryScreen.extractRenderState(entity) instanceof LivingEntityRenderState state && state.isUpsideDown) {
            //Invert the scale
            scale = -scale;
            if (adjustByHeight) {
                state.boundingBoxWidth /= state.scale;
                state.boundingBoxHeight /= state.scale;
                //Negate the scale and also undo the transformation that InventoryScreen#extractEntityInInventoryFollowsMouse does
                scale -= state.boundingBoxHeight / 2;
            }
        }
        return scale + shift;
    }
}

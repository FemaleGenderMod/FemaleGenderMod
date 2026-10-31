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
import com.wildfire.common.entities.avatars.AbstractAvatarConfigHolder;
import com.wildfire.common.entities.players.PlayerConfigHolder;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/// @apiNote Only use this on the client side
public abstract class BaseWildfireScreen extends Screen implements IFancyFontRenderer {

    private static final float ENTITY_SCALE = 0.0625F;

    protected final AbstractAvatarConfigHolder config;
    protected final @Nullable Screen parent;

    private long lastMSInitialized;

    protected BaseWildfireScreen(Component title, @Nullable Screen parent, AbstractAvatarConfigHolder holder) {
        super(title);
        this.parent = parent;
        this.config = holder;
    }

    protected WildfireButton addButton(Consumer<WildfireButton.Builder> builder) {
        var buttonBuilder = new WildfireButton.Builder();
        builder.accept(buttonBuilder);
        return addRenderableWidget(buttonBuilder.build(lastMSInitialized));
    }

    protected WildfireSlider addSlider(Consumer<WildfireSlider.Builder> builder) {
        var sliderBuilder = new WildfireSlider.Builder();
        sliderBuilder.save(ignored -> save());
        builder.accept(sliderBuilder);
        return addRenderableWidget(sliderBuilder.build(lastMSInitialized));
    }

    protected @Nullable LivingEntity getEntity() {
        ClientLevel level = minecraft.level;
        if(level == null) {
            return null;
        }

        LivingEntity player = level.getPlayerByUUID(config.uuid);
        if(player != null) {
            return player;
        }
        //Note: 1.21.1 has no Level#getEntity(UUID), so fall back to scanning rendered entities
        // (also covers non-player avatars such as armor stands)
        for(Entity entity : level.entitiesForRendering()) {
            if(entity instanceof LivingEntity living && config.uuid.equals(entity.getUUID())) {
                return living;
            }
        }
        return null;
    }

    protected void save() {
        if(config instanceof PlayerConfigHolder playerConfig) {
            LocalPlayer self = minecraft.player;
            if(self == null || playerConfig.uuid != self.getUUID()) {
                return;
            }
            playerConfig.save();
        }
    }

    protected void renderPlayerInFrame(GuiGraphics graphics, int xP, int yP, int mouseX, int mouseY) {
        LivingEntity entity = getEntity();
        if(entity != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, xP - 38, yP - 79, xP + 38, yP + 9, 70, getEntityScale(entity, 0.4F), mouseX, mouseY, entity);
        }
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
        minecraft.setScreen(parent);
    }

    protected static float getEntityScale(LivingEntity entity, float shift) {
        return getEntityScale(entity, shift, true);
    }

    protected static float getEntityScale(LivingEntity entity, float shift, boolean adjustByHeight) {
        float scale = ENTITY_SCALE;
        //Note: While we could reimplement the default values that vanilla uses for rendering entities upside down,
        // it is easier and more mod compatible to just check the name tag directly
        if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
            //Invert the scale
            scale = -scale;
            if (adjustByHeight) {
                //Negate the scale and also undo the transformation that the upside-down rendering does,
                // approximated via half of the entity height
                scale -= entity.getBbHeight() / 2;
            }
        }
        return scale + shift;
    }
}

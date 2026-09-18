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
import com.wildfire.common.entities.avatars.MannequinConfigHolder;
import com.wildfire.common.entities.players.PlayerConfigHolder;
import com.wildfire.common.networking.WildfireSync;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
        sliderBuilder.save(_ -> save());
        builder.accept(sliderBuilder);
        return addRenderableWidget(sliderBuilder.build(lastMSInitialized));
    }

    // TODO does this still serve a purpose?
    public AbstractAvatarConfigHolder getPlayer() {
        return config;
    }

    protected @Nullable LivingEntity getEntity() {
        Level level = minecraft.level;
        if(level == null) {
            return null;
        }

        Entity entity = level.getEntity(config.uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    protected void save() {
        if(config instanceof PlayerConfigHolder playerConfig) {
            LocalPlayer self = minecraft.player;
            if(self == null || playerConfig.uuid != self.getUUID()) {
                return;
            }
            playerConfig.save();
        } else if(config instanceof MannequinConfigHolder mannequinConfig) {
            var connection = Objects.requireNonNull(Minecraft.getInstance().getConnection(), "Connection is null while editing a mannequin?!");
            WildfireSync.sendToServer(connection.getConnection(), mannequinConfig);
        }
    }

    protected void renderPlayerInFrame(GuiGraphicsExtractor graphics, int xP, int yP, int mouseX, int mouseY) {
        LivingEntity entity = getEntity();
        if(entity != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, xP - 38, yP - 79, xP + 38, yP + 9, 70, getEntityScale(entity, 0.4F), mouseX, mouseY, entity);
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

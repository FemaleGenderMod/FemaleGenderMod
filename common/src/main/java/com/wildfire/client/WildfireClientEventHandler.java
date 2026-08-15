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

package com.wildfire.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.client.gui.SyncedPlayerList;
import com.wildfire.client.gui.WildfireToast;
import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireHelper;
import com.wildfire.common.WildfireLang;
import com.wildfire.client.cloud.CloudSync;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.entitydata.EntityConfig;
import com.wildfire.common.entitydata.EntityConfigHolder;
import com.wildfire.common.entitydata.PlayerConfigHolder;
import com.wildfire.common.networking.WildfireSync;
import com.wildfire.client.render.GenderArmorLayer;
import com.wildfire.client.render.GenderLayer;
import com.wildfire.client.render.HolidayFeaturesRenderer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/// @apiNote Only use this on the client side
public final class WildfireClientEventHandler {

    private WildfireClientEventHandler() {
        throw new UnsupportedOperationException();
    }

    private static int timer = 0;

    static void onPlayerNametag(AvatarRenderState state, SubmitNodeCollector nodeCollector, PoseStack matrixStack, CameraRenderState camera) {
        var genderRenderState = ClientHelper.INSTANCE.getRenderState(state);
        if (genderRenderState == null) {
            return;
        }

        Component nametag = genderRenderState.nametag;
        if (nametag == null) {
            return;
        }

        matrixStack.pushPose();
        //TODO - both: Try to base this off of EntityRenderState#nameTagAttachment which should help position it appropriately
        float translationAmt = switch (state.pose) {
            case CROUCHING -> 0.8f;
            case SLEEPING -> 0.125f;
            case SWIMMING, FALL_FLYING -> 0.3f;
            case SITTING -> 0.275f; //not tested; sitting on a pig doesn't work apparently
            default -> 0.95f;
        };
        matrixStack.translate(0f, translationAmt, 0f);
        matrixStack.scale(0.5f, 0.5f, 0.5f);

        nodeCollector.submitNameTag(
            matrixStack,
            state.nameTagAttachment,
            state.showExtraEars ? -10 : 0,
            nametag,
            !state.isDiscrete,
            state.lightCoords,
            //? if <26.2
            //state.distanceToCameraSq,
            camera
        );

        matrixStack.popPose();
        // shift the rest of the name tag up a little bit
        matrixStack.translate(0f, 2.15F * 1.15F * 0.025F, 0f);
    }

    static void renderTooltip(ItemStack item, Consumer<Component> tooltipAppender, @Nullable Player player) {
        if (player == null || !ClientConfig.config().armorStat().get()) {
            return;
        }
        if (ClientConfig.config().overrideArmorPhysics().get()) {
            return;
        }
        var equippableComponent = item.get(DataComponents.EQUIPPABLE);
        if (equippableComponent == null || equippableComponent.slot() != EquipmentSlot.CHEST) {
            return;
        }

        var playerConfig = WildfireGender.getPlayerById(player.getUUID());
        if (playerConfig == null || !playerConfig.gender().get().canHaveBreasts()) {
            return;
        }

        var config = WildfireClientHelper.getArmorConfig(item);
        // don't show a +0 tooltip on items that don't interact with physics (e.g. Elytra)
        if (!config.coversBreasts() || config.physicsResistance() == 0f) {
            return;
        }

        String formatted = WildfireHelper.toFormattedPercent(config.physicsResistance()) + "%";
        //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor'
        tooltipAppender.accept(WildfireLang.ARMOR_TOOLTIP.translateColored(TextColor.LIGHT_PURPLE, formatted));
    }

    static void renderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        var client = Minecraft.getInstance();
        //~ if >=26.2 'client.screen' -> 'client.gui.screen()'
        if (client.gui.screen() instanceof WardrobeBrowserScreen) {
            SyncedPlayerList.resetTimer();
            return;
        }

        if (ClientConfig.config().playerListMode().get().isVisible()) {
            SyncedPlayerList.drawSyncedPlayers(context);
        } else {
            SyncedPlayerList.resetTimer();
        }
    }

    /// Remove (non-player) entities from the client cache when they're unloaded
    static void onEntityUnload(Entity entity, Level world) {
        // note that we don't attempt to unload players; they're instead only ever unloaded once we leave a world,
        // or once they disconnect
        EntityConfigHolder.CACHE.invalidate(entity.getUUID());
    }

    /// Perform various actions that should happen once per client tick, such as syncing client player settings to the server.
    static void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }
        timer++;

        if (timer % 5 == 0) {
            PlayerConfigHolder clientConfig = WildfireGender.getPlayerById(client.player.getUUID());
            // Only attempt to sync if the server will accept the packet, and only once every 5 ticks, or around 4 times a second
            if (client.isMultiplayerServer() && clientConfig != null) {
                // sendToServer will only actually send a packet if any changes have been made that need to be synced, or if we haven't synced before.
                WildfireSync.sendToServer(client.player.connection.getConnection(), clientConfig);
            }
            if (timer % 40 == 0) {//All timers that are divisible by 40 will be divisible by 5, so we may as well put it within the outer if statement
                CloudSync.sendNextQueueBatch();
                if (clientConfig != null) {
                    clientConfig.attemptCloudSync();
                }
            }
        }

        //~ if >=26.2 'client.screen' -> 'client.gui.screen()' {
        if (WildfireKeyBindings.INSTANCE.toggleKey().consumeClick() && client.gui.screen() == null) {
            ClientConfig.config().disableRendering().update(ConfigValue.TOGGLE);
            ClientConfig.INSTANCE.save();
        }
        if (WildfireKeyBindings.INSTANCE.configKey().consumeClick() && client.gui.screen() == null) {
            WardrobeBrowserScreen.open(client, client.player);
        }
        //~}
    }

    /// Clears all caches when the client player disconnects from a server/closes a singleplayer world
    static void clientDisconnect() {
        WildfireGender.CACHE.invalidateAll();
        EntityConfigHolder.CACHE.invalidateAll();
    }

    static void clientJoin(Minecraft client) {
        if (client.player != null && ClientConfig.config().showToast().get()) {
            var button = WildfireKeyBindings.INSTANCE.configKey().getTranslatedKeyMessage();
            //~ if >=26.2 'client.getToastManager()' -> 'client.gui.toastManager()'
            ToastManager toastManager = client.gui.toastManager();
            toastManager.addToast(new WildfireToast(Minecraft.getInstance().font, WildfireLang.PLAYER_LIST_TITLE.translate(), WildfireLang.TOAST_GET_STARTED.translate(button)));
        }
    }

    /// Tick breast physics on entity tick
    static void onEntityTick(LivingEntity entity) {
        if (EntityConfig.isSupportedEntity(entity)) {
            EntityConfigHolder<?> cfg = EntityConfigHolder.getEntity(entity);
            if (entity instanceof ArmorStand) {
                cfg.readFromStack(entity.getItemBySlot(EquipmentSlot.CHEST));
            }
            cfg.breastPhysics().tick(entity);
        }
    }

    static void addAvatarRenderLayers(@Nullable AvatarRenderer<?> avatarRenderer, EquipmentLayerRenderer equipmentRenderer,
        BiConsumer<AvatarRenderer<?>, RenderLayer<AvatarRenderState, PlayerModel>> registration) {
        if (avatarRenderer != null) {
            registration.accept(avatarRenderer, new GenderLayer<>(avatarRenderer));
            registration.accept(avatarRenderer, new GenderArmorLayer<>(avatarRenderer, equipmentRenderer));
            registration.accept(avatarRenderer, new HolidayFeaturesRenderer(avatarRenderer));
        }
    }

    static void addArmorStandRenderLayers(ArmorStandRenderer armorStandRenderer, EquipmentLayerRenderer equipmentRenderer,
        BiConsumer<ArmorStandRenderer, RenderLayer<ArmorStandRenderState, ArmorStandArmorModel>> registration) {
        registration.accept(armorStandRenderer, new GenderArmorLayer<>(armorStandRenderer, equipmentRenderer));
    }
}

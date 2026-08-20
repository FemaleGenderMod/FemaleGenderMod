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
import net.minecraft.client.renderer.entity.EntityRenderer;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/// @apiNote Only use this on the client side
public final class WildfireClientEventHandler {

    private WildfireClientEventHandler() {
        throw new UnsupportedOperationException();
    }

    private static int timer = 0;

    public static void onPlayerNametag(AvatarRenderState state, SubmitNodeCollector nodeCollector, PoseStack matrixStack, CameraRenderState camera) {
        var genderRenderState = ClientHelper.INSTANCE.getRenderState(state);
        if (genderRenderState != null && genderRenderState.nametag != null && state.nameTagAttachment != null) {
            matrixStack.pushPose();
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            nodeCollector.submitNameTag(
                matrixStack,
                //Shift the text positioning upwards slightly
                new Vec3(state.nameTagAttachment.x, (state.nameTagAttachment.y + 0.05) * 2, state.nameTagAttachment.z),
                state.showExtraEars ? -10 : 0,
                genderRenderState.nametag,
                !state.isDiscrete,
                state.lightCoords,
                //? if <26.2
                //state.distanceToCameraSq,
                camera
            );
            matrixStack.popPose();
            // shift the rest of the name tag up a little bit. This is akin to what vanilla does when rendering the score, but with a different multiplier (in this case 2.15)
            matrixStack.translate(0f, 2.15F * 1.15F * EntityRenderer.NAMETAG_SCALE, 0f);
        }
    }

    public static void renderTooltip(ItemStack item, Consumer<Component> tooltipAppender, @Nullable Player player) {
        if (player == null || !ClientConfig.config().armorStat().get() || ClientConfig.config().overrides().armorPhysics().get()) {
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

    public static void renderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
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
    public static void onEntityUnload(Entity entity, Level world) {
        // note that we don't attempt to unload players; they're instead only ever unloaded once we leave a world,
        // or once they disconnect
        EntityConfigHolder.CACHE.invalidate(entity.getUUID());
    }

    /// Perform various actions that should happen once per client tick, such as syncing client player settings to the server.
    public static void onClientTick(Minecraft client) {
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
        if (WildfireKeyBindings.INSTANCE.toggleKey().consumeClick() && client.gui.screen() == null &&
            ClientConfig.config().overrides().disableRendering().update(ConfigValue.TOGGLE)) {//Update should always succeed, but validate it just in case
            ClientConfig.INSTANCE.save();
        }
        if (WildfireKeyBindings.INSTANCE.configKey().consumeClick() && client.gui.screen() == null) {
            WardrobeBrowserScreen.open(client, client.player);
        }
        //~}
    }

    /// Clears all caches when the client player disconnects from a server/closes a singleplayer world
    public static void clientDisconnect() {
        WildfireGender.CACHE.invalidateAll();
        EntityConfigHolder.CACHE.invalidateAll();
    }

    public static void clientJoin(Minecraft client) {
        if (client.player != null && ClientConfig.config().showToast().get()) {
            var button = WildfireKeyBindings.INSTANCE.configKey().getTranslatedKeyMessage();
            //~ if >=26.2 'client.getToastManager()' -> 'client.gui.toastManager()'
            ToastManager toastManager = client.gui.toastManager();
            toastManager.addToast(new WildfireToast(Minecraft.getInstance().font, WildfireLang.PLAYER_LIST_TITLE.translate(), WildfireLang.TOAST_GET_STARTED.translate(button)));
        }
    }

    /// Tick breast physics on entity tick
    public static void onEntityTick(LivingEntity entity) {
        //Note: We don't need to check if the entity is frozen as far as /tick is concerned,
        // as the tick event shouldn't happen in the first place if the entity is frozen
        if (EntityConfig.isSupportedEntity(entity)) {
            EntityConfigHolder<?> cfg = EntityConfigHolder.getEntity(entity);
            if (entity instanceof ArmorStand) {
                cfg.readFromStack(entity.getItemBySlot(EquipmentSlot.CHEST));
            }
            cfg.breastPhysics().tick(entity);
        }
    }

    public static void addAvatarRenderLayers(@Nullable AvatarRenderer<?> avatarRenderer, EquipmentLayerRenderer equipmentRenderer,
        BiConsumer<AvatarRenderer<?>, RenderLayer<AvatarRenderState, PlayerModel>> registration) {
        if (avatarRenderer != null) {
            registration.accept(avatarRenderer, new GenderLayer<>(avatarRenderer));
            registration.accept(avatarRenderer, new GenderArmorLayer<>(avatarRenderer, equipmentRenderer));
            registration.accept(avatarRenderer, new HolidayFeaturesRenderer(avatarRenderer));
        }
    }

    public static void addArmorStandRenderLayers(ArmorStandRenderer armorStandRenderer, EquipmentLayerRenderer equipmentRenderer,
        BiConsumer<ArmorStandRenderer, RenderLayer<ArmorStandRenderState, ArmorStandArmorModel>> registration) {
        registration.accept(armorStandRenderer, new GenderArmorLayer<>(armorStandRenderer, equipmentRenderer));
    }
}

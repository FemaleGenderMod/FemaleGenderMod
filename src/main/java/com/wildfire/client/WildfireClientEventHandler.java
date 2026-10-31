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

import com.wildfire.api.client.WildfireClientAPI;
import com.wildfire.client.cloud.CloudSync;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.client.gui.SyncedPlayerList;
import com.wildfire.client.gui.FakeGUIPlayer;
import com.wildfire.client.gui.WildfireToast;
import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.client.render.GenderArmorLayer;
import com.wildfire.client.render.GenderLayer;
import com.wildfire.common.WildfireHelper;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.entities.EntityConfigHolder;
import com.wildfire.common.entities.armorstands.ArmorStandConfigHolder;
import com.wildfire.common.entities.players.PlayerConfigHolder;
import com.wildfire.common.networking.WildfireSync;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.jspecify.annotations.Nullable;
import net.minecraft.ChatFormatting;

/// @apiNote Only use this on the client side
public final class WildfireClientEventHandler {

    private WildfireClientEventHandler() {
        throw new UnsupportedOperationException();
    }

    private static int timer = 0;

    public static void renderTooltip(ItemStack item, Consumer<Component> tooltipAppender, @Nullable Player player) {
        if (player == null || !ClientConfig.config().armorStat().get() || ClientConfig.config().overrides().armorPhysics().get()) {
            return;
        }
        if (!(item.getItem() instanceof ArmorItem armorItem) || armorItem.getEquipmentSlot() != EquipmentSlot.CHEST) {
            return;
        }

        //Note: Unlike 26.1 (where the config lives on a data attachment and is always present), the cache
        // may have expired here, so we must use getOrCreate to (re)load the config from disk/cloud
        var playerConfig = WildfireClientAPI.players().getOrCreate(player);
        if (playerConfig == null || !playerConfig.gender().get().canHaveBreasts()) {
            return;
        }

        var config = WildfireClientHelper.getArmorConfig(item);
        // don't show a +0 tooltip on items that don't interact with physics (e.g. Elytra)
        if (!config.coversBreasts() || config.physicsResistance() == 0f) {
            return;
        }

        String formatted = WildfireHelper.toFormattedPercent(config.physicsResistance()) + "%";
        tooltipAppender.accept(WildfireLang.ARMOR_TOOLTIP.translateColored(ChatFormatting.LIGHT_PURPLE, formatted));
    }

    public static void renderHud(GuiGraphics context, @SuppressWarnings("unused") DeltaTracker tickCounter) {
        var client = Minecraft.getInstance();
        if (client.screen instanceof WardrobeBrowserScreen) {
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
    public static void onEntityUnload(Entity entity, @SuppressWarnings("unused") Level world) {
        // players are intentionally not unloaded outside of disconnecting or expiring as their data relies
        // on being loaded through external means (e.g. a sync packet, cloud sync, or a file on disk),
        // which we may not always be able to rely on being resent (especially in the case of third-party servers),
        // or would otherwise like to avoid loading from again where possible (in the case of disk reads)
        if(entity instanceof ArmorStand armorStand) {
            // armor stands however can safely reload their data later if they're loaded again without relying
            // on any external means, as their data is stored in their equipped chestplate item
            WildfireClientAPI.armorStands().invalidate(armorStand);
        }
    }

    /// Perform various actions that should happen once per client tick, such as syncing client player settings to the server.
    public static void onClientTick(Minecraft client) {
        if(client.level == null || client.player == null) {
            return;
        }
        timer++;

        if(timer % 5 == 0) {
            PlayerConfigHolder clientConfig = WildfireClientAPI.players().get(client.player);
            // Only attempt to sync if the server will accept the packet, and only once every 5 ticks, or around 4 times a second
            if(clientConfig != null && clientConfig.needsSync) {
                if(WildfireSync.sendToServer(client.player.connection.getConnection(), clientConfig)) {
                    clientConfig.needsSync = false;
                }
            }

            if(timer % 40 == 0) {
                CloudSync.sendNextQueueBatch();
                if(clientConfig != null) {
                    clientConfig.attemptCloudSync();
                }
            }
        }

        if(WildfireKeyBindings.INSTANCE.toggleKey().consumeClick() && client.screen == null) {
            // Update should always succeed, but validate it just in case
            if(ClientConfig.config().overrides().disableRendering().update(ConfigValue.TOGGLE)) {
                ClientConfig.INSTANCE.save();
            }
        }
        if(WildfireKeyBindings.INSTANCE.configKey().consumeClick() && client.screen == null) {
            WardrobeBrowserScreen.open(client, client.player);
        }
    }

    /// Clears all caches when the client player disconnects from a server/closes a singleplayer world
    public static void clientDisconnect() {
        WildfireClientAPI.players().invalidateAll();
        WildfireClientAPI.armorStands().invalidateAll();
    }

    public static void clientJoin(Minecraft client) {
        if (client.player != null && ClientConfig.config().showToast().get()) {
            var button = WildfireKeyBindings.INSTANCE.configKey().getTranslatedKeyMessage();
            client.getToasts().addToast(new WildfireToast(Minecraft.getInstance().font, WildfireLang.MOD_NAME.translate(), WildfireLang.TOAST_GET_STARTED.translate(button)));
        }
    }

    /// Tick breast physics on entity tick
    public static void onEntityTick(LivingEntity entity) {
        //Note: We don't need to check if the entity is frozen as far as /tick is concerned,
        // as the tick event shouldn't happen in the first place if the entity is frozen
        EntityConfigHolder<?> cfg = WildfireClientAPI.getConfig(entity);
        if(cfg == null) {
            return;
        }
        if(cfg instanceof ArmorStandConfigHolder armorStandCfg) {
            armorStandCfg.readFromStack(entity.getItemBySlot(EquipmentSlot.CHEST));
        }
        cfg.breastPhysics().tick(entity);
    }

    public static void addAvatarRenderLayers(@Nullable PlayerRenderer playerRenderer, ModelManager modelManager,
        BiConsumer<PlayerRenderer, RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> registration) {
        if (playerRenderer != null) {
            registration.accept(playerRenderer, new GenderLayer<>(playerRenderer));
            registration.accept(playerRenderer, new GenderArmorLayer<>(playerRenderer, modelManager));
        }
    }

    public static void addArmorStandRenderLayers(ArmorStandRenderer armorStandRenderer, ModelManager modelManager,
        BiConsumer<ArmorStandRenderer, RenderLayer<ArmorStand, ArmorStandArmorModel>> registration) {
        registration.accept(armorStandRenderer, new GenderArmorLayer<>(armorStandRenderer, modelManager));
    }

    /// Renders contributor nametags above players
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (FakeGUIPlayer.isFakePlayer(event.getEntity())) {
            //The credits screen renders fake players using contributor UUIDs; never draw nametags for those
            event.setCanRender(TriState.FALSE);
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Component nametag = WildfireGenderClient.getNametag(player.getUUID());
        if (nametag != null) {
            event.setContent(nametag);
        }
    }
}

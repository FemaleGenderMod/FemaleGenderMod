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

package com.wildfire.neoforge.client;

import com.google.common.reflect.TypeToken;
import com.wildfire.api.WildfireAPI;
import com.wildfire.client.ClientHelper;
import com.wildfire.client.WildfireClientEventHandler;
import com.wildfire.client.WildfireGenderClient;
import com.wildfire.client.WildfireKeyBindings;
import com.wildfire.client.command.WildfireCommand;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.client.gui.SyncedPlayerList;
import com.wildfire.client.render.GenderRenderState;
import com.wildfire.client.render.debug.GenderDebugHudEntry;
import com.wildfire.client.render.debug.PhysicsDebugHudEntry;
import com.wildfire.client.resources.GenderArmorResourceManager;
import com.wildfire.common.LoaderAgnostics;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.entitydata.EntityConfig;
import com.wildfire.common.entitydata.EntityConfigHolder;
import com.wildfire.common.entitydata.PlayerConfigHolder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TriState;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@Mod(value = WildfireAPI.MODID, dist = Dist.CLIENT)
public class WildfireGenderClientNeo {

    private Set<SoundEvent> hurtSounds = Collections.emptySet();

    public WildfireGenderClientNeo(ModContainer modContainer, IEventBus modEventBus) {
        WildfireGenderClient.tryMigrate();
        ClientConfig.INSTANCE.load(modContainer);

        modEventBus.addListener(RegisterKeyMappingsEvent.class, this::registerKeybindings);
        modEventBus.addListener(AddClientReloadListenersEvent.class, this::registerReloadListeners);
        modEventBus.addListener(RegisterDebugEntriesEvent.class, this::registerDebugEntries);
        modEventBus.addListener(RegisterGuiLayersEvent.class, this::registerOverlays);
        modEventBus.addListener(RegisterRenderStateModifiersEvent.class, this::registerRenderStateModifiers);
        modEventBus.addListener(EntityRenderersEvent.AddLayers.class, this::addLayers);

        //Note: Unlike fabric's mixin this ends up after all attribute tooltips instead of at the tail end of the chest equipment group,
        // but as chestplates are highly unlikely to have attributes for other slots, this is fine and allows us to avoid mixins
        NeoForge.EVENT_BUS.addListener(AddAttributeTooltipsEvent.class, event -> {
            if (event.shouldShow()) {//Only add the tooltip if the display is showing attribute modifiers
                Player player = event.getContext().player();
                //Note: this event is also fired on the logical server, so we just validate that we only add it on the client side, as the values
                // are based on client side settings
                if (player != null && player.level().isClientSide()) {
                    WildfireClientEventHandler.renderTooltip(event.getStack(), event::addTooltipLines, player);
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, this::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, _ -> WildfireClientEventHandler.clientJoin(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, _ -> WildfireClientEventHandler.clientDisconnect());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, _ -> {
            Minecraft minecraft = Minecraft.getInstance();
            WildfireClientEventHandler.onClientTick(minecraft);
            SyncedPlayerList.onTick(minecraft);
        });
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Post.class, event -> {
            if (event.getEntity() instanceof LivingEntity living && living.level().isClientSide()) {
                WildfireClientEventHandler.onEntityTick(living);
            }
        });
        NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, event -> {
            if (event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity && EntityConfig.isSupportedEntity(entity)) {
                WildfireClientEventHandler.onEntityUnload(entity, event.getLevel());
            }
        });

        if (LoaderAgnostics.INSTANCE.isDevelopmentEnv()) {
            NeoForge.EVENT_BUS.addListener(RenderNameTagEvent.CanRender.class, event -> {
                if (event.getEntity() instanceof LocalPlayer && ClientConfig.INSTANCE.displayOwnNameTag()) {
                    event.setCanRender(TriState.TRUE);
                }
            });
        }
        NeoForge.EVENT_BUS.addListener(RenderNameTagEvent.DoRender.class, this::renderNameTag);

        //Note: We intentionally only register the sound events on the client, as if the client has extra sound events it works
        // but if the server has extra, then the connection fails
        NeoClientHelper.SOUND_EVENTS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PlayLevelSoundEvent.AtEntity.class, this::onPlaySound);
    }

    private void renderNameTag(RenderNameTagEvent.DoRender event) {
        if (event.getEntityRenderState() instanceof AvatarRenderState state) {
            WildfireClientEventHandler.onPlayerNametag(state, event.getSubmitNodeCollector(), event.getPoseStack(), event.getCameraRenderState());
        }
    }

    private void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(GenderArmorResourceManager.ID, GenderArmorResourceManager.INSTANCE);
    }

    private void registerDebugEntries(RegisterDebugEntriesEvent event) {
        event.register(GenderDebugHudEntry.SELF, new GenderDebugHudEntry(true));
        event.register(GenderDebugHudEntry.OTHER, new GenderDebugHudEntry(false));
        // only register this in dev env, as this likely isn't going to be very useful anywhere else.
        if (LoaderAgnostics.INSTANCE.isDevelopmentEnv()) {
            event.register(PhysicsDebugHudEntry.ID, new PhysicsDebugHudEntry());
        }
    }

    private void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.TAB_LIST, WildfireGender.id("player_list"), WildfireClientEventHandler::renderHud);
    }

    private void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<? extends LivingEntity, LivingEntityRenderState, ?>>() {
        }, (entity, renderState) -> {
            if (renderState instanceof HumanoidRenderState state && EntityConfig.isSupportedEntity(entity)) {
                var config = EntityConfigHolder.getEntity(entity);
                state.setRenderData(NeoClientHelper.STATE, new GenderRenderState(config, entity, state, state.partialTick));
            }
        });
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        WildfireCommand.register(event.getDispatcher(), new NeoCommandHelper());
    }

    private void registerKeybindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(WildfireKeyBindings.INSTANCE.category());
        event.register(WildfireKeyBindings.INSTANCE.configKey());
        event.register(WildfireKeyBindings.INSTANCE.toggleKey());
    }

    private void addLayers(EntityRenderersEvent.AddLayers event) {
        EquipmentLayerRenderer equipmentRenderer = event.getContext().getEquipmentRenderer();
        for (final PlayerModelType skin : event.getSkins()) {
            WildfireClientEventHandler.addAvatarRenderLayers(event.getPlayerRenderer(skin), equipmentRenderer, LivingEntityRenderer::addLayer);
            WildfireClientEventHandler.addAvatarRenderLayers(event.getMannequinRenderer(skin), equipmentRenderer, LivingEntityRenderer::addLayer);
        }
        //~ if >=26.2 'net.minecraft.world.entity.EntityType' -> 'net.minecraft.world.entity.EntityTypes'
        if (event.getRenderer(net.minecraft.world.entity.EntityTypes.ARMOR_STAND) instanceof ArmorStandRenderer armorStandRenderer) {
            WildfireClientEventHandler.addArmorStandRenderLayers(armorStandRenderer, equipmentRenderer, LivingEntityRenderer::addLayer);
        }
    }

    private void onPlaySound(PlayLevelSoundEvent.AtEntity event) {
        if (ClientConfig.config().overrides().disableSoundReplacement().get()) {
            return;
        }
        Holder<SoundEvent> soundHolder = event.getSound();
        if (soundHolder != null) {
            if (hurtSounds.isEmpty()) {
                hurtSounds = Arrays.stream(DamageEffects.values()).map(DamageEffects::sound).collect(Collectors.toUnmodifiableSet());
            }
            if (hurtSounds.contains(soundHolder.value()) && event.getEntity() instanceof Player p && p.level().isClientSide()) {
                //Cancel as we handle all hurt sounds manually so that we can
                if (p.hurtTime == p.hurtDuration && p.hurtTime > 0) {
                    //Note: We check hurtTime == hurtDuration and hurtTime > 0 or otherwise when the server sends a hurt sound to the client
                    // and the client will check itself instead of the player who was damaged.
                    PlayerConfigHolder plr = WildfireGender.getPlayerById(p.getUUID());
                    if (plr != null && plr.sounds().hurt().get()) {
                        Holder<SoundEvent> soundOverride = ClientHelper.INSTANCE.hurtSound(plr.gender().get());
                        if (soundOverride != null) {
                            //If the player who produced the hurt sound is a female sound replace it
                            //TODO - Neo: Do we want to add an extra config to allow playing the sound in addition like fabric does, instead of just all out replacing it?
                            // It sort of sounds like it plays with instead of instead anyway? At least the thud and stuff still exists
                            event.setSound(soundOverride);
                            //Note: Vanilla uses + 1 for the pitch of hurt sounds, but we allow configuring the change, so we need to subtract that adjustment
                            float pitchAdjustment = plr.sounds().voicePitch().get() - 1;
                            event.setNewPitch(event.getNewPitch() + pitchAdjustment);
                        }
                    }
                }
            }
        }
    }
}

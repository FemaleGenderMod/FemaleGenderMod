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

import com.google.common.reflect.TypeToken;
import com.wildfire.client.command.WildfireCommand;
import com.wildfire.gui.SyncedPlayerList;
import com.wildfire.main.LoaderAgnostics;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireNeoSounds;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.EntityConfigHolder;
import com.wildfire.render.GenderArmorLayer;
import com.wildfire.render.GenderLayer;
import com.wildfire.render.GenderRenderState;
import com.wildfire.render.HolidayFeaturesRenderer;
import com.wildfire.render.debug.GenderDebugHudEntry;
import com.wildfire.render.debug.PhysicsDebugHudEntry;
import com.wildfire.resources.GenderArmorResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
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
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jspecify.annotations.Nullable;

@Mod(value = WildfireGender.MODID, dist = Dist.CLIENT)
public class WildfireGenderClientNeo {

    public WildfireGenderClientNeo(IEventBus modEventBus) {
        WildfireGenderClient.tryMigrate();

        //TODO - Neo: Replace this ClientConfig with a neo config and connect it to the config menu
        ClientConfig.load();

        modEventBus.addListener(RegisterKeyMappingsEvent.class, this::registerKeybindings);
        modEventBus.addListener(AddClientReloadListenersEvent.class, this::registerReloadListeners);
        modEventBus.addListener(RegisterDebugEntriesEvent.class, this::registerDebugEntries);
        modEventBus.addListener(RegisterGuiLayersEvent.class, this::registerOverlays);
        modEventBus.addListener(RegisterRenderStateModifiersEvent.class, this::registerRenderStateModifiers);
        modEventBus.addListener(EntityRenderersEvent.AddLayers.class, this::addLayers);
        //TODO: Can we get this rendering with the attributes? Similar to fabric
        modEventBus.addListener(RegisterTooltipAppendersEvent.class, event -> event.registerAppender(TooltipLocation.PRE_ITEM_INFO,
            (stack, _, _, player, _, builder) -> WildfireClientEventHandler.renderTooltip(stack, builder, player)));

        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, this::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, _ -> WildfireClientEventHandler.clientJoin(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, _ -> WildfireClientEventHandler.clientDisconnect());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, _ -> {
            Minecraft minecraft = Minecraft.getInstance();
            WildfireClientEventHandler.onClientTick(minecraft);
            SyncedPlayerList.onTick(minecraft);
        });
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Post.class, event -> {
            //TODO: Should we be checking level.tickRateManager().isEntityFrozen(living) ??
            if (event.getEntity() instanceof LivingEntity living && living.level().isClientSide()) {
                WildfireClientEventHandler.onEntityTick(living);
            }
        });
        NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, event -> {
            if (event.getLevel().isClientSide()) {
                //TODO: Should this check if it is a living entity?
                WildfireClientEventHandler.onEntityUnload(event.getEntity(), event.getLevel());
            }
        });

        if (LoaderAgnostics.INSTANCE.isDevelopmentEnv()) {
            NeoForge.EVENT_BUS.addListener(RenderNameTagEvent.CanRender.class, event -> {
                //TODO: Test this
                if (event.getEntity() instanceof LocalPlayer && ClientConfig.DISPLAY_OWN_NAMETAG) {
                    event.setCanRender(TriState.TRUE);
                }
            });
        }
        NeoForge.EVENT_BUS.addListener(RenderNameTagEvent.DoRender.class, this::renderNameTag);

        //Note: We intentionally only register the sound events on the client, as if the client has extra sound events it works
        // but if the server has extra, then the connection fails
        WildfireNeoSounds.SOUND_EVENTS.register(modEventBus);
    }

    private void renderNameTag(RenderNameTagEvent.DoRender event) {
        //TODO - Neo: Implement this
        //WildfireClientEventHandler.onPlayerNametag(event.getEntityRenderState(), event.getPoseStack(), event.getSubmitNodeCollector());
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
        //TODO - Neo: Fabric does MISC_OVERLAYS, what is the equivalent of that. Also does this render if the tab list isn't displayed?
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
}

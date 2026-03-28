package com.wildfire.main;

import com.wildfire.gui.screen.WardrobeBrowserScreen;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import com.wildfire.render.GenderArmorLayer;
import com.wildfire.render.GenderLayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

/**
 * Manejador de eventos para NeoForge 1.21.1.
 */
@EventBusSubscriber(modid = WildfireGender.MODID)
public final class WildfireEventHandler {

    public static final KeyMapping CONFIG_KEYBIND = new KeyMapping(
            "key.wildfire_gender.gender_menu",
            GLFW.GLFW_KEY_G,
            "category.wildfire_gender.generic"
    );

    private static int timer = 0;

    private WildfireEventHandler() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
    }

    // --- EVENTOS DE JUEGO ---

    @SubscribeEvent
    public static void onBeginTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player toSync) {
            PlayerConfig genderToSync = WildfireGender.getPlayerById(toSync.getUUID());
            if (genderToSync != null && event.getEntity() instanceof ServerPlayer serverPlayer) {
                WildfireSync.sendToClient(serverPlayer, genderToSync);
            }
        }
    }

    @SubscribeEvent
    public static void playerDisconnected(PlayerEvent.PlayerLoggedOutEvent event) {
        WildfireGender.PLAYER_CACHE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && Minecraft.getInstance().player != null) {
            if (event.getEntity() instanceof AbstractClientPlayer plr) {
                UUID uuid = plr.getUUID();
                boolean isClientPlayer = uuid.equals(Minecraft.getInstance().player.getUUID());
                WildfireGenderClient.loadPlayerIfMissing(uuid, isClientPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        EntityConfig.ENTITY_CACHE.remove(event.getEntity().getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {

            // Lógica de sincronización periódica
            if (timer++ % 5 == 0) {
                PlayerConfig aPlr = WildfireGender.getPlayerById(client.player.getUUID());
                if (aPlr != null) {
                    WildfireSync.sendToServer(aPlr);
                }
            }

            // Lógica de apertura de menú por tecla
            while (CONFIG_KEYBIND.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new WardrobeBrowserScreen(null, client.player.getUUID()));
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        WildfireGender.PLAYER_CACHE.clear();
        EntityConfig.ENTITY_CACHE.clear();
    }

    // --- EVENTOS DE REGISTRO (MOD BUS) ---

    @EventBusSubscriber(modid = WildfireGender.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(CONFIG_KEYBIND);
        }

        @SubscribeEvent
        public static void registerRenderLayers(EntityRenderersEvent.AddLayers event) {
            for (PlayerSkin.Model skinName : event.getSkins()) {
                LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer = event.getSkin(skinName);

                if (renderer != null) {
                    renderer.addLayer(new GenderLayer<>(renderer));
                    renderer.addLayer(new GenderArmorLayer<>(renderer, event.getContext().getModelSet()));
                }
            }

            ArmorStandRenderer armorStandRenderer = event.getRenderer(EntityType.ARMOR_STAND);

            if (armorStandRenderer != null) {
                armorStandRenderer.addLayer(new GenderArmorLayer<>(
                        armorStandRenderer,
                        event.getContext().getModelSet()
                ));
            }
        }
    }
}
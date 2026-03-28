package com.wildfire.main;

import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Clase de inicialización y utilidades de cliente para Forge 1.21.1.
 */
@OnlyIn(Dist.CLIENT)
public class WildfireGenderClient {

    public WildfireGenderClient() {
        // Constructor vacío como el original
    }

    /**
     * Equivalente a onInitializeClient de Fabric.
     * Se debe llamar desde el bus de eventos del mod (FMLClientSetupEvent).
     */
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        WildfireEventHandler.init();
    }

    public static CompletableFuture<Optional<PlayerConfig>> loadGenderInfo(UUID uuid, boolean markForSync) {
        // Usamos CompletableFuture (más moderno) y el ejecutor de fondo de Minecraft
        return CompletableFuture.supplyAsync(
                () -> Optional.ofNullable(PlayerConfig.loadCachedPlayer(uuid, markForSync)),
                Util.backgroundExecutor()
        );
    }

    /**
     * Carga al jugador en el caché si no está presente.
     */
    public static void loadPlayerIfMissing(UUID uuid, boolean markForSync) {
        if (!WildfireGender.PLAYER_CACHE.containsKey(uuid)) {
            WildfireGender.getOrAddPlayerById(uuid);
            loadGenderInfo(uuid, markForSync);
        }
    }
}
package com.wildfire.main;

import com.mojang.logging.LogUtils;
import com.wildfire.main.entitydata.PlayerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase principal del mod para NeoForge 1.21.1.
 * La anotación @Mod define el ID del mod y marca esta clase como el punto de entrada.
 */
@Mod(WildfireGender.MODID)
public class WildfireGender {
    public static final String MODID = "wildfire_gender";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Caché de configuraciones de jugadores, sincronizado para evitar errores de hilos
    public static final Map<UUID, PlayerConfig> PLAYER_CACHE = new ConcurrentHashMap<>();

    // En NeoForge 1.21.1, el IEventBus se pide directamente como parámetro en el constructor
    public WildfireGender(IEventBus modEventBus) {

        // Registramos el método de configuración común
        modEventBus.addListener(this::commonSetup);

        // Se eliminó el registro en EVENT_BUS porque esta clase no tiene métodos @SubscribeEvent.
        // Si más adelante añades eventos globales aquí, descomenta la siguiente línea:
        // NeoForge.EVENT_BUS.register(this);

        // Inicializamos los eventos comunes (si tienes lógica manual en EventHandler)
        WildfireEventHandler.init();
    }

    /**
     * Equivalente a onInitialize de Fabric.
     * Aquí se ejecutan las tareas de carga que no dependen del cliente ni del servidor.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing Wildfire Gender Common Setup...");

        // Nota: En el port que hicimos de WildfireSync, usamos @EventBusSubscriber,
        // por lo que el registro de red se hace automáticamente.
        // Si tu WildfireSync.register() tiene lógica extra, llámalo aquí:
        // WildfireSync.register();
    }

    /**
     * Busca un jugador en el caché por su UUID.
     */
    public static @Nullable PlayerConfig getPlayerById(UUID id) {
        return PLAYER_CACHE.get(id);
    }

    /**
     * Busca un jugador en el caché o crea uno nuevo si no existe.
     * (Asumiendo que PlayerConfig tiene un constructor vacío).
     */
    public static @NotNull PlayerConfig getOrAddPlayerById(UUID id) {
        return PLAYER_CACHE.computeIfAbsent(id, PlayerConfig::new);
    }
}

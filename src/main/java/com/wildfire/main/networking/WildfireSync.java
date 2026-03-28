package com.wildfire.main.networking;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

// Esta anotación hace que NeoForge registre el evento automáticamente. 
// No necesitas llamar a register() manualmente en tu clase principal.
@EventBusSubscriber(modid = WildfireGender.MODID)
public final class WildfireSync {

    private static final String PROTOCOL_VERSION = "1";

    private WildfireSync() {
        throw new UnsupportedOperationException();
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Registro de ClientboundSyncPacket (Servidor -> Cliente)
        registrar.playToClient(
                ClientboundSyncPacket.TYPE,
                ClientboundSyncPacket.STREAM_CODEC,
                (payload, context) -> payload.handle(context)
        );

        // Registro de ServerboundSyncPacket (Cliente -> Servidor)
        registrar.playToServer(
                ServerboundSyncPacket.TYPE,
                ServerboundSyncPacket.STREAM_CODEC,
                (payload, context) -> payload.handle(context)
        );
    }

    public static void sendToAllClients(@NotNull Player toSync, @NotNull PlayerConfig playerConfig) {
        // En NeoForge 1.21 esto se encarga de enviar a los que ven la entidad y a la entidad en sí
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(toSync, new ClientboundSyncPacket(playerConfig));
    }

    public static void sendToClient(@NotNull ServerPlayer sendTo, @NotNull PlayerConfig toSync) {
        PacketDistributor.sendToPlayer(sendTo, new ClientboundSyncPacket(toSync));
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(@NotNull PlayerConfig plr) {
        if (plr.needsSync) {
            PacketDistributor.sendToServer(new ServerboundSyncPacket(plr));
            plr.needsSync = false;
        }
    }
}
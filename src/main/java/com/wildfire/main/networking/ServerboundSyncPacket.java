package com.wildfire.main.networking;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundSyncPacket extends AbstractSyncPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(WildfireGender.MODID, "server_sync"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSyncPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> packet.encode(buffer), ServerboundSyncPacket::new);

    public ServerboundSyncPacket(PlayerConfig plr) {
        super(plr);
    }

    public ServerboundSyncPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                PlayerConfig plr = WildfireGender.getOrAddPlayerById(player.getUUID());
                updatePlayerFromPacket(plr);

                // Re-transmitir a otros clientes
                WildfireSync.sendToAllClients(player, plr);
            }
        });
    }
}
package com.wildfire.main.networking;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.entitydata.PlayerConfig.SyncStatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientboundSyncPacket extends AbstractSyncPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(WildfireGender.MODID, "client_sync"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundSyncPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> packet.encode(buffer), ClientboundSyncPacket::new);

    public ClientboundSyncPacket(PlayerConfig plr) {
        super(plr);
    }

    public ClientboundSyncPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerConfig plr = WildfireGender.getOrAddPlayerById(this.uuid);
            updatePlayerFromPacket(plr);
            plr.syncStatus = SyncStatus.SYNCED;
        });
    }
}
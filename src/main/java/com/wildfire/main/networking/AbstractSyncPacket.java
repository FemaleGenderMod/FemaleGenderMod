package com.wildfire.main.networking;

import com.wildfire.main.Gender;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3f;
import java.util.UUID;

public abstract class AbstractSyncPacket {
    protected final UUID uuid;
    protected final Gender gender;
    protected final float bustSize;
    protected final boolean breastPhysics;
    protected final boolean showInArmor;
    protected final float bounceMultiplier;
    protected final float floppyMultiplier;
    protected final Vector3f offsets;
    protected final boolean uniboob;
    protected final float cleavage;
    protected final boolean hurtSounds;

    protected AbstractSyncPacket(PlayerConfig plr) {
        this.uuid = plr.uuid;
        this.gender = plr.getGender();
        this.bustSize = plr.getBustSize();
        this.hurtSounds = plr.hasHurtSounds();
        this.breastPhysics = plr.hasBreastPhysics();
        this.showInArmor = plr.showBreastsInArmor();
        this.bounceMultiplier = plr.getBounceMultiplier();
        this.floppyMultiplier = plr.getFloppiness();
        Breasts breasts = plr.getBreasts();
        this.offsets = breasts.getOffsets();
        this.uniboob = breasts.isUniboob();
        this.cleavage = breasts.getCleavage();
    }

    protected AbstractSyncPacket(FriendlyByteBuf buffer) {
        this.uuid = buffer.readUUID();
        this.gender = buffer.readEnum(Gender.class);
        this.bustSize = buffer.readFloat();
        this.hurtSounds = buffer.readBoolean();
        this.breastPhysics = buffer.readBoolean();
        this.showInArmor = buffer.readBoolean();
        this.bounceMultiplier = buffer.readFloat();
        this.floppyMultiplier = buffer.readFloat();
        this.offsets = buffer.readVector3f();
        this.uniboob = buffer.readBoolean();
        this.cleavage = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.uuid);
        buffer.writeEnum(this.gender);
        buffer.writeFloat(this.bustSize);
        buffer.writeBoolean(this.hurtSounds);
        buffer.writeBoolean(this.breastPhysics);
        buffer.writeBoolean(this.showInArmor);
        buffer.writeFloat(this.bounceMultiplier);
        buffer.writeFloat(this.floppyMultiplier);
        buffer.writeVector3f(this.offsets);
        buffer.writeBoolean(this.uniboob);
        buffer.writeFloat(this.cleavage);
    }

    public void updatePlayerFromPacket(PlayerConfig plr) {
        plr.updateGender(this.gender);
        plr.updateBustSize(this.bustSize);
        plr.updateHurtSounds(this.hurtSounds);
        plr.updateBreastPhysics(this.breastPhysics);
        plr.updateShowBreastsInArmor(this.showInArmor);
        plr.updateBounceMultiplier(this.bounceMultiplier);
        plr.updateFloppiness(this.floppyMultiplier);
        Breasts breasts = plr.getBreasts();
        breasts.updateOffsets(this.offsets);
        breasts.updateUniboob(this.uniboob);
        breasts.updateCleavage(this.cleavage);
    }
}
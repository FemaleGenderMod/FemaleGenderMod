package com.wildfire.main.entitydata;

import com.wildfire.main.config.ConfigKey;
import com.wildfire.main.config.Configuration;
import java.util.function.Consumer;
import org.joml.Vector3f;

public final class Breasts {
    private float xOffset;
    private float yOffset;
    private float zOffset;
    private float cleavage;
    private boolean uniboob;

    public Breasts() {
        this.xOffset = (Float)Configuration.BREASTS_OFFSET_X.getDefault();
        this.yOffset = (Float)Configuration.BREASTS_OFFSET_Y.getDefault();
        this.zOffset = (Float)Configuration.BREASTS_OFFSET_Z.getDefault();
        this.cleavage = (Float)Configuration.BREASTS_CLEAVAGE.getDefault();
        this.uniboob = (Boolean)Configuration.BREASTS_UNIBOOB.getDefault();
    }

    private <VALUE> boolean updateValue(ConfigKey<VALUE> key, VALUE value, Consumer<VALUE> setter) {
        if (key.validate(value)) {
            setter.accept(value);
            return true;
        } else {
            return false;
        }
    }

    public Vector3f getOffsets() {
        return new Vector3f(this.xOffset, this.yOffset, this.zOffset);
    }

    public void updateOffsets(Vector3f offsets) {
        this.updateXOffset(offsets.x);
        this.updateYOffset(offsets.y);
        this.updateZOffset(offsets.z);
    }

    public float getXOffset() {
        return this.xOffset;
    }

    public boolean updateXOffset(float value) {
        return this.updateValue(Configuration.BREASTS_OFFSET_X, value, (v) -> this.xOffset = v);
    }

    public float getYOffset() {
        return this.yOffset;
    }

    public boolean updateYOffset(float value) {
        return this.updateValue(Configuration.BREASTS_OFFSET_Y, value, (v) -> this.yOffset = v);
    }

    public float getZOffset() {
        return this.zOffset;
    }

    public boolean updateZOffset(float value) {
        return this.updateValue(Configuration.BREASTS_OFFSET_Z, value, (v) -> this.zOffset = v);
    }

    public float getCleavage() {
        return this.cleavage;
    }

    public boolean updateCleavage(float value) {
        return this.updateValue(Configuration.BREASTS_CLEAVAGE, value, (v) -> this.cleavage = v);
    }

    public boolean isUniboob() {
        return this.uniboob;
    }

    public boolean updateUniboob(boolean value) {
        return this.updateValue(Configuration.BREASTS_UNIBOOB, value, (v) -> this.uniboob = v);
    }
}

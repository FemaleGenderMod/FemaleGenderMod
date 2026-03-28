package com.wildfire.api;

public interface IGenderArmor {
    default boolean coversBreasts() {
        return true;
    }

    default boolean alwaysHidesBreasts() {
        return false;
    }

    default float physicsResistance() {
        return 0.0f;
    }

    default float tightness() {
        return 0.0f;
    }

    default boolean armorStandsCopySettings() {
        return !this.alwaysHidesBreasts() && this.coversBreasts() && this.physicsResistance() == 1.0f;
    }
}

package com.wildfire.api.impl;

import com.wildfire.api.IBreastArmorTexture;
import com.wildfire.api.IGenderArmor;

/**
 * Default record implementation of {@link IGenderArmor} used for resource pack entries
 *
 * @see IGenderArmor
 * @since 4.0.0
 */
public record GenderArmor(
      float physicsResistance,
      float tightness,
      boolean coversBreasts,
      boolean alwaysHidesBreasts,
      boolean armorStandsCopySettings,
      IBreastArmorTexture texture
) implements IGenderArmor {
    /**
     * Default implementation used to represent armor types that lack any configuration
     */
    public static final IGenderArmor DEFAULT = new Default();

    /**
     * Default implementation used when the player {@link net.minecraft.world.item.ItemStack#isEmpty() isn't wearing a chestplate},
     * or if the worn chestplate specifies that it doesn't cover the breasts.
     */
    public static final IGenderArmor EMPTY = new GenderArmor(0f, 0f, false, false, false, BreastArmorTexture.DEFAULT);

    public GenderArmor(float physicsResistance, float tightness, boolean armorStandsCopySettings) {
        this(physicsResistance, tightness, true, false, armorStandsCopySettings, IBreastArmorTexture.DEFAULT);
    }

    /**
     * Dummy implementation of {@link IGenderArmor}; simply defers to the default interface implementations for all methods.
     */
    public static final class Default implements IGenderArmor {//TODO - 1.21: Re-evaluate this and probably remove
        private Default() {}
    }
}
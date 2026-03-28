// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package com.wildfire.render.armor;

import com.wildfire.api.IGenderArmor;

public record SimpleGenderArmor(float physicsResistance, float tightness, boolean armorStandsCopySettings) implements IGenderArmor {
    public static final SimpleGenderArmor FALLBACK = new SimpleGenderArmor(0.5F);
    public static final SimpleGenderArmor LEATHER = new SimpleGenderArmor(0.3F, 0.5F);
    public static final SimpleGenderArmor CHAIN_MAIL = new SimpleGenderArmor(0.5F, 0.2F);
    public static final SimpleGenderArmor GOLD = new SimpleGenderArmor(0.85F, true);
    public static final SimpleGenderArmor IRON = new SimpleGenderArmor(0.7F, true);
    public static final SimpleGenderArmor DIAMOND = new SimpleGenderArmor(0.8F, true);
    public static final SimpleGenderArmor NETHERITE = new SimpleGenderArmor(1.0F, true);

    public SimpleGenderArmor(float physicsResistance) {
        this(physicsResistance, 0.0F, false);
    }

    public SimpleGenderArmor(float physicsResistance, boolean armorStandsCopySettings) {
        this(physicsResistance, 0.0F, armorStandsCopySettings);
    }

    public SimpleGenderArmor(float physicsResistance, float tightness) {
        this(physicsResistance, tightness, false);
    }
}

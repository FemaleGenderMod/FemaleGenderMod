package com.wildfire.main.config;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Configuration extends AbstractConfiguration {
    private static final String CONFIG_DIR = "WildfireGender";
    public static final UUIDConfigKey USERNAME;
    public static final GenderConfigKey GENDER;
    public static final FloatConfigKey BUST_SIZE;
    public static final BooleanConfigKey HURT_SOUNDS;
    public static final FloatConfigKey BREASTS_OFFSET_X;
    public static final FloatConfigKey BREASTS_OFFSET_Y;
    public static final FloatConfigKey BREASTS_OFFSET_Z;
    public static final BooleanConfigKey BREASTS_UNIBOOB;
    public static final FloatConfigKey BREASTS_CLEAVAGE;
    public static final BooleanConfigKey BREAST_PHYSICS;
    public static final BooleanConfigKey ARMOR_PHYSICS_OVERRIDE;
    public static final BooleanConfigKey SHOW_IN_ARMOR;
    public static final FloatConfigKey BOUNCE_MULTIPLIER;
    public static final FloatConfigKey FLOPPY_MULTIPLIER;

    public Configuration(String cfgName) {
        super("WildfireGender", cfgName);
    }

    static {
        USERNAME = new UUIDConfigKey("username", UUID.nameUUIDFromBytes("UNKNOWN".getBytes(StandardCharsets.UTF_8)));
        GENDER = new GenderConfigKey("gender");
        BUST_SIZE = new FloatConfigKey("bust_size", 0.6F, 0.0F, 0.8F);
        HURT_SOUNDS = new BooleanConfigKey("hurt_sounds", true);
        BREASTS_OFFSET_X = new FloatConfigKey("breasts_xOffset", 0.0F, -1.0F, 1.0F);
        BREASTS_OFFSET_Y = new FloatConfigKey("breasts_yOffset", 0.0F, -1.0F, 1.0F);
        BREASTS_OFFSET_Z = new FloatConfigKey("breasts_zOffset", 0.0F, -1.0F, 0.0F);
        BREASTS_UNIBOOB = new BooleanConfigKey("breasts_uniboob", true);
        BREASTS_CLEAVAGE = new FloatConfigKey("breasts_cleavage", 0.0F, 0.0F, 0.1F);
        BREAST_PHYSICS = new BooleanConfigKey("breast_physics", true);
        ARMOR_PHYSICS_OVERRIDE = new BooleanConfigKey("armor_physics_override", false);
        SHOW_IN_ARMOR = new BooleanConfigKey("show_in_armor", true);
        BOUNCE_MULTIPLIER = new FloatConfigKey("bounce_multiplier", 0.333F, 0.0F, 0.5F);
        FLOPPY_MULTIPLIER = new FloatConfigKey("floppy_multiplier", 0.75F, 0.25F, 1.0F);
    }
}

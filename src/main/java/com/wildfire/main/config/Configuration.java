/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.main.config;

import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;

public class Configuration extends AbstractConfiguration {

    public static final String CONFIG_DIR = "FemaleGenderMod";

    public static final ConfigKey<Gender> GENDER = ConfigKey.create("gender", Gender.MALE, Gender.CODEC_OR_LEGACY);
    public static final ConfigKey<Float> BUST_SIZE = ConfigKey.create("bust_size", 0.6F, 0, 0.8f);
    public static final ConfigKey<Boolean> HURT_SOUNDS = ConfigKey.create("hurt_sounds", true);
    public static final ConfigKey<Float> VOICE_PITCH = ConfigKey.create("voice_pitch", 1F, 0.8f, 1.2f);

    public static final ConfigKey<Float> BREASTS_OFFSET_X = ConfigKey.create("breasts_xOffset", 0.0F, -1, 1);
    public static final ConfigKey<Float> BREASTS_OFFSET_Y = ConfigKey.create("breasts_yOffset", 0.0F, -1, 1);
    public static final ConfigKey<Float> BREASTS_OFFSET_Z = ConfigKey.create("breasts_zOffset", 0.0F, -1, 0);
    public static final ConfigKey<Boolean> BREASTS_UNIBOOB = ConfigKey.create("breasts_uniboob", true);
    public static final ConfigKey<Float> BREASTS_CLEAVAGE = ConfigKey.create("breasts_cleavage", 0, 0, 0.1F);

    public static final ConfigKey<Boolean> BREAST_PHYSICS = ConfigKey.create("breast_physics", true);
    public static final ConfigKey<Boolean> SHOW_IN_ARMOR = ConfigKey.create("show_in_armor", true);
    public static final ConfigKey<Float> BOUNCE_MULTIPLIER = ConfigKey.create("bounce_multiplier", 0.333F, 0, 0.5f);
    public static final ConfigKey<Float> FLOPPY_MULTIPLIER = ConfigKey.create("floppy_multiplier", 0.75F, 0.25f, 1);

    public static final ConfigKey<Boolean> HOLIDAY_THEMES = ConfigKey.create("holiday_themes", true);

    // TODO change these UVLayout entries to use UVMap objects?
    //        would probably require adding some form of migration capability to AbstractConfiguration

    // Base breasts
    public static final ConfigKey<UVLayout> LEFT_BREAST_UV_LAYOUT = ConfigKey.create("leftBreastUVLayout", new UVLayout(
        new UVQuad(24, 21, 27, 26),  // EAST
        new UVQuad(16, 21, 20, 26),  // WEST
        new UVQuad(20, 17, 24, 21),  // DOWN
        new UVQuad(20, 25, 24, 27),  // UP
        new UVQuad(20, 21, 24, 26)   // NORTH
    )::copy);

    public static final ConfigKey<UVLayout> RIGHT_BREAST_UV_LAYOUT = ConfigKey.create("rightBreastUVLayout", new UVLayout(
        new UVQuad(28, 21, 32, 26),  // EAST
        new UVQuad(21, 21, 24, 26),  // WEST
        new UVQuad(24, 17, 28, 21),  // DOWN
        new UVQuad(24, 25, 28, 27),  // UP
        new UVQuad(24, 21, 28, 26)   // NORTH
    )::copy);

    // Overlay breasts
    public static final ConfigKey<UVLayout> LEFT_BREAST_OVERLAY_UV_LAYOUT = ConfigKey.create("leftBreastOverlayUVLayout", new UVLayout(
        UVQuad.UNUSED,                              // EAST
        new UVQuad(17, 37, 20, 42),  // WEST
        new UVQuad(20, 34, 24, 37),  // DOWN
        new UVQuad(20, 41, 24, 44),  // UP
        new UVQuad(20, 37, 24, 42)   // NORTH
    )::copy);

    public static final ConfigKey<UVLayout> RIGHT_BREAST_OVERLAY_UV_LAYOUT = ConfigKey.create("rightBreastOverlayUVLayout", new UVLayout(
        new UVQuad(28, 37, 31, 42),  // EAST
        UVQuad.UNUSED,                              // WEST
        new UVQuad(24, 34, 28, 37),  // DOWN
        new UVQuad(24, 41, 28, 44),  // UP
        new UVQuad(24, 37, 28, 42)   // NORTH
    )::copy);

    public Configuration(String cfgName) {
        super(CONFIG_DIR, cfgName);
    }
}

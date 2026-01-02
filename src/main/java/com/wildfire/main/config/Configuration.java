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

import com.google.gson.JsonObject;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.functions.BreastGetter;
import com.wildfire.main.config.functions.BreastSetter;
import com.wildfire.main.config.functions.PlayerGetter;
import com.wildfire.main.config.functions.PlayerSetter;
import com.wildfire.main.config.types.*;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class Configuration extends AbstractConfiguration {

	public static final String CONFIG_DIR = "FemaleGenderMod";

	public static final EnumConfigKey<Gender> GENDER = new EnumConfigKey<>("gender", Gender.MALE, Gender.BY_ID);
	public static final FloatConfigKey BUST_SIZE = new FloatConfigKey("bust_size", 0.6F, 0, 0.8f);
	public static final BooleanConfigKey HURT_SOUNDS = new BooleanConfigKey("hurt_sounds", true);
	public static final FloatConfigKey VOICE_PITCH = new FloatConfigKey("voice_pitch", 1F, 0.8f, 1.2f);

	public static final FloatConfigKey BREASTS_OFFSET_X = new FloatConfigKey("breasts_xOffset", 0.0F, -1, 1);
	public static final FloatConfigKey BREASTS_OFFSET_Y = new FloatConfigKey("breasts_yOffset", 0.0F, -1, 1);
	public static final FloatConfigKey BREASTS_OFFSET_Z = new FloatConfigKey("breasts_zOffset", 0.0F, -1, 0);
	public static final BooleanConfigKey BREASTS_UNIBOOB = new BooleanConfigKey("breasts_uniboob", true);
	public static final FloatConfigKey BREASTS_CLEAVAGE = new FloatConfigKey("breasts_cleavage", 0, 0, 0.1F);

	public static final BooleanConfigKey BREAST_PHYSICS = new BooleanConfigKey("breast_physics", true);
	public static final BooleanConfigKey SHOW_IN_ARMOR = new BooleanConfigKey("show_in_armor", true);
	public static final FloatConfigKey BOUNCE_MULTIPLIER = new FloatConfigKey("bounce_multiplier", 0.333F, 0, 0.5f);
	public static final FloatConfigKey FLOPPY_MULTIPLIER = new FloatConfigKey("floppy_multiplier", 0.75F, 0.25f, 1);

	public static final BooleanConfigKey HOLIDAY_THEMES = new BooleanConfigKey("holiday_themes", true);

	// Base breasts
	public static final ConfigKey<UVLayout> LEFT_BREAST_UV_LAYOUT =
			new UVLayoutConfigKey("leftBreastUVLayout", new UVLayout(
					new UVQuad(24, 21, 27, 26),  // EAST
					new UVQuad(16, 21, 20, 26),  // WEST
					new UVQuad(20, 17, 24, 21),  // DOWN
					new UVQuad(20, 25, 24, 27),  // UP
					new UVQuad(20, 21, 24, 26)   // NORTH
			));

	public static final ConfigKey<UVLayout> RIGHT_BREAST_UV_LAYOUT =
			new UVLayoutConfigKey("rightBreastUVLayout", new UVLayout(
					new UVQuad(28, 21, 32, 26),  // EAST
					new UVQuad(21, 21, 24, 26),  // WEST
					new UVQuad(24, 17, 28, 21),  // DOWN
					new UVQuad(24, 25, 28, 27),  // UP
					new UVQuad(24, 21, 28, 26)   // NORTH
			));

	// Overlay breasts
	public static final ConfigKey<UVLayout> LEFT_BREAST_OVERLAY_UV_LAYOUT =
			new UVLayoutConfigKey("leftBreastOverlayUVLayout", new UVLayout(
					new UVQuad( 0,  0,  0,  0),  // EAST (not used)
					new UVQuad(17, 37, 20, 42),  // WEST
					new UVQuad(20, 34, 24, 37),  // DOWN
					new UVQuad(20, 42, 24, 45),  // UP
					new UVQuad(20, 37, 24, 42)   // NORTH
			));

	public static final ConfigKey<UVLayout> RIGHT_BREAST_OVERLAY_UV_LAYOUT =
			new UVLayoutConfigKey("rightBreastOverlayUVLayout", new UVLayout(
					new UVQuad(28, 37, 31, 42),  // EAST
					new UVQuad( 0,  0,  0,  0),  // WEST (not used)
					new UVQuad(24, 34, 28, 37),  // DOWN
					new UVQuad(24, 42, 28, 45),  // UP
					new UVQuad(24, 37, 28, 42)   // NORTH
			));

	// Armor breasts
	public static final ConfigKey<UVLayout> LEFT_BREAST_ARMOR_UV_LAYOUT =
			new UVLayoutConfigKey("leftBreastArmorUVLayout", new UVLayout(
					new UVQuad(24, 21, 28, 26),  // EAST
					new UVQuad(16, 21, 20, 26),  // WEST
					new UVQuad(20, 17, 24, 21),  // DOWN
					new UVQuad(20, 25, 24, 27),  // UP
					new UVQuad(20, 21, 24, 26)   // NORTH
			));

	public static final ConfigKey<UVLayout> RIGHT_BREAST_ARMOR_UV_LAYOUT =
			new UVLayoutConfigKey("rightBreastArmorUVLayout", new UVLayout(
					new UVQuad(28, 21, 32, 26),  // EAST
					new UVQuad(20, 21, 24, 26),  // WEST
					new UVQuad(24, 17, 28, 21),  // DOWN
					new UVQuad(24, 25, 28, 27),  // UP
					new UVQuad(24, 21, 28, 26)   // NORTH
			));


	public static final @Unmodifiable List<RegisteredKey<?>> KEYS = List.of(
			new RegisteredKey<>(GENDER, PlayerConfig::getGender, PlayerConfig::updateGender),
			new RegisteredKey<>(BUST_SIZE, PlayerConfig::getBustSize, PlayerConfig::updateBustSize),
			new RegisteredKey<>(HURT_SOUNDS, PlayerConfig::hasHurtSounds, PlayerConfig::updateHurtSounds),
			new RegisteredKey<>(VOICE_PITCH, PlayerConfig::getVoicePitch, PlayerConfig::updateVoicePitch),

			new RegisteredKey<>(BREASTS_OFFSET_X, Breasts::getXOffset, Breasts::updateXOffset),
			new RegisteredKey<>(BREASTS_OFFSET_Y, Breasts::getYOffset, Breasts::updateYOffset),
			new RegisteredKey<>(BREASTS_OFFSET_Z, Breasts::getZOffset, Breasts::updateZOffset),
			new RegisteredKey<>(BREASTS_UNIBOOB, Breasts::isUniboob, Breasts::updateUniboob),
			new RegisteredKey<>(BREASTS_CLEAVAGE, Breasts::getCleavage, Breasts::updateCleavage),

			new RegisteredKey<>(BREAST_PHYSICS, PlayerConfig::hasBreastPhysics, PlayerConfig::updateBreastPhysics),
			new RegisteredKey<>(SHOW_IN_ARMOR, PlayerConfig::showBreastsInArmor, PlayerConfig::updateShowBreastsInArmor),
			new RegisteredKey<>(BOUNCE_MULTIPLIER, PlayerConfig::getBounceMultiplier, PlayerConfig::updateBounceMultiplier),
			new RegisteredKey<>(FLOPPY_MULTIPLIER, PlayerConfig::getFloppiness, PlayerConfig::updateFloppiness),

			new RegisteredKey<>(HOLIDAY_THEMES, PlayerConfig::hasHolidayThemes, PlayerConfig::updateHolidayThemes),

			new RegisteredKey<>(LEFT_BREAST_UV_LAYOUT, PlayerConfig::getLeftBreastUVLayout, PlayerConfig::updateLeftBreastUVLayout),
			new RegisteredKey<>(RIGHT_BREAST_UV_LAYOUT, PlayerConfig::getRightBreastUVLayout, PlayerConfig::updateRightBreastUVLayout),

			new RegisteredKey<>(LEFT_BREAST_OVERLAY_UV_LAYOUT, PlayerConfig::getLeftBreastOverlayUVLayout, PlayerConfig::updateLeftBreastOverlayUVLayout),
			new RegisteredKey<>(RIGHT_BREAST_OVERLAY_UV_LAYOUT, PlayerConfig::getRightBreastOverlayUVLayout, PlayerConfig::updateRightBreastOverlayUVLayout)
	);

	public Configuration(String cfgName) {
		super(CONFIG_DIR, cfgName);
	}

	public void setDefaults() {
		KEYS.stream().map(RegisteredKey::key).forEach(this::setDefault);
	}

	public record RegisteredKey<T>(ConfigKey<T> key, PlayerGetter<T> getter, PlayerSetter<T> setter) {
		RegisteredKey(ConfigKey<T> key, BreastGetter<T> getter, BreastSetter<T> setter) {
			// java isn't quite smart enough to do all of this for us, but it is smart enough to cast the setter
			// for us, so long as we give it enough of a hint with the getter.
			this(key, (PlayerGetter<T>) getter, setter);
		}

		public void dump(PlayerConfig config, JsonObject obj) {
			key.save(obj, config.getConfig().get(key));
		}

		public void writeToConfig(PlayerConfig player) {
			player.getConfig().set(key, getter.get(player));
		}

		public void writeToPlayer(PlayerConfig player) {
			setter.set(player, player.getConfig().get(key));
		}
	}
}

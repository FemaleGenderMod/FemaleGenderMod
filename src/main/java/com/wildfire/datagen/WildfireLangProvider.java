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

package com.wildfire.datagen;

import com.wildfire.main.WildfireGender;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
class WildfireLangProvider extends FabricLanguageProvider {
	protected WildfireLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(dataOutput, registryLookup);
	}

	@Override
	public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
		builder.add("category.wildfire_gender.generic", "Female Gender Mod");
		builder.add("key.wildfire_gender.gender_menu", "Female Gender Menu");
		builder.add("key.wildfire_gender.toggle", "Toggle Breast Rendering");

		builder.add(WildfireGender.id("armor.tooltip"), "+%s Breast Support");
		builder.add(WildfireGender.id("wardrobe.players_using_mod"), "Synced Players:");

		builder.add(WildfireGender.id("always_show_list"), "Show Synced Players: %s");
		builder.add(WildfireGender.id("always_show_list.mod_ui_only"), "This screen");
		builder.add(WildfireGender.id("always_show_list.mod_ui_only.tooltip"), "The synced player list will only show while in this menu");
		builder.add(WildfireGender.id("always_show_list.tab_list_open"), "Player list");
		builder.add(WildfireGender.id("always_show_list.tab_list_open.tooltip"), "The synced player list will show while in this menu or by pressing %s");
		builder.add(WildfireGender.id("always_show_list.always"), "Always");
		builder.add(WildfireGender.id("always_show_list.always.tooltip"), "The synced player list will always show");

		builder.add(WildfireGender.id("wardrobe.title"), "Female Gender Mod");
		builder.add(WildfireGender.id("breast_customization.tab_customization"), "Customization");
		builder.add(WildfireGender.id("breast_customization.tab_physics"), "Breast Physics");
		builder.add(WildfireGender.id("breast_customization.tab_miscellaneous"), "Miscellaneous");

		builder.add(WildfireGender.id("breast_customization.presets.add_new"), "Add New...");
		builder.add(WildfireGender.id("breast_customization.presets.delete"), "Delete");

		builder.add(WildfireGender.id("wardrobe.slider.breast_size"), "Breast Size: %s%%");
		builder.add(WildfireGender.id("wardrobe.slider.separation"), "Separation: %s");
		builder.add(WildfireGender.id("wardrobe.slider.height"), "Height: %s");
		builder.add(WildfireGender.id("wardrobe.slider.depth"), "Depth: %s");
		builder.add(WildfireGender.id("wardrobe.slider.rotation"), "Rotation: %s°");
		builder.add(WildfireGender.id("slider.voice_pitch"), "Pitch: %s%%");

		builder.add(WildfireGender.id("appearance_settings.title"), "Character Personalization");
		builder.add(WildfireGender.id("char_settings.title"), "Character Settings OLD");
		builder.add(WildfireGender.id("char_settings.physics"), "Breast Physics: %s");

		builder.add(WildfireGender.id("char_settings.override_armor_physics"), "Armor Physics: %s");
		emitMultiple(
				builder, "tooltip.override_armor_physics",
				"Breast physics will no longer be reduced/suppressed by your equipped armor while enabled",
				"This is intended for use with resource packs that hide armor, or any similar minimal armor packs"
		);

		builder.add(WildfireGender.id("char_settings.hide_in_armor"), "Hide In Armor: %s");
		builder.add(WildfireGender.id("char_settings.show_armor_stat"), "Show Armor Tooltip: %s");
		builder.add(WildfireGender.id("char_settings.hurt_sounds"), "Female Hurt Sounds: %s");
		builder.add(WildfireGender.id("tooltip.hurt_sounds"), "Your character will play a female hurt sound when taking damage if your gender is set to either Female or Other");

		builder.add(WildfireGender.id("breast_customization.dual_physics"), "Dual-Physics: %s");

		builder.add(WildfireGender.id("label.gender"), "Gender");
		builder.add(WildfireGender.id("label.female"), "Female");
		builder.add(WildfireGender.id("label.male"), "Male");
		builder.add(WildfireGender.id("label.other"), "Other");

		builder.add(WildfireGender.id("label.enabled"), "Enabled");
		builder.add(WildfireGender.id("label.disabled"), "Disabled");
		builder.add(WildfireGender.id("label.on"), "On");
		builder.add(WildfireGender.id("label.off"), "Off");
		builder.add(WildfireGender.id("label.yes"), "Yes");
		builder.add(WildfireGender.id("label.no"), "No");
		builder.add(WildfireGender.id("label.with_creator"), "You are playing on a server with the creator of this mod!");
		builder.add(WildfireGender.id("label.with_contributor"), "You are playing on a server with a contributor of this mod!");
		builder.add(WildfireGender.id("label.with_both"), "You are playing on a server with the creator and a contributor of this mod!");

		builder.add(WildfireGender.id("slider.bounce"), "Intensity: %s%%");
		builder.add(WildfireGender.id("slider.floppy"), "Momentum: %s%%");

		builder.add(WildfireGender.id("cancer_awareness.title"), "Hey, it's Breast Cancer Awareness Month!");

		builder.add(WildfireGender.id("first_time_setup.title"), "Welcome to the Female Gender Mod!");
		builder.add(WildfireGender.id("first_time_setup.description"), "Would you like to enable cloud server syncing for your gender settings? This feature allows other players to view your customized gender appearance, even if the server doesn't have the mod installed.");
		builder.add(WildfireGender.id("first_time_setup.notice"), "You can always change this setting later in the mod menu.");
		builder.add(WildfireGender.id("first_time_setup.enable"), "Enable Cloud Syncing");
		builder.add(WildfireGender.id("first_time_setup.disable"), "Disable Cloud Syncing");

		builder.add(WildfireGender.id("cloud_settings"), "Cloud Sync Server Settings");
		builder.add(WildfireGender.id("cloud.tooltip"), "Cloud Sync");
		builder.add(WildfireGender.id("cloud.unavailable.invalid_account"), "Cloud syncing is unavailable as you aren't currently logged into a valid Minecraft account");
		builder.add(WildfireGender.id("cloud.unavailable.offline_server"), "Cloud syncing is unavailable as the server you're connected to is in offline mode");
		builder.add(WildfireGender.id("cloud.status"), "Cloud Sync: %s");
		builder.add(WildfireGender.id("cloud.automatic"), "Automatic Sync: %s");
		emitMultiple(
				builder, "cloud.automatic.tooltip",
				"While enabled, your config will automatically be synced to the cloud after making any changes.",
				"You can still sync manually with the button below if this is disabled."
		);
		builder.add(WildfireGender.id("cloud.sync"), "Sync Now");
		builder.add(WildfireGender.id("cloud.syncing"), "Syncing...");

		builder.add(WildfireGender.id("cloud.status_log"), "Status Log");

		builder.add(WildfireGender.id("cloud.syncing.success"), "Synced");
		builder.add(WildfireGender.id("cloud.syncing.fail"), "Sync Failed");

		builder.add(WildfireGender.id("sync_log.authenticating_mojang"), "Authenticating with Mojang...");
		builder.add(WildfireGender.id("sync_log.authenticating_sync"), "Authenticating with cloud sync...");
		builder.add(WildfireGender.id("sync_log.authentication_failed"), "Authentication failed.");
		builder.add(WildfireGender.id("sync_log.reauthenticating"), "Re-authenticating...");

		builder.add(WildfireGender.id("sync_log.failed_to_sync_data"), "Failed to sync data.");
		builder.add(WildfireGender.id("sync_log.attempting_sync"), "Syncing profile...");
		builder.add(WildfireGender.id("sync_log.sync_success"), "Sync successful.");
		builder.add(WildfireGender.id("sync_log.sync_too_frequently"), "Sync rate limited.");

		builder.add(WildfireGender.id("sync_log.get_single_profile"), "Retrieving profile...");
		builder.add(WildfireGender.id("sync_log.get_multiple_profiles"), "Retrieving batch of profiles...");

		builder.add(WildfireGender.id("details.next_page"), "Next Page");
		builder.add(WildfireGender.id("details.prev_page"), "Prev Page");

		builder.add(WildfireGender.id("cloud_details.title"), "Cloud Sync Server Information");

		builder.add(WildfireGender.id("nametag.creator"), "Female Gender Mod Creator");
		builder.add(WildfireGender.id("nametag.contributor"), "Female Gender Mod Contributor");

		builder.add(WildfireGender.id("misc.holiday_themes"), "Holiday Themes: %s");
		emitMultiple(builder, "tooltip.holiday_themes", "When enabled, this feature automatically showcases cosmetics like Santa hats and other holiday-themed items during their respective holidays.");

		// intentionally omitted as they aren't used anywhere:
//		builder.add("toast.wildfire_gender.get_started", "Press %s to get started!");
//		builder.add(WildfireGender.id("player_list.title"), "Female Gender Mod");
//		builder.add(WildfireGender.id("player_list.settings_button"), "Settings");
//		builder.add(WildfireGender.id("player_list.sync_status"), "Sync Status");
//		builder.add(WildfireGender.id("player_list.state.loading"), "Loading Data...");
//		builder.add(WildfireGender.id("player_list.state.synced"), "Synced Player");
//		builder.add(WildfireGender.id("player_list.bounce_multiplier"), "Bounce Multiplier: %sx");
//		builder.add(WildfireGender.id("player_list.breast_momentum"), "Breast Momentum: %s%%");
//		builder.add(WildfireGender.id("player_list.female_sounds"), "Female Sounds: %s");
	}

	private void emitMultiple(TranslationBuilder builder, String key, String... lines) {
		int index = 1;
		for(var line : lines) {
			builder.add(WildfireGender.id(key + ".line" + index++), line);
		}
	}
}

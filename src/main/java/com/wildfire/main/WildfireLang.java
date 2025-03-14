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

package com.wildfire.main;

import com.wildfire.main.text.ILangEntry;
import org.jetbrains.annotations.NotNull;

//TODO - 1.21: Data generate the values for this enum
//TODO - 1.21: Organize this enum
public enum WildfireLang implements ILangEntry {
    MOD_NAME("wildfire_gender.name"),
    PACK_DESCRIPTION("wildfire_gender.pack_description"),

    KEY_CATEGORY("category.wildfire_gender.generic"),
    KEY_CONFIG("key.wildfire_gender.gender_menu"),
    KEY_TOGGLE("key.wildfire_gender.toggle"),

    ARMOR_TOOLTIP("armor", "tooltip"),

    HURT_SOUND_SUBTITLE("hurt", "female"),

    PLAYER_LIST_MODE("wildfire_gender.always_show_list"),
    PLAYER_LIST_MODE_MOD_UI("always_show_list", "mod_ui_only"),
    PLAYER_LIST_MODE_MOD_UI_TOOLTIP("always_show_list", "mod_ui_only.tooltip"),
    PLAYER_LIST_MODE_TAB_LIST("always_show_list", "tab_list_open"),
    PLAYER_LIST_MODE_TAB_LIST_TOOLTIP("always_show_list", "tab_list_open.tooltip"),
    PLAYER_LIST_MODE_ALWAYS("always_show_list", "always"),
    PLAYER_LIST_MODE_ALWAYS_TOOLTIP("always_show_list", "always.tooltip"),

    OFF("label", "off"),
    ENABLED("label", "enabled"),
    DISABLED("label", "disabled"),
    GENDER("label", "gender"),
    FEMALE("label", "female"),
    MALE("label", "male"),
    OTHER("label", "other"),
    LABEL_WITH_CREATOR("label", "with_creator"),
    LABEL_WITH_CONTRIBUTOR("label", "with_contributor"),
    LABEL_WITH_BOTH("label", "with_both"),

    TOOLTIP_BOUNCE_WARNING("tooltip", "bounce_warning"),
    TOOLTIP_HIDE_IN_ARMOR("tooltip", "hide_in_armor"),
    TOOLTIP_HURT_SOUNDS("tooltip", "hurt_sounds"),
    TOOLTIP_OVERRIDE_PHYSICS_1("tooltip", "override_armor_physics.line1"),
    TOOLTIP_OVERRIDE_PHYSICS_2("tooltip", "override_armor_physics.line2"),
    TOOLTIP_HOLIDAY_THEMES_1("tooltip", "holiday_themes.line1"),

    NAME_TAG_CREATOR("nametag", "creator"),
    NAME_TAG_CONTRIBUTOR("name_tag", "contributor"),

    HOLIDAY_THEMES("misc", "holiday_themes"),

    BREAST_CUSTOMIZATION_DUAL_PHYSICS("breast_customization", "dual_physics"),
    BREAST_CUSTOMIZATION_TAB_CUSTOMIZATION("breast_customization", "tab_customization"),
    BREAST_CUSTOMIZATION_TAB_PHYSICS("breast_customization", "tab_physics"),
    BREAST_CUSTOMIZATION_TAB_MISC("breast_customization", "tab_miscellaneous"),
    //Presets TODO: Use these?
    PRESETS_ADD_NEW("breast_customization", "presets.add_new"),
    PRESETS_DELETE("breast_customization", "presets.delete"),

    WARDROBE("wardrobe", "title"),
    WARDROBE_PLAYERS_USING("wardrobe", "players_using_mod"),
    WARDROBE_SLIDER_BREAST_SIZE("wardrobe", "slider.breast_size"),
    WARDROBE_SLIDER_SEPARATION("wardrobe", "slider.separation"),
    WARDROBE_SLIDER_HEIGHT("wardrobe", "slider.height"),
    WARDROBE_SLIDER_DEPTH("wardrobe", "slider.depth"),
    WARDROBE_SLIDER_ROTATION("wardrobe", "slider.rotation"),

    CHAR_SETTINGS("char_settings", "title"),
    CHAR_SETTING_PHYSICS("char_settings", "physics"),
    CHAR_SETTING_HIDE_IN_ARMOR("char_settings", "hide_in_armor"),
    CHAR_SETTING_OVERRIDE_PHYSICS("char_settings", "override_armor_physics"),
    CHAR_SETTING_HURT_SOUNDS("char_settings", "hurt_sounds"),
    CHAR_SETTING_SHOW_ARMOR_STAT("char_settings", "show_armor_stat"),

    CANCER_AWARENESS("cancer_awareness", "title"),
    APPEARANCE_SETTINGS("appearance_settings", "title"),

    SLIDER_BOUNCE("slider", "bounce"),
    SLIDER_FLOPPY("slider", "floppy"),
    SLIDER_VOICE_PITCH("slider", "voice_pitch"),

    //First time setup TODO: Proper translations
    FIRST_TIME_SETUP("first_time_setup", "title"),
    FIRST_TIME_SETUP_DESCRIPTION("first_time_setup", "description"),
    FIRST_TIME_SETUP_NOTICE("first_time_setup", "notice"),
    FIRST_TIME_SETUP_ENABLE_CLOUD_SYNC("first_time_setup", "enable"),
    FIRST_TIME_SETUP_DISABLE_CLOUD_SYNC("first_time_setup", "disable"),

    //TODO: Re-evaluate these details ones
    CLOUD_DETAILS("cloud_details", "title"),
    CLOUD_DETAILS_PAGE1("cloud_details", "page1"),
    DETAILS_NEXT_PAGE("details", "next_page"),
    DETAILS_PREV_PAGE("details", "prev_page"),

    //Cloud
    CLOUD_SETTINGS("wildfire_gender.cloud_settings"),
    CLOUD_STATUS("cloud", "status"),
    CLOUD_STATUS_LOG("cloud", "status_log"),
    CLOUD_TOOLTIP("cloud", "tooltip"),
    CLOUD_AUTOMATIC("cloud", "automatic"),
    CLOUD_AUTOMATIC_TOOLTIP_1("cloud", "automatic.tooltip.line1"),
    CLOUD_AUTOMATIC_TOOLTIP_2("cloud", "automatic.tooltip.line2"),
    CLOUD_SYNC("cloud", "sync"),
    CLOUD_SYNCING("cloud", "syncing"),
    CLOUD_SYNCING_SUCCESS("cloud", "syncing.success"),
    CLOUD_SYNCING_FAIL("cloud", "syncing.fail"),
    CLOUD_UNAVAILABLE_INVALID_ACC("cloud", "unavailable.invalid_account"),
    CLOUD_UNAVAILABLE_SERVER_OFFLINE("cloud", "unavailable.offline_server"),
    //Sync Logging
    SYNC_LOG_AUTHENTICATING("sync_log", "authenticating"),
    SYNC_LOG_AUTHENTICATION_FAILED("sync_log", "authentication_failed"),
    SYNC_LOG_AUTHENTICATION_SUCCESS("sync_log", "authentication_success"),
    SYNC_LOG_REAUTHENTICATING("sync_log", "reauthenticating"),
    SYNC_LOG_ATTEMPTING_SYNC("sync_log", "attempting_sync"),
    SYNC_LOG_SYNC_SUCCESS("sync_log", "sync_success"),
    SYNC_LOG_SYNC_TOO_FREQUENTLY("sync_log", "sync_too_frequently"),
    SYNC_LOG_FAILED_TO_SYNC_DATA("sync_log", "failed_to_sync_data"),
    SYNC_LOG_SYNC_TO_CLOUD("sync_log", "sync_to_cloud"),
    SYNC_LOG_GET_SINGLE_PROFILE("sync_log", "get_single_profile"),
    SYNC_LOG_GET_MULTIPLE_PROFILES("sync_log", "get_multiple_profiles"),
    ;

    private final String key;

    WildfireLang(String type, String path) {
        //TODO - 1.21: Re-evaluate this
        //this(Util.makeDescriptionId(type, WildfireGender.rl(path)));
        this(WildfireGender.MODID + "." + type + "." + path);
    }

    WildfireLang(String key) {
        this.key = key;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }
}

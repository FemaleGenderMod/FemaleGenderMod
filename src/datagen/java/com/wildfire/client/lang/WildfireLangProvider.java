package com.wildfire.client.lang;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import net.minecraft.data.PackOutput;

public class WildfireLangProvider extends BaseLanguageProvider {

    public WildfireLangProvider(PackOutput output) {
        super(output, WildfireGender.MODID, "Female Gender Mod");
    }

    @Override
    protected void addTranslations() {
        //TODO - 1.21: Re-evaluate these translation keys to validate they are no longer used
        //	"toast.wildfire_gender.get_started": "Press %s to get started!",
        //	"wildfire_gender.player_list.title": "Female Gender Mod",
        //	"wildfire_gender.player_list.settings_button": "Settings",
        //	"wildfire_gender.player_list.sync_status": "Sync Status",
        //	"wildfire_gender.player_list.state.loading": "Loading Data...",
        //	"wildfire_gender.player_list.state.synced": "Synced Player",
        //	"wildfire_gender.player_list.bounce_multiplier": "Bounce Multiplier: %sx",
        //	"wildfire_gender.player_list.breast_momentum": "Breast Momentum: %s%%",
        //	"wildfire_gender.player_list.female_sounds": "Female Sounds: %s",
        //	"wildfire_gender.label.on": "On"

        addPackData(WildfireLang.MOD_NAME, WildfireLang.PACK_DESCRIPTION);
        addModInfo("Adds extra customization options to the player model by adding breasts for a more feminine appearance.");

        add(WildfireLang.KEY_CATEGORY, "Female Gender Mod");
        add(WildfireLang.KEY_CONFIG, "Female Gender Menu");
        add(WildfireLang.KEY_TOGGLE, "Toggle Breast Rendering");

        add(WildfireLang.HURT_SOUND_SUBTITLE, "Female Player Hurt");

        add(WildfireLang.ARMOR_TOOLTIP, "+%$1s Breast Support");

        add(WildfireLang.PLAYER_LIST_MODE, "Show Synced Players: %$1s");
        add(WildfireLang.PLAYER_LIST_MODE_MOD_UI, "This screen");
        add(WildfireLang.PLAYER_LIST_MODE_MOD_UI_TOOLTIP, "The synced player list will only show while in this menu");
        add(WildfireLang.PLAYER_LIST_MODE_TAB_LIST, "Player list");
        add(WildfireLang.PLAYER_LIST_MODE_TAB_LIST_TOOLTIP, "The synced player list will show while in this menu or by pressing %$1s");
        add(WildfireLang.PLAYER_LIST_MODE_ALWAYS, "Always");
        add(WildfireLang.PLAYER_LIST_MODE_ALWAYS_TOOLTIP, "The synced player list will always show");

        add(WildfireLang.OFF, "Off");
        add(WildfireLang.ENABLED, "Enabled");
        add(WildfireLang.DISABLED, "Disabled");
        add(WildfireLang.GENDER, "Gender");
        add(WildfireLang.FEMALE, "Female");
        add(WildfireLang.MALE, "Male");
        add(WildfireLang.OTHER, "Other");
        add(WildfireLang.LABEL_WITH_CREATOR, "You are playing on a server with the creator of this mod!");
        add(WildfireLang.LABEL_WITH_CONTRIBUTOR, "You are playing on a server with a contributor of this mod!");
        add(WildfireLang.LABEL_WITH_BOTH, "You are playing on a server with the creator and a contributor of this mod!");

        add(WildfireLang.TOOLTIP_BOUNCE_WARNING, "Setting 'Bounce Intensity' to a high value will look very unnatural!");
        add(WildfireLang.TOOLTIP_HIDE_IN_ARMOR, "Hide Breast Model When Wearing Armors");
        add(WildfireLang.TOOLTIP_HURT_SOUNDS, "Your character will play a female hurt sound when taking damage if your gender is set to either Female or Other");
        add(WildfireLang.TOOLTIP_OVERRIDE_PHYSICS_1, "Breast physics will no longer be reduced/suppressed by your equipped armor while enabled");
        add(WildfireLang.TOOLTIP_OVERRIDE_PHYSICS_2, "This is intended for use with resource packs that hide armor, or any similar minimal armor packs");
        add(WildfireLang.TOOLTIP_HOLIDAY_THEMES_1, "When enabled, this feature automatically showcases cosmetics like Santa hats and other holiday-themed items during their respective holidays.");

        add(WildfireLang.NAME_TAG_CREATOR, "Female Gender Mod Creator");
        add(WildfireLang.NAME_TAG_CONTRIBUTOR, "Female Gender Mod Contributor");

        add(WildfireLang.HOLIDAY_THEMES, "Holiday Themes: %$1s");

        add(WildfireLang.BREAST_CUSTOMIZATION_DUAL_PHYSICS, "Dual-Physics: %$1s");
        add(WildfireLang.BREAST_CUSTOMIZATION_TAB_CUSTOMIZATION, "Customization");
        add(WildfireLang.BREAST_CUSTOMIZATION_TAB_PHYSICS, "Breast Physics");
        add(WildfireLang.BREAST_CUSTOMIZATION_TAB_MISC, "Miscellaneous");

        add(WildfireLang.WARDROBE, "Female Gender Mod");
        add(WildfireLang.WARDROBE_PLAYERS_USING, "Players Using the Mod:");
        add(WildfireLang.WARDROBE_SLIDER_BREAST_SIZE, "Breast Size: %$1s%%");
        add(WildfireLang.WARDROBE_SLIDER_SEPARATION, "Separation: %$1s");
        add(WildfireLang.WARDROBE_SLIDER_HEIGHT, "Height: %$1s");
        add(WildfireLang.WARDROBE_SLIDER_DEPTH, "Depth: %$1s");
        add(WildfireLang.WARDROBE_SLIDER_ROTATION, "Rotation: %$1s°");

        add(WildfireLang.CHAR_SETTINGS, "Character Settings OLD");
        add(WildfireLang.CHAR_SETTING_PHYSICS, "Breast Physics: %$1s");
        add(WildfireLang.CHAR_SETTING_HIDE_IN_ARMOR, "Hide In Armor: %$1s");
        add(WildfireLang.CHAR_SETTING_OVERRIDE_PHYSICS, "Armor Physics: %$1s");
        add(WildfireLang.CHAR_SETTING_HURT_SOUNDS, "Female Hurt Sounds: %$1s");
        add(WildfireLang.CHAR_SETTING_SHOW_ARMOR_STAT, "Show Armor Tooltip: %$1s");

        add(WildfireLang.CANCER_AWARENESS, "Hey, it's Breast Cancer Awareness Month!");
        add(WildfireLang.APPEARANCE_SETTINGS, "Character Personalization");

        add(WildfireLang.SLIDER_BOUNCE, "Intensity: %$1s%%");
        add(WildfireLang.SLIDER_FLOPPY, "Momentum: %$1s%%");
        add(WildfireLang.SLIDER_VOICE_PITCH, "Pitch: %$1s%%");

        //Presets
        add(WildfireLang.PRESETS_ADD_NEW, "Add New...");
        add(WildfireLang.PRESETS_DELETE, "Delete");

        //First time setup
        add(WildfireLang.FIRST_TIME_SETUP, "Welcome to the Female Gender Mod!");
        add(WildfireLang.FIRST_TIME_SETUP_DESCRIPTION, "Would you like to enable cloud server syncing for your gender settings? This feature allows other players to view your customized gender appearance, even if the server doesn't have the mod installed.");
        add(WildfireLang.FIRST_TIME_SETUP_NOTICE, "You can always change this setting later in the mod menu.");
        add(WildfireLang.FIRST_TIME_SETUP_ENABLE_CLOUD_SYNC, "Enable Cloud Syncing");
        add(WildfireLang.FIRST_TIME_SETUP_DISABLE_CLOUD_SYNC, "Disable Cloud Syncing");

        add(WildfireLang.CLOUD_DETAILS, "Cloud Sync Server Information");
        add(WildfireLang.CLOUD_DETAILS_PAGE1, "Page 1");
        add(WildfireLang.DETAILS_NEXT_PAGE, "Next Page");
        add(WildfireLang.DETAILS_PREV_PAGE, "Prev Page");

        //Cloud
        add(WildfireLang.CLOUD_SETTINGS, "Cloud Sync Server Settings");
        add(WildfireLang.CLOUD_STATUS, "Cloud Sync: %$1s");
        add(WildfireLang.CLOUD_STATUS_LOG, "Status Log");
        add(WildfireLang.CLOUD_TOOLTIP, "Cloud Sync");
        add(WildfireLang.CLOUD_AUTOMATIC, "Automatic Sync: %$1s");
        add(WildfireLang.CLOUD_AUTOMATIC_TOOLTIP_1, "While enabled, your config will automatically be synced to the cloud after making any changes.");
        add(WildfireLang.CLOUD_AUTOMATIC_TOOLTIP_2, "You can still sync manually with the button below if this is disabled.");
        add(WildfireLang.CLOUD_SYNC, "Sync Now");
        add(WildfireLang.CLOUD_SYNCING, "Syncing...");
        add(WildfireLang.CLOUD_SYNCING_SUCCESS, "Synced");
        add(WildfireLang.CLOUD_SYNCING_FAIL, "Sync Failed");
        add(WildfireLang.CLOUD_UNAVAILABLE_INVALID_ACC, "Cloud syncing is unavailable as you aren't currently logged into a valid Minecraft account");
        add(WildfireLang.CLOUD_UNAVAILABLE_SERVER_OFFLINE, "Cloud syncing is unavailable as the server you're connected to is in offline mode");
        //Sync Logging
        add(WildfireLang.SYNC_LOG_AUTHENTICATING, "Authenticating Account...");
        add(WildfireLang.SYNC_LOG_AUTHENTICATION_FAILED, "Failed Authentication.");
        add(WildfireLang.SYNC_LOG_AUTHENTICATION_SUCCESS, "Authentication Successful.");
        add(WildfireLang.SYNC_LOG_REAUTHENTICATING, "Re-Authenticating Account...");
        add(WildfireLang.SYNC_LOG_ATTEMPTING_SYNC, "Syncing Profile...");
        add(WildfireLang.SYNC_LOG_SYNC_SUCCESS, "Sync Successful.");
        add(WildfireLang.SYNC_LOG_SYNC_TOO_FREQUENTLY, "Sync Rate Limited.");
        add(WildfireLang.SYNC_LOG_FAILED_TO_SYNC_DATA, "Failed to Sync Data.");
        add(WildfireLang.SYNC_LOG_SYNC_TO_CLOUD, "Syncing Data to Cloud...");
        add(WildfireLang.SYNC_LOG_GET_SINGLE_PROFILE, "Retrieving Profile...");
        add(WildfireLang.SYNC_LOG_GET_MULTIPLE_PROFILES, "Retrieving Batch of Profiles...");
    }
}
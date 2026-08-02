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

package com.wildfire.datagen.lang;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class WildfireLangProvider extends FabricLanguageProvider {

    private final ConvertibleLanguageProvider[] altProviders;

    public WildfireLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        altProviders = new ConvertibleLanguageProvider[]{
            new UpsideDownLanguageProvider(),
            new NonAmericanLanguageProvider("en_au"),
            new NonAmericanLanguageProvider("en_gb"),
            new NonAmericanLanguageProvider("en_ca")
        };
    }

    private void add(TranslationBuilder builder, String key, String value) {
        if (value.contains("%s")) {
            throw new IllegalArgumentException("Values containing substitutions should use explicit numbered indices: " + key + " - " + value);
        }
        builder.add(key, value);
        if (altProviders.length > 0) {
            List<FormatSplitter.Component> splitEnglish = FormatSplitter.split(value);
            for (ConvertibleLanguageProvider provider : altProviders) {
                provider.convert(key, value, splitEnglish);
            }
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return super.run(cache).thenCompose(_ -> {
            CompletableFuture<?>[] futures = new CompletableFuture[altProviders.length];
            for (int i = 0; i < altProviders.length; i++) {
                futures[i] = altProviders[i].save(cache, this::getLangFilePath);
            }
            return CompletableFuture.allOf(futures);
        });
    }

    private void add(TranslationBuilder builder, WildfireLang lang, String translation) {
        add(builder, lang.getTranslationKey(), translation);
    }

    private void addCompact(TranslationBuilder builder, WildfireLang lang, String translation, String shortTranslation) {
        add(builder, lang, translation);
        add(builder, lang.getTranslationKey() + ".short", shortTranslation);
    }

    private void addCommand(TranslationBuilder builder, WildfireLang lang, String usage, String description) {
        add(builder, lang, usage);
        add(builder, lang.getTranslationKey() + ".description", description);
    }

    private void add(TranslationBuilder builder, WildfireLang lang, String... translations) {
        String translationKey = lang.getTranslationKey();
        if (translations.length == 0) {
            throw new IllegalArgumentException("No translations provided for: " + translationKey);
        } else if (translations.length == 1) {
            WildfireGender.LOGGER.warn("Explicitly creating array for translating: '{}', falling back to adding a direct translation", translationKey);
        }
        int index = 1;
        for (String translation : translations) {
            add(builder, translationKey + ".line" + index++, translation);
        }
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder builder) {
        add(builder, WildfireLang.ARMOR_TOOLTIP, "+%1$s Breast Support");

        add(builder, WildfireLang.UV_EDITOR, "Breast Texture Editor");
        add(builder, WildfireLang.UV_EDITOR_RESET_ALL, "Reset All to Default UVs");
        add(builder, WildfireLang.UV_EDITOR_RESET, "Reset to Default UVs");
        add(builder, WildfireLang.UV_EDITOR_NO_FACE, "Select a texture face on the left to edit it.");
        add(builder, WildfireLang.UV_EDITOR_LB, "Left");
        add(builder, WildfireLang.UV_EDITOR_RB, "Right");
        add(builder, WildfireLang.UV_EDITOR_LB_OVERLAY, "Left");
        add(builder, WildfireLang.UV_EDITOR_RB_OVERLAY, "Right");

        add(builder, WildfireLang.UV_EDITOR_BODY_LAYER, "Body Layer");
        add(builder, WildfireLang.UV_EDITOR_JACKET_LAYER, "Jacket Layer");

        add(builder, WildfireLang.UV_EDITOR_X_POS, "X-Pos");
        add(builder, WildfireLang.UV_EDITOR_Y_POS, "Y-Pos");
        add(builder, WildfireLang.UV_EDITOR_WIDTH, "Width");
        add(builder, WildfireLang.UV_EDITOR_HEIGHT, "Height");
        add(builder, WildfireLang.UV_EDITOR_INCREMENT, "Hold SHIFT for +10", "SHIFT+CTRL for +20");
        add(builder, WildfireLang.UV_EDITOR_ADD, "Add");
        add(builder, WildfireLang.UV_EDITOR_REMOVE, "Remove");

        add(builder, WildfireLang.UV_EDITOR_FACE_INNER, "Inner Face");
        add(builder, WildfireLang.UV_EDITOR_FACE_OUTER, "Outer Face");
        add(builder, WildfireLang.UV_EDITOR_FACE_TOP, "Top Face");
        add(builder, WildfireLang.UV_EDITOR_FACE_BOTTOM, "Bottom Face");
        add(builder, WildfireLang.UV_EDITOR_FACE_FRONT, "Front Face");

        add(builder, WildfireLang.UV_SELECTED_DIRECTION, "%1$s (%2$s)");
        add(builder, WildfireLang.UV_QUAD, "[%1$s, %2$s, %3$s, %4$s]");

        add(builder, WildfireLang.CREDITS_TITLE, "Mod Credits");
        add(builder, WildfireLang.CREDITS_DESCRIPTION, "This is a list of the awesome people who have made this mod possible!");
        add(builder, WildfireLang.CREDITS_GENERAL, "General");
        add(builder, WildfireLang.CREDITS_TRANSLATORS, "Translators");

        add(builder, WildfireLang.PLAYER_LIST_TITLE, "Female Gender Mod");
        add(builder, WildfireLang.PLAYER_LIST_SETTINGS, "Settings");
        add(builder, WildfireLang.PLAYER_LIST_SYNC_STATUS, "Sync Status");
        add(builder, WildfireLang.PLAYER_LIST_LOADING, "Loading Data...");
        add(builder, WildfireLang.PLAYER_LIST_SYNCED, "Synced Player");
        add(builder, WildfireLang.PLAYER_LIST_BOUNCE_MULTIPLIER, "Bounce Multiplier: %1$sx");
        add(builder, WildfireLang.PLAYER_LIST_BREAST_MOMENTUM, "Breast Momentum: %1$s%%");
        add(builder, WildfireLang.PLAYER_LIST_SOUNDS, "Female Sounds: %1$s");

        add(builder, WildfireLang.PLAYER_LIST_MODE, "Show Synced Players: %1$s");
        add(builder, WildfireLang.PLAYER_LIST_MODE_MOD_UI, "This screen");
        add(builder, WildfireLang.PLAYER_LIST_MODE_MOD_UI_TOOLTIP, "The synced player list will only show while in this menu");
        add(builder, WildfireLang.PLAYER_LIST_MODE_TAB_LIST, "Player list");
        add(builder, WildfireLang.PLAYER_LIST_MODE_TAB_LIST_TOOLTIP, "The synced player list will show while in this menu or by pressing %1$s");
        add(builder, WildfireLang.PLAYER_LIST_MODE_ALWAYS, "Always");
        add(builder, WildfireLang.PLAYER_LIST_MODE_ALWAYS_TOOLTIP, "The synced player list will always show");

        add(builder, WildfireLang.CUSTOMIZATION_TAB_CUSTOMIZATION, "Customization");
        add(builder, WildfireLang.CUSTOMIZATION_TAB_PHYSICS, "Breast Physics");
        add(builder, WildfireLang.CUSTOMIZATION_TAB_MISC, "Miscellaneous");
        add(builder, WildfireLang.CUSTOMIZATION_PRESET_NEW, "Add New...");
        add(builder, WildfireLang.CUSTOMIZATION_PRESET_DELETE, "Delete");
        add(builder, WildfireLang.CUSTOMIZATION_DUAL_PHYSICS, "Dual-Physics: %1$s");

        add(builder, WildfireLang.WARDROBE_TITLE, "Female Gender Mod");
        add(builder, WildfireLang.WARDROBE_PLAYERS_USING, "Synced Players:");
        add(builder, WildfireLang.WARDROBE_SLIDER_BREAST_SIZE, "Breast Size: %1$s%%");
        add(builder, WildfireLang.WARDROBE_SLIDER_SEPARATION, "Separation: %1$s");
        add(builder, WildfireLang.WARDROBE_SLIDER_HEIGHT, "Height: %1$s");
        add(builder, WildfireLang.WARDROBE_SLIDER_DEPTH, "Depth: %1$s");
        add(builder, WildfireLang.WARDROBE_SLIDER_ROTATION, "Rotation: %1$s°");
        add(builder, WildfireLang.WARDROBE_SLIDER_PITCH, "Pitch: %1$s%%");
        add(builder, WildfireLang.WARDROBE_SLIDER_BOUNCE, "Intensity: %1$s%%");
        add(builder, WildfireLang.WARDROBE_SLIDER_FLOPPY, "Momentum: %1$s%%");

        add(builder, WildfireLang.APPEARANCE_SETTINGS_TITLE, "Character Personalization");
        add(builder, WildfireLang.CHAR_SETTINGS_PHYSICS, "Breast Physics: %1$s");
        add(builder, WildfireLang.CHAR_SETTINGS_JUMP, "Jump");
        add(builder, WildfireLang.CHAR_SETTINGS_JUMPING, "Stop Jumping");

        add(builder, WildfireLang.CHAR_SETTINGS_HIDE_IN_ARMOR, "Hide In Armor: %1$s");
        add(builder, WildfireLang.CHAR_SETTINGS_ARMOR_STAT, "Show Armor Tooltip: %1$s");
        add(builder, WildfireLang.CHAR_SETTINGS_HURT_SOUNDS, "Female Hurt Sounds: %1$s");
        add(builder, WildfireLang.CHAR_SETTINGS_HURT_SOUNDS_TOOLTIP, "Your character will play a female hurt sound when taking damage if your gender is set to either Female or Other ");
        add(builder, WildfireLang.CHAR_SETTINGS_OVERRIDE_PHYSICS, "Armor Physics: %1$s");
        add(builder, WildfireLang.CHAR_SETTINGS_OVERRIDE_PHYSICS_TOOLTIP,
            "Breast physics will no longer be reduced/suppressed by any worn armor while enabled; this is primarily intended for use with resource packs that hide armor.",
            "This affects how you see other players and yourself, but does not affect how others see you."
        );

        add(builder, WildfireLang.LABEL_GENDER, "Gender");
        add(builder, WildfireLang.LABEL_FEMALE, "Female");
        add(builder, WildfireLang.LABEL_MALE, "Male");
        add(builder, WildfireLang.LABEL_OTHER, "Other");

        add(builder, WildfireLang.LABEL_ENABLED, "Enabled");
        add(builder, WildfireLang.LABEL_DISABLED, "Disabled");
        add(builder, WildfireLang.LABEL_ON, "On");
        add(builder, WildfireLang.LABEL_OFF, "Off");
        add(builder, WildfireLang.LABEL_YES, "Yes");
        add(builder, WildfireLang.LABEL_NO, "No");
        add(builder, WildfireLang.LABEL_WITH_CREATOR, "You are playing on a server with the creator of this mod!");
        add(builder, WildfireLang.LABEL_WITH_CONTRIBUTOR, "You are playing on a server with a contributor of this mod!");
        add(builder, WildfireLang.LABEL_WITH_BOTH, "You are playing on a server with the creator and a contributor of this mod!");

        add(builder, WildfireLang.CANCER_AWARENESS_TITLE, "Hey, it's Breast Cancer Awareness Month!");

        add(builder, WildfireLang.FIRST_TIME_TITLE, "Welcome to the Female Gender Mod!");
        add(builder, WildfireLang.FIRST_TIME_DESCRIPTION, "Would you like to enable cloud server syncing for your gender settings? This feature allows other players to view your customized gender appearance, even if the server doesn't have the mod installed.");
        add(builder, WildfireLang.FIRST_TIME_NOTICE, "You can always change this setting later in the mod menu.");
        add(builder, WildfireLang.FIRST_TIME_ENABLE, "Enable Cloud Syncing");
        add(builder, WildfireLang.FIRST_TIME_ENABLE_TOOLTIP,
            "With cloud sync enabled, your configuration will be stored and provided to other players even if you don't currently have the mod installed.",
            "You can delete your cloud sync profile at any point in the Cloud Sync settings menu after turning it off."
        );
        add(builder, WildfireLang.FIRST_TIME_DISABLE, "Disable Cloud Syncing");

        add(builder, WildfireLang.CLOUD_SETTINGS, "Cloud Sync Server Settings");
        add(builder, WildfireLang.CLOUD_TOOLTIP, "Cloud Sync");
        add(builder, WildfireLang.CLOUD_UNAVAILABLE_INVALID_ACC, "Cloud syncing is unavailable as you aren't currently logged into a valid Minecraft account");
        add(builder, WildfireLang.CLOUD_UNAVAILABLE_OFFLINE_SERVER, "Cloud syncing is unavailable as the server you're connected to is in offline mode");
        add(builder, WildfireLang.CLOUD_STATUS, "Cloud Sync: %1$s");
        add(builder, WildfireLang.CLOUD_AUTOMATIC, "Automatic Sync: %1$s");
        add(builder, WildfireLang.CLOUD_AUTOMATIC_TOOLTIP,
            "While enabled, your config will automatically be synced to the cloud after making any changes.",
            "You can still sync manually with the button below if this is disabled."
        );

        add(builder, WildfireLang.CLOUD_SYNC, "Sync Now");
        add(builder, WildfireLang.CLOUD_SYNCING, "Syncing...");
        add(builder, WildfireLang.CLOUD_SYNCING_SUCCESS, "Synced");
        add(builder, WildfireLang.CLOUD_SYNCING_FAIL, "Sync Failed");

        add(builder, WildfireLang.CLOUD_DELETE, "Delete");
        add(builder, WildfireLang.CLOUD_DELETED, "Deleted");
        add(builder, WildfireLang.CLOUD_DELETE_FAILED, "Delete Failed");

        add(builder, WildfireLang.CLOUD_STATUS_LOG, "Status Log");

        add(builder, WildfireLang.CLOUD_DETAILS, "Cloud Sync Server Information");
        add(builder, WildfireLang.DETAILS_NEXT_PAGE, "Next Page");
        add(builder, WildfireLang.DETAILS_PREV_PAGE, "Prev Page");
        add(builder, WildfireLang.DETAILS_BACK, "Go Back");

        add(builder, WildfireLang.SYNC_LOG_AUTH_MOJANG, "Authenticating with Mojang...");
        add(builder, WildfireLang.SYNC_LOG_AUTH_SYNC, "Authenticating with cloud sync...");
        add(builder, WildfireLang.SYNC_LOG_AUTH_FAILED, "Authentication failed.");
        add(builder, WildfireLang.SYNC_LOG_REAUTH, "Re-authenticating...");

        add(builder, WildfireLang.SYNC_LOG_FAILED, "Failed to sync data.");
        add(builder, WildfireLang.SYNC_LOG_START, "Syncing profile...");
        add(builder, WildfireLang.SYNC_LOG_SUCCESS, "Sync successful.");
        add(builder, WildfireLang.SYNC_LOG_TOO_FREQUENT, "Sync rate limited.");

        add(builder, WildfireLang.SYNC_LOG_PROFILE_DELETED, "Deleted cloud sync profile.");
        add(builder, WildfireLang.SYNC_LOG_PROFILE_DELETION_FAILED, "Failed to delete cloud sync profile.");
        add(builder, WildfireLang.SYNC_LOG_NO_PROFILE, "No cloud sync profile found.");

        add(builder, WildfireLang.SYNC_LOG_SINGLE_PROFILE, "Retrieving profile...");
        add(builder, WildfireLang.SYNC_LOG_MULTIPLE_PROFILES, "Retrieving batch of profiles...");

        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_MOD_CREATOR, "Female Gender Mod Creator", "Mod Creator");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_FABRIC_MAINTAINER, "Female Gender Mod Maintainer", "Maintainer (Fabric)");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_NEO_MAINTAINER, "Female Gender Mod Maintainer", "Maintainer (NeoForge)");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_DEVELOPER, "Female Gender Mod Contributor", "Programmer");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_CI_MAINTAINER, "Female Gender Mod Contributor", "CI/CD Maintainer");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_TRANSLATOR, "Female Gender Mod Translator", "Mod Translator");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_MASCOT, "Female Gender Mod Mascot", "Mod Mascot");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_FEMALE_VOICE_ACTOR, "Keira Emberlyn Voice Actress", "Voice Actress");
        addCompact(builder, WildfireLang.CONTRIBUTOR_ROLE_GENERIC, "Female Gender Mod Contributor", "Contributor");

        add(builder, WildfireLang.GENERIC_BRACKETS, "[%1$s]");
        add(builder, WildfireLang.GENERIC_ELLIPSIS, "...");
        add(builder, WildfireLang.GENERIC_ELLIPSIS_SUFFIX, "%1$s...");
        add(builder, WildfireLang.GENERIC_SPACE, "%1$s %2$s");
        add(builder, WildfireLang.GENERIC_DASH_EXPLANATION, "%1$s - %2$s");

        add(builder, WildfireLang.KEIRA, "Keira Emberlyn:");
        add(builder, WildfireLang.HOLIDAY_THEMES, "Holiday Themes: %1$s");
        //TODO: This is like this to support the legacy translation key of one line. But maybe we should change it?
        add(builder, WildfireLang.HOLIDAY_THEMES_TOOLTIP, new String[]{"When enabled, this feature automatically showcases cosmetics like Santa hats and other holiday-themed items during their respective holidays."});

        add(builder, WildfireLang.NOT_IN_WORLD_TITLE, "Unavailable in Main Menu");
        add(builder, WildfireLang.NOT_IN_WORLD, "You need to be in a world to configure your gender settings.");

        add(builder, WildfireLang.HURT_SOUND_SUBTITLE, "Female Player Hurt");

        add(builder, WildfireLang.KEY_CATEGORY, "Female Gender Mod");
        add(builder, WildfireLang.KEY_CONFIG, "Female Gender Menu");
        add(builder, WildfireLang.KEY_TOGGLE, "Toggle Breast Rendering");
        add(builder, WildfireLang.TOAST_GET_STARTED, "Press %1$s to get started!");

        add(builder, WildfireLang.DEBUG_COMMAND, "Debug Commands:");
        addCommand(builder, WildfireLang.COMMAND_INVALIDATE_CACHE, "invalidatecache", "Clears the player & entity caches");
        add(builder, WildfireLang.COMMAND_INVALIDATE_CACHE_SUCCESS, "Cache has been invalidated!");
        addCommand(builder, WildfireLang.COMMAND_TARGET, "target", "Show debug info for entity you are looking at");
        addCommand(builder, WildfireLang.COMMAND_CACHE, "cache [allPlayers] [showEntities]", "Display cached entities/players");
        addCommand(builder, WildfireLang.COMMAND_FIRST_TIME, "firsttime", "Display the first time setup screen");
        addCommand(builder, WildfireLang.COMMAND_SYNC_VERBOSITY, "syncverbosity [level]", "Change how verbose the sync log is");

        add(builder, WildfireLang.SINGLE_PLAYER_COMMAND, "Singleplayer Commands:");
        addCommand(builder, WildfireLang.COMMAND_ARMOR_STAND, "armorstand", "Spawns an armor stand with armor copying your breast settings pre-equipped");
        add(builder, WildfireLang.COMMAND_ARMOR_STAND_NO_COMPONENT, "Returned breast data component was null; do you have Hide in Armor on?");
        addCommand(builder, WildfireLang.COMMAND_TRIM, "trim [glint]", "Equips a chestplate with a trim pre-applied onto yourself");

        add(builder, WildfireLang.COMMAND_LOOKING_AT, "Looking at: %1$s");
        add(builder, WildfireLang.COMMAND_LOOKING_AT_NONE, "No entity in sight.");
        add(builder, WildfireLang.COMMAND_LOOKING_AT_UUID, "UUID: %1$s");
        add(builder, WildfireLang.COMMAND_LOOKING_AT_TYPE, "Type: %1$s");
        add(builder, WildfireLang.COMMAND_LOOKING_AT_CLASS, "Class: %1$s");
        add(builder, WildfireLang.COMMAND_LOOKING_AT_RENDERER, "Renderer: %1$s");

        add(builder, WildfireLang.COMMAND_LOG_LEVEL, "Log level set to: %1$s");
        add(builder, WildfireLang.COMMAND_SYNCED_PLAYERS, "Synced Players (%1$s):");
        add(builder, WildfireLang.COMMAND_ENTITIES, "Entities (Class: %1$s):");

        // intentionally omitted as they aren't used anywhere:
    }
}

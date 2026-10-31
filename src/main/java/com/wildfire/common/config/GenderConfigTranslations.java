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

package com.wildfire.common.config;

import com.wildfire.common.WildfireGender;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public enum GenderConfigTranslations implements ConfigTranslation {
    CLIENT_FIRST_TIME_LOAD("client.first_time_load", "First Time Setup", "Should the first time setup screen be displayed"),
    CLIENT_SHOW_TOAST("client.show_toast", "Show Toast", "Should a toast be displayed directing how to get started with the mod"),
    CLIENT_ARMOR_STAT("client.item_tooltip.armor_stat", "Item Attribute Tooltip", "Should the Armor Tooltip be displayed with vanilla's other item attributes"),
    CLIENT_PLAYER_LIST_MODE("client.player_list_mode", "Synced Player List Mode", "Determines what situations the synced player list should be displayed"),
    CLIENT_HIDE_OWN_CONTRIBUTOR_TAG("client.hide_own_contributor_tag", "Hide Own Contributor Tag", "If you are a contributor, prevents your name tag from displaying you contributed to the mod"),

    //Cloud Sync
    //TODO: How does this look as a section title
    CLIENT_CLOUD_SYNC("client.cloud_sync", "Cloud Sync", "Settings for configuring Cloud Sync", "Edit Cloud Sync Settings"),
    CLIENT_CLOUD_SYNC_ENABLED("client.cloud_sync.enabled", "Enabled", "Should settings be synced with the cloud"),
    CLIENT_CLOUD_SYNC_AUTOMATIC("client.cloud_sync.automatic", "Automatic Sync", "Should setting updates be automatically synced to the cloud. Has no effect if \"Enabled\" is false"),
    CLIENT_CLOUD_SYNC_SERVER("client.cloud_sync.server", "Server", "The cloud sync server to connect to, leave empty for the default"),
    CLIENT_CLOUD_SYNC_LOG_VERBOSITY("client.cloud_sync.log_verbosity", "Log Verbosity", "How verbose the cloud sync log should be when logging sync attempts and failures"),

    //Overrides
    CLIENT_OVERRIDE("client.override", "Client Side Overrides", "Settings to override how the mod behaves", true),
    CLIENT_OVERRIDE_ARMOR_PHYSICS("client.override.armor_physics", "Armor Physics",
        "Breast physics will no longer be reduced/suppressed by any worn armor while enabled; this is primarily intended for use with resource packs that hide armor. "
        + "This affects how you see other players and yourself, but does not affect how others see you."),
    CLIENT_OVERRIDE_DISABLE_RENDERING("client.override.disable.rendering", "Disable Rendering", "Disables all rendering related to the mod (including in gender menus)"),
    CLIENT_OVERRIDE_DISABLE_SOUND_REPLACEMENT("client.override.disable.sound_replacement", "Disable Sound Replacement", "Disable replacing sounds of any player that has female variants selected"),
    ;

    private final String key;
    private final String title;
    private final String tooltip;
    @Nullable
    private final String button;

    GenderConfigTranslations(String path, String title, String tooltip) {
        this(path, title, tooltip, false);
    }

    GenderConfigTranslations(String path, String title, String tooltip, boolean isSection) {
        this(path, title, tooltip, ConfigTranslation.getSectionTitle(title, isSection));
    }

    GenderConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
        this.key = WildfireGender.id(path).toLanguageKey("configuration");
        this.title = title;
        this.tooltip = tooltip;
        this.button = button;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }

    @Nullable
    @Override
    public String button() {
        return button;
    }

    public Component tooltipComponent() {
        return Component.translatable(getTranslationKey() + ".tooltip");
    }
}

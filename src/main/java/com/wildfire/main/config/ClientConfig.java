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

import com.wildfire.main.config.enums.ShowPlayerListMode;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.config.types.BooleanConfigKey;
import com.wildfire.main.config.types.EnumConfigKey;
import com.wildfire.main.config.types.StringConfigKey;
import com.wildfire.main.config.types.TriStateConfigKey;

public class ClientConfig extends AbstractConfiguration {
    public static final ClientConfig INSTANCE = new ClientConfig();

    private ClientConfig() {
        super(".", "wildfire_gender");
    }

    // note: this option is not intended to be saved in any persistent manner
    public static boolean RENDER_BREASTS = true;

    public static final BooleanConfigKey ARMOR_PHYSICS_OVERRIDE = new BooleanConfigKey("armor_physics_override", false);

    public static final BooleanConfigKey FIRST_TIME_LOAD = new BooleanConfigKey("firstTimeLoad", true);
    public static final BooleanConfigKey CLOUD_SYNC_ENABLED = new BooleanConfigKey("cloud_sync", false);
    public static final BooleanConfigKey AUTOMATIC_CLOUD_SYNC = new BooleanConfigKey("sync_player_data", false);
    // see CloudSync#DEFAULT_CLOUD_URL for the actual default
    public static final StringConfigKey CLOUD_SERVER = new StringConfigKey("cloud_server", "");
    public static final EnumConfigKey<SyncVerbosity> SYNC_VERBOSITY = new EnumConfigKey<>("sync_log_verbosity", SyncVerbosity.DEFAULT, SyncVerbosity.BY_ID);

    public static final EnumConfigKey<ShowPlayerListMode> ALWAYS_SHOW_LIST = new EnumConfigKey<>("alwaysShowList", ShowPlayerListMode.MOD_UI_ONLY, ShowPlayerListMode.BY_ID);

    public static final BooleanConfigKey ARMOR_STAT = new BooleanConfigKey("armor_stat", true);

    public static final BooleanConfigKey HIDE_OWN_CONTRIBUTOR_TAG = new BooleanConfigKey("hide_own_contributor_nametag", false);
    // TODO support forcing certain cosmetics to always/never render if/when more are added than just the santa hat
    // this is intentionally omitted from defaults as this is primarily intended for use in debugging
    public static final TriStateConfigKey HOLIDAY_COSMETICS = new TriStateConfigKey("holiday_cosmetics");

    static {
        INSTANCE.setDefault(ARMOR_PHYSICS_OVERRIDE);
        INSTANCE.setDefault(FIRST_TIME_LOAD);
        INSTANCE.setDefault(CLOUD_SYNC_ENABLED);
        INSTANCE.setDefault(AUTOMATIC_CLOUD_SYNC);
        INSTANCE.setDefault(CLOUD_SERVER);
        INSTANCE.setDefault(SYNC_VERBOSITY);
        INSTANCE.setDefault(ALWAYS_SHOW_LIST);
        INSTANCE.setDefault(ARMOR_STAT);
        INSTANCE.setDefault(HIDE_OWN_CONTRIBUTOR_TAG);
        // HOLIDAY_COSMETICS is intentionally omitted
        if(!INSTANCE.exists()) {
            INSTANCE.save();
        }
    }
}

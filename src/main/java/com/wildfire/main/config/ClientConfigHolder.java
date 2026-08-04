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

import com.mojang.serialization.JsonOps;
import com.wildfire.main.config.enums.ShowPlayerListMode;
import com.wildfire.main.config.enums.SyncVerbosity;

public class ClientConfigHolder {

    public static final ClientConfigHolder INSTANCE = new ClientConfigHolder();

    private final AbstractConfiguration cfgFile = new AbstractConfiguration(".", "female_gender_mod");

    private ClientConfig config;

    private ClientConfigHolder() {
        //TODO - 26.2: Should this actually be using JsonOps.INSTANCE.empty() and then let the orDefault handle it all instead of trying to read from the config during construction
        config = ClientConfig.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();
        if (!cfgFile.exists()) {
            save();
        }
    }

    public ClientConfig config() {
        return INSTANCE.config;
    }

    public void load() {
        //TODO: If empty bc not able to read such as on server, should this try to load or skip?
        //TODO: If not success do we want to log it failed? Can it even fail? Given the fact everything has orDefault
        ClientConfig.CODEC.parse(JsonOps.INSTANCE, cfgFile.read()).ifSuccess(parsed -> config = parsed);
    }

    public void save() {
        cfgFile.save(ClientConfig.CODEC, config);
    }

    public static boolean armorPhysicsOverride() {
        return INSTANCE.config.armorPhysicsOverride;
    }

    public static boolean firstTimeLoad() {
        return INSTANCE.config.firstTimeLoad;
    }

    public static boolean cloudSyncEnabled() {
        return INSTANCE.config.cloudSyncEnabled;
    }

    public static boolean automaticCloudSync() {
        return INSTANCE.config.automaticCloudSync;
    }

    public static String cloudServer() {
        return INSTANCE.config.cloudServer;
    }

    public static SyncVerbosity syncVerbosity() {
        return INSTANCE.config.syncVerbosity;
    }

    public static ShowPlayerListMode alwaysShowList() {
        return INSTANCE.config.alwaysShowList;
    }

    public static boolean armorStat() {
        return INSTANCE.config.armorStat;
    }

    public static boolean hideOwnContributorTag() {
        return INSTANCE.config.hideOwnContributorTag;
    }

    public static boolean showToast() {
        return INSTANCE.config.showToast;
    }
}

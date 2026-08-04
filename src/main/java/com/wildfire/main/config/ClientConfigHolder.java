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

    private ClientConfigHolder() {
    }

    private static final AbstractConfiguration cfgFile = new AbstractConfiguration(".", "female_gender_mod");

    //TODO - 26.2: Should this actually be using JsonOps.INSTANCE.empty() and then let the orDefault handle it all instead of trying to read from the config during construction
    private static ClientConfig config = ClientConfig.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();
    static {
        if (!cfgFile.exists()) {
            save();
        }
    }

    public static ClientConfig config() {
        return config;
    }

    public static void load() {
        //TODO: If empty bc not able to read such as on server, should this try to load or skip?
        //TODO: If not success do we want to log it failed? Can it even fail? Given the fact everything has orDefault
        ClientConfig.CODEC.parse(JsonOps.INSTANCE, cfgFile.read()).ifSuccess(parsed -> config = parsed);
    }

    public static void save() {
        cfgFile.save(ClientConfig.CODEC, config);
    }

    public static boolean armorPhysicsOverride() {
        return config.armorPhysicsOverride.get();
    }

    public static boolean firstTimeLoad() {
        return config.firstTimeLoad.get();
    }

    public static boolean cloudSyncEnabled() {
        return config.cloudSyncEnabled.get();
    }

    public static boolean automaticCloudSync() {
        return config.automaticCloudSync.get();
    }

    public static String cloudServer() {
        return config.cloudServer.get();
    }

    public static SyncVerbosity syncVerbosity() {
        return config.syncVerbosity.get();
    }

    public static ShowPlayerListMode alwaysShowList() {
        return config.alwaysShowList.get();
    }

    public static boolean armorStat() {
        return config.armorStat.get();
    }

    public static boolean hideOwnContributorTag() {
        return config.hideOwnContributorTag.get();
    }

    public static boolean showToast() {
        return config.showToast.get();
    }
}

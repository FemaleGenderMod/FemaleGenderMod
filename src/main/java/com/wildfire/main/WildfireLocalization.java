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

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class WildfireLocalization {
    //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor' {
    public static final Component ENABLED = WildfireLang.LABEL_ENABLED.translateColored(TextColor.GREEN);
    public static final Component DISABLED = WildfireLang.LABEL_DISABLED.translateColored(TextColor.RED);
    //~}
    public static final Component OFF = WildfireLang.LABEL_OFF.translate();

    public static final Component SYNC_LOG_AUTHENTICATING_MOJANG = WildfireLang.SYNC_LOG_AUTH_MOJANG.translate();
    public static final Component SYNC_LOG_AUTHENTICATING_CLOUD_SYNC = WildfireLang.SYNC_LOG_AUTH_SYNC.translate();
    public static final Component SYNC_LOG_AUTHENTICATION_FAILED = WildfireLang.SYNC_LOG_AUTH_FAILED.translate();
    public static final Component SYNC_LOG_REAUTHENTICATING = WildfireLang.SYNC_LOG_REAUTH.translate();
    public static final Component SYNC_LOG_ATTEMPTING_SYNC = WildfireLang.SYNC_LOG_START.translate();
    public static final Component SYNC_LOG_SYNC_SUCCESS = WildfireLang.SYNC_LOG_SUCCESS.translate();
    public static final Component SYNC_LOG_SYNC_TOO_FREQUENTLY = WildfireLang.SYNC_LOG_TOO_FREQUENT.translate();
    public static final Component SYNC_LOG_FAILED_TO_SYNC_DATA = WildfireLang.SYNC_LOG_FAILED.translate();

    public static final Component SYNC_LOG_DELETED = WildfireLang.SYNC_LOG_PROFILE_DELETED.translate();
    public static final Component SYNC_LOG_DELETION_FAILED = WildfireLang.SYNC_LOG_PROFILE_DELETION_FAILED.translate();
    public static final Component SYNC_LOG_NO_PROFILE_TO_DELETE = WildfireLang.SYNC_LOG_NO_PROFILE.translate();

    public static final Component SYNC_LOG_GET_SINGLE_PROFILE = WildfireLang.SYNC_LOG_SINGLE_PROFILE.translate();
    public static final Component SYNC_LOG_GET_MULTIPLE_PROFILES = WildfireLang.SYNC_LOG_MULTIPLE_PROFILES.translate();
}

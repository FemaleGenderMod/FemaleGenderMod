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

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class WildfireLocalization {
	public static final Component ENABLED = Component.translatable("wildfire_gender.label.enabled").withStyle(ChatFormatting.GREEN);
	public static final Component DISABLED = Component.translatable("wildfire_gender.label.disabled").withStyle(ChatFormatting.RED);
	public static final Component OFF = Component.translatable("wildfire_gender.label.off");

	public static final Component SYNC_LOG_AUTHENTICATING_MOJANG = Component.translatable("wildfire_gender.sync_log.authenticating_mojang");
	public static final Component SYNC_LOG_AUTHENTICATING_CLOUD_SYNC = Component.translatable("wildfire_gender.sync_log.authenticating_sync");
	public static final Component SYNC_LOG_AUTHENTICATION_FAILED = Component.translatable("wildfire_gender.sync_log.authentication_failed");
	public static final Component SYNC_LOG_REAUTHENTICATING = Component.translatable("wildfire_gender.sync_log.reauthenticating");
	public static final Component SYNC_LOG_ATTEMPTING_SYNC = Component.translatable("wildfire_gender.sync_log.attempting_sync");
	public static final Component SYNC_LOG_SYNC_SUCCESS = Component.translatable("wildfire_gender.sync_log.sync_success");
	public static final Component SYNC_LOG_SYNC_TOO_FREQUENTLY = Component.translatable("wildfire_gender.sync_log.sync_too_frequently");
	public static final Component SYNC_LOG_FAILED_TO_SYNC_DATA = Component.translatable("wildfire_gender.sync_log.failed_to_sync_data");

	public static final Component SYNC_LOG_DELETED = Component.translatable("wildfire_gender.sync_log.data_deleted");
	public static final Component SYNC_LOG_DELETION_FAILED = Component.translatable("wildfire_gender.sync_log.data_deletion_failed");
	public static final Component SYNC_LOG_NO_PROFILE_TO_DELETE = Component.translatable("wildfire_gender.sync_log.no_data_to_delete");

	public static final Component SYNC_LOG_GET_SINGLE_PROFILE = Component.translatable("wildfire_gender.sync_log.get_single_profile");
	public static final Component SYNC_LOG_GET_MULTIPLE_PROFILES = Component.translatable("wildfire_gender.sync_log.get_multiple_profiles");
}

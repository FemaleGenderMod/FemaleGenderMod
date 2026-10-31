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

package com.wildfire.common.entities.players;

/// Enum denoting the current sync status of a [PlayerConfigHolder]
public enum SyncStatus {
    /// Indicates that the relevant configuration has had its data loaded from a file on disk.
    ///
    /// This is only applicable for configurations loaded on the logical client, as the
    /// logical server (dedicated or otherwise) does not read player data from disk.
    CACHED,

    /// Indicates that the relevant configuration has had its data loaded from a sync packet,
    /// or on the logical client a profile retrieved from [`the cloud sync server`][com.wildfire.client.cloud.CloudSync].
    SYNCED,

    /// Indicates that this configuration has an unknown sync state.
    ///
    /// This is the default sync state for new configuration instances.
    UNKNOWN,
}

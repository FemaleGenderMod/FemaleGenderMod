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

package com.wildfire.common.entities.avatars;

import com.google.common.base.Preconditions;
import com.wildfire.common.LoaderAgnostics;
import com.wildfire.common.networking.WildfireSync;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.decoration.Mannequin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class MannequinConfigHolder extends AbstractAvatarConfigHolder {

    public MannequinConfigHolder(final UUID uuid) {
        super(uuid, AvatarConfig.createDefault());
    }

    @Override
    public void updateFromPacket(final AvatarConfig config) {
        this.config = config;
    }

    public boolean canEdit(ServerPlayer player) {
        // TODO extend this to allow for some kind of proper permission API?
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /// Sets the config stored on this holder
    @ApiStatus.Internal
    public void setConfig(AvatarConfig config) {
        this.config = config;
    }

    /// Saves the current config to the provided [Mannequin] entity and syncs it to all nearby players
    ///
    /// @param mannequin The [Mannequin] entity to save this config on
    /// @param except Optional player to skip syncing this config to
    ///
    /// @throws IllegalArgumentException If the provided [Mannequin] is not on the logical server
    @ApiStatus.Internal
    public void sync(Mannequin mannequin, @Nullable ServerPlayer except) {
        Preconditions.checkArgument(!mannequin.level().isClientSide(), "This method can only be run on a mannequin on the logical server");
        LoaderAgnostics.INSTANCE.writeToMannequin(mannequin, config());
        WildfireSync.sendToAllClients(mannequin, this, except);
    }

    /// Convenience method that sets the config stored on this holder, saves it to the provided [Mannequin] entity,
    /// and syncs it to nearby players.
    ///
    /// @param mannequin The [Mannequin] entity to [save to][#sync(Mannequin, ServerPlayer)]
    /// @param config The new config to set on this holder
    /// @param except Optional player to skip syncing the new config to
    ///
    /// @see #setConfig(AvatarConfig)
    /// @see #sync(Mannequin, ServerPlayer)
    ///
    /// @throws IllegalArgumentException If the provided [Mannequin] is not on the logical server
    @ApiStatus.Internal
    public void setConfigAndSync(Mannequin mannequin, AvatarConfig config, @Nullable ServerPlayer except) {
        Preconditions.checkArgument(!mannequin.level().isClientSide(), "This method can only be run on a mannequin on the logical server");
        setConfig(config);
        sync(mannequin, except);
    }
}

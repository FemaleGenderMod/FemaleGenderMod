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

public class MannequinConfigHolder extends AbstractAvatarConfigHolder {

    public MannequinConfigHolder(final UUID uuid) {
        super(uuid, AvatarConfig.createDefault());
    }

    public boolean canEdit(ServerPlayer player) {
        // TODO extend this to allow for some kind of proper permission API?
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    @ApiStatus.Internal
    public void setConfig(AvatarConfig config) {
        this.config = config;
    }

    /// @apiNote Only call on the logical server
    @ApiStatus.Internal
    public void sync(Mannequin mannequin) {
        Preconditions.checkArgument(!mannequin.level().isClientSide(), "This method can only be run on a mannequin on the logical server");
        LoaderAgnostics.INSTANCE.writeToMannequin(mannequin, config());
        WildfireSync.sendToAllClients(mannequin, this);
    }

    /// @apiNote Only call on the logical server
    @ApiStatus.Internal
    public void setConfigAndSync(Mannequin mannequin, AvatarConfig config) {
        Preconditions.checkArgument(!mannequin.level().isClientSide(), "This method can only be run on a mannequin on the logical server");
        setConfig(config);
        sync(mannequin);
    }
}

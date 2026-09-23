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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.wildfire.client.ClientHelper;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.entities.EntityConfigHolder;
import com.wildfire.common.entities.Sounds;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Avatar;import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public abstract class AbstractAvatarConfigHolder extends EntityConfigHolder<AvatarConfig> {
    protected AbstractAvatarConfigHolder(final UUID uuid, final AvatarConfig config) {
        super(uuid, config);
    }

    /// Update player data from the provided [JsonObject]
    ///
    /// @param serialized The [JsonObject] to merge with the existing config for this player
    public void updateFromJson(JsonElement serialized) {
        //TODO: If not success do we want to log it failed? Can it even fail? Given the fact everything has orDefault
        AvatarConfig.CODEC.parse(JsonOps.INSTANCE, serialized).ifSuccess(parsed -> config = parsed);
    }

    public abstract void updateFromPacket(AvatarConfig config);

    /// Play the relevant mod hurt sound when a player takes damage
    ///
    /// @apiNote Only call this on the client side as sounds are only registered on the client.
    public void tryPlayHurtSound(Avatar player) {
        if(!sounds().hurt().get()) {
            return;
        }

        Holder<SoundEvent> hurtSound = ClientHelper.INSTANCE.hurtSound(gender().get());
        if(hurtSound == null) {
            return;
        }

        float pitchVariation = (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F;
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), hurtSound.value(),
            SoundSource.PLAYERS, 1f, pitchVariation + sounds().voicePitch().get(), false);
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> lines = super.getDebugInfo();
        lines.add("Female hurt sounds: " + sounds().hurt());
        lines.add("Show in armor: " + showBreastsInArmor());
        return lines;
    }

    // Bouncer methods for config values that act upon the current config instance

    public final Sounds sounds() {
        return config.sounds;
    }

    public final ConfigValue<Boolean> showBreastsInArmor() {
        return config.showBreastsInArmor;
    }
}

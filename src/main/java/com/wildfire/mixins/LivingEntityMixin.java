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

package com.wildfire.mixins;

import com.wildfire.events.EntityHurtSoundEvent;
import com.wildfire.events.EntityTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
@Environment(EnvType.CLIENT)
abstract class LivingEntityMixin extends Entity {
    private LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    // TODO would it be worth adding an extra @Inject to #animateDamage(float) to account for servers (namely hypixel)
    //		using DamageTiltS2CPacket instead of the standard entity damage packet?
    @Inject(
        method = "handleDamageEvent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
        )
    )
    public void wildfiregender$playGenderHurtSound(DamageSource damageSource, CallbackInfo ci) {
        EntityHurtSoundEvent.EVENT.invoker().onHurt((LivingEntity)(Object)this, damageSource);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void wildfiregender$onTick(CallbackInfo ci) {
        //noinspection resource
        if(!level().isClientSide()) return; // ignore ticks from the singleplayer integrated server
        EntityTickEvent.EVENT.invoker().onTick((LivingEntity)(Object)this);
    }
}

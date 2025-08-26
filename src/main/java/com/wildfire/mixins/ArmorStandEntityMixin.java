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

import com.llamalad7.mixinextras.sugar.Local;
import com.wildfire.events.ArmorStandInteractEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ArmorStandEntity.class)
abstract class ArmorStandEntityMixin extends LivingEntity {
	private ArmorStandEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@ModifyArg(
		method = "equip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/decoration/ArmorStandEntity;equipStack(Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)V"
		),
		index = 1
	)
	public ItemStack wildfiregender$attachBreastData(ItemStack stack, @Local(argsOnly = true) EquipmentSlot slot,
	                                                 @Local(argsOnly = true) PlayerEntity player) {
		if(player == null || getEntityWorld().isClient() || slot != EquipmentSlot.CHEST || stack.isEmpty()) {
			return stack;
		}

		ArmorStandInteractEvents.EQUIP.invoker().onEquip(player, stack);

		return stack;
	}

	@ModifyArg(
		method = "equip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/player/PlayerEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"
		),
		index = 1
	)
	public ItemStack wildfiregender$removeBreastDataOnReplace(ItemStack stack, @Local(argsOnly = true) PlayerEntity player) {
		if(!player.getEntityWorld().isClient()) {
			ArmorStandInteractEvents.REMOVE.invoker().onRemove(stack);
		}
		return stack;
	}

	@ModifyArg(
		method = "onBreak",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/Block;dropStack(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V"
		),
		index = 2
	)
	public ItemStack wildfiregender$removeBreastDataOnBreak(ItemStack stack) {
		if(!getEntityWorld().isClient()) {
			ArmorStandInteractEvents.REMOVE.invoker().onRemove(stack);
		}
		return stack;
	}
}

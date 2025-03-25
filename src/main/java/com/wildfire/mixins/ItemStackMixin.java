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

import com.wildfire.events.ArmorStatsTooltipEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
@Environment(EnvType.CLIENT)
abstract class ItemStackMixin {
	@Shadow public abstract Item getItem();

	@Inject(method = "appendAttributeModifiersTooltip", at = @At("TAIL"))
	public void wildfiregender$armorStats(Consumer<Text> textConsumer, TooltipDisplayComponent displayComponent, PlayerEntity player, CallbackInfo ci) {
		var item = (ItemStack)(Object)this;
		if(item.get(DataComponentTypes.EQUIPPABLE) != null && displayComponent.shouldDisplay(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
			ArmorStatsTooltipEvent.EVENT.invoker().appendTooltips(item, textConsumer, player);
		}
	}
}

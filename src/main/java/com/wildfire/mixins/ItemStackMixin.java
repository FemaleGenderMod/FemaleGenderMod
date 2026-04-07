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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wildfire.events.ArmorStatsTooltipEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
@Environment(EnvType.CLIENT)
abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @SuppressWarnings("LocalMayUseName") // mixinextras seems to have issues with argsOnly named locals
    @WrapOperation(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V"))
    public void wildfiregender$appendPhysicsStats(
        ItemStack instance,
        EquipmentSlotGroup slot,
        @SuppressWarnings("NameDoesntMatchTargetClass") // name conflicts with our @Local consumer
        TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumers,
        Operation<Void> original,
        @Local(name = "first") MutableBoolean first,
        @Local(argsOnly = true) @Nullable Player player,
        @Local(argsOnly = true) Consumer<Component> consumer
    ) {
        original.call(instance, slot, consumers);
        if(slot == EquipmentSlotGroup.CHEST && first.isFalse()) {
            var item = (ItemStack)(Object)this;
            if(item.get(DataComponents.EQUIPPABLE) == null) {
                return;
            }
            ArmorStatsTooltipEvent.EVENT.invoker().appendTooltips(item, consumer, player);
        }
    }
}

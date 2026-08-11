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

import com.wildfire.main.entitydata.BreastDataComponent;
import com.wildfire.main.networking.NeoSync;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(WildfireGender.MODID)
public class WildfireGenderNeo {

    public WildfireGenderNeo(IEventBus modEventBus) {
        NeoSync.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(PlayerLoggedOutEvent.class, event -> WildfireEventHandler.playerDisconnected(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.StartTracking.class, this::onStartTracking);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntityJoinLevelEvent.class, this::onEntitySpawn);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PlayerInteractEvent.EntityInteract.class, this::onRightClickArmorStand);
        //TODO - Neo:
        // ArmorStandInteractEvents.EQUIP.register(WildfireEventHandler::onEquipArmorStand);
        // ArmorStandInteractEvents.REMOVE.register(BreastDataComponent::removeFromStack);
    }

    private void onStartTracking(PlayerEvent.StartTracking evt) {
        if (evt.getEntity() instanceof ServerPlayer sendTo) {
            WildfireEventHandler.onBeginTracking(evt.getTarget(), sendTo);;
        }
    }

    private static EquipmentSlot getEquipmentSlot(ItemStack stack) {
        EquipmentSlot slot = stack.getEquipmentSlot();
        if (slot == null) {
            Equippable equipable = stack.get(DataComponents.EQUIPPABLE);
            return equipable == null ? EquipmentSlot.MAINHAND : equipable.slot();
        }
        return slot;
    }

    private void onEntitySpawn(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity entity && getEquipmentSlot(entity.getItem()) == EquipmentSlot.CHEST) {
            //Remove our tag if it is present when an item drops (such as from an armor stand being broken)
            BreastDataComponent.removeFromStack(entity.getItem());
        }
    }

    private void onRightClickArmorStand(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        //TODO - Neo: Implement this again
        //Copy of various checks from ArmorStand#interactAt, so that we can only apply it if a stack is being transferred
        /*if (!player.level().isClientSide() && event.getTarget() instanceof ArmorStand armorStand && !armorStand.isMarker() && !player.isSpectator()) {
            ItemStack stack = player.getItemInHand(event.getHand());
            // Only apply to chestplates
            if (stack.isEmpty()) {
                EquipmentSlot clickedSlot = armorStand.getClickedSlot(event.getLocation());
                EquipmentSlot equipmentslot2 = armorStand.isDisabled(clickedSlot) ? getEquipmentSlot(stack) : clickedSlot;
                if (equipmentslot2 == EquipmentSlot.CHEST) {
                    //Copy of logic from ArmorStand#swapItem
                    ItemStack itemstack = armorStand.getItemBySlot(equipmentslot2);
                    if (!itemstack.isEmpty()) {
                        if ((armorStand.disabledSlots & 1 << equipmentslot2.getFilterBit(8)) == 0) {
                            //Stack is being removed from the armor stand, remove the corresponding tag key we added if it is present
                            BreastDataComponent.removeFromStack(itemstack);
                        }
                    }
                }
            } else if (getEquipmentSlot(stack) == EquipmentSlot.CHEST && WildfireClientHelper.getArmorConfig(stack).armorStandsCopySettings() &&
                       !armorStand.isDisabled(EquipmentSlot.CHEST)) {
                //Copy of logic from ArmorStand#swapItem
                ItemStack itemstack = armorStand.getItemBySlot(EquipmentSlot.CHEST);
                if (!itemstack.isEmpty() && (armorStand.disabledSlots & 1 << EquipmentSlot.CHEST.getFilterBit(8)) != 0) {
                    return;
                } else if (itemstack.isEmpty() && (armorStand.disabledSlots & 1 << EquipmentSlot.CHEST.getFilterBit(16)) != 0) {
                    return;
                } else if (player.getAbilities().instabuild && itemstack.isEmpty()) {
                    //Copy the stack and set it in the armor stand manually, cancelling the event so that it doesn't go through
                    // so that we can apply it but not set nbt on the held stack
                    stack = stack.copyWithCount(1);
                    event.setCanceled(true);
                } else if (!itemstack.isEmpty()) {
                    //Stack is being removed from the armor stand remove the corresponding tag key we added if it is present
                    BreastDataComponent.removeFromStack(itemstack);
                    if (stack.getCount() > 1) {
                        //If the held stack has a size greater than one, we are only removing so can exit. Otherwise we are swapping
                        // so need to add to the held stack
                        return;
                    }
                } else {
                    //Copy the stack and set it in the armor stand manually, cancelling the event so that it doesn't go through
                    // so that we can apply it but not set nbt on the held stack
                    stack = stack.split(1);
                    event.setCanceled(true);
                }

                PlayerConfigHolder playerConfig = WildfireGender.getPlayerById(player.getUUID());
                if (playerConfig == null) {
                    BreastDataComponent.removeFromStack(itemstack);
                } else {
                    IGenderArmor armorConfig = WildfireClientHelper.getArmorConfig(stack);
                    if (armorConfig.armorStandsCopySettings()) {
                        BreastDataComponent component = BreastDataComponent.fromPlayer(player, playerConfig);
                        if (component != null) {
                            component.write(stack);
                        }
                    }
                }
                if (event.isCanceled()) {
                    //We cancelled it, so we need to now actually set it as well
                    armorStand.setItemSlot(EquipmentSlot.CHEST, stack);
                }
            }
        }*/
    }
}

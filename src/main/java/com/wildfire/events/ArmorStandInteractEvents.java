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

package com.wildfire.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Events invoked when a player interacts with the {@link EquipmentSlot#CHEST chest slot} on an armor stand
 */
public final class ArmorStandInteractEvents {
	private ArmorStandInteractEvents() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Event invoked when a player equips an item onto an armor stand's {@link EquipmentSlot#CHEST chest slot}
	 */
	public static final Event<EquipItem> EQUIP = EventFactory.createArrayBacked(EquipItem.class, listeners -> (player, item) -> {
		for(var listener : listeners) {
			listener.onEquip(player, item);
		}
	});

	// this doesn't have the same chest slot item guarantee as the above event purely because the only use case
	// we have for this event is removing our nbt from the removed items, which is already only applicable
	// to chest slot items, and safely no-ops otherwise.
	/**
	 * Event invoked when an item is removed from an armor stand
	 *
	 * @apiNote The provided {@link ItemStack} is <b>not</b> guaranteed to be a {@link EquipmentSlot#CHEST chest slot} item.
	 */
	public static final Event<RemoveItem> REMOVE = EventFactory.createArrayBacked(RemoveItem.class, listeners -> item -> {
		for(var listener : listeners) {
			listener.onRemove(item);
		}
	});

	@FunctionalInterface
	public interface EquipItem {
		void onEquip(Player player, ItemStack item);
	}

	@FunctionalInterface
	public interface RemoveItem {
		void onRemove(ItemStack item);
	}
}

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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

/**
 * <p>Event invoked when <b>any</b> {@link LivingEntity} ticks on the client.</p>
 *
 * <p>Note that this event may not be consistently invoked for every entity, such as if other
 * mods (e.g. EntityCulling) cancel the entity tick.</p>
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface EntityTickEvent {
	Event<EntityTickEvent> EVENT = EventFactory.createArrayBacked(EntityTickEvent.class, listeners -> entity -> {
		for(var listener : listeners) {
			listener.onTick(entity);
		}
	});

	void onTick(LivingEntity entity);
}

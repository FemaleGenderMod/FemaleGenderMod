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

package com.wildfire.client.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.client.WildfireClientHelper;
import com.wildfire.common.entitydata.EntityConfigHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public record BothBreastsPhysics(BreastPhysics left, BreastPhysics right) {

    public BothBreastsPhysics(EntityConfigHolder<?> configHolder) {
        this(new BreastPhysics(configHolder), new BreastPhysics(configHolder));
    }

    /// @apiNote Only call this on the client side, or the implementation will crash
    public void tick(LivingEntity entity) {
        IGenderArmor armor = WildfireClientHelper.getArmorConfig(entity.getItemBySlot(EquipmentSlot.CHEST));

        left.update(entity, armor);
        right.update(entity, armor);
    }
}

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

package com.wildfire.common.entities.armorstands;

import com.wildfire.api.Gender;
import com.wildfire.common.entities.BreastDataComponent;
import com.wildfire.common.entities.EntityConfigHolder;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

public class ArmorStandConfigHolder extends EntityConfigHolder<ArmorStandConfig> {
    protected boolean jacketLayer = true;
    protected @Nullable BreastDataComponent fromComponent;

    public ArmorStandConfigHolder(final UUID uuid) {
        super(uuid, ArmorStandConfig.createDefault());
    }

    /// Copy gender settings included in the given [`item NBT`][ItemStack] to the current entity
    ///
    /// @see BreastDataComponent
    public void readFromStack(ItemStack chestplate) {
        CustomData component = chestplate.get(DataComponents.CUSTOM_DATA);
        if (chestplate.isEmpty() || component == null) {
            this.fromComponent = null;
            gender().update(Gender.MALE);
            return;
        } else if(fromComponent != null && Objects.equals(component, fromComponent.nbtComponent())) {
            // nothing's changed since the last time we checked, so there's no need to read from the
            // underlying nbt tag again
            return;
        }

        fromComponent = BreastDataComponent.fromComponent(component);
        if (fromComponent == null) {
            gender().update(Gender.MALE);
            return;
        }

        breasts().updateFromComponent(fromComponent);
        gender().update(breasts().bustSize().get() >= 0.02f ? Gender.FEMALE : Gender.MALE);
        this.jacketLayer = fromComponent.jacket();
    }

    /// Returns `true` if the player who equipped the chestplate onto this armor stand has their jacket layer visible
    public boolean hasJacketLayer() {
        return jacketLayer;
    }
}

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

package com.wildfire.mixins.renderstate;

import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.EntityConfigState;
import com.wildfire.render.GenderEntityRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("unused")
@Mixin(LivingEntityRenderState.class)
@Implements(@Interface(iface = GenderEntityRenderState.class, prefix = "wildfire_gender$"))
@Environment(EnvType.CLIENT)
abstract class LivingEntityRenderStateMixin {
	private @Unique final EntityConfigState entityConfigState = new EntityConfigState();
	private @Unique boolean isBreathing = true;
	private @Unique @Nullable Text wildfireNametag = null;
	private @Unique boolean isArmorStand = false;

	public @NotNull EntityConfigState wildfire_gender$getEntityConfigState() {
		return this.entityConfigState;
	}

	public void wildfire_gender$updateEntityConfigState(EntityConfig entityConfig) {
		this.entityConfigState.update(entityConfig);
	}

	public boolean wildfire_gender$isBreathing() {
		return this.isBreathing;
	}

	public void wildfire_gender$setBreathing(boolean breathing) {
		this.isBreathing = breathing;
	}

	public @Nullable Text wildfire_gender$getWildfireNametag() {
		return this.wildfireNametag;
	}

	public void wildfire_gender$setWildfireNametag(@Nullable Text nametag) {
		this.wildfireNametag = nametag;
	}

	public boolean wildfire_gender$isArmorStand() {
		return this.isArmorStand;
	}

	public void wildfire_gender$setArmorStand(boolean armorStand) {
		this.isArmorStand = armorStand;
	}
}

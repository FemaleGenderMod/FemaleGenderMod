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
import com.wildfire.render.GenderRenderState;
import com.wildfire.render.GenderEntityRenderStateAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("unused")
@Mixin(LivingEntityRenderState.class)
@Environment(EnvType.CLIENT)
abstract class LivingEntityRenderStateMixin implements GenderEntityRenderStateAccessor {
	private @Unique final GenderRenderState genderRenderState = new GenderRenderState();

	@Override
	public @NotNull GenderRenderState wildfire_gender$getRenderState() {
		return this.genderRenderState;
	}

	@Override
	public void wildfire_gender$updateRenderState(EntityConfig entityConfig, LivingEntity entity) {
		this.genderRenderState.update(entityConfig, entity);
	}
}

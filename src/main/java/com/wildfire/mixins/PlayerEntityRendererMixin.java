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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.wildfire.events.PlayerNametagRenderEvent;
import com.wildfire.main.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
@Environment(EnvType.CLIENT)
abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<PlayerEntity, PlayerEntityRenderState, BipedEntityModel<PlayerEntityRenderState>> {
	private PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, BipedEntityModel<PlayerEntityRenderState> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}

	@ModifyReturnValue(method = "hasLabel(Lnet/minecraft/entity/PlayerLikeEntity;D)Z", at = @At("RETURN"))
	public boolean wildfiregender$forceLabel(boolean original, @Local(argsOnly = true) PlayerLikeEntity player) {
		if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
			if(player instanceof ClientPlayerEntity && ClientConfig.INSTANCE.get(ClientConfig.DISPLAY_OWN_NAMETAG)) {
				return true;
			}
		}
		return original;
	}

	@SuppressWarnings("CodeBlock2Expr")
	@Inject(
		method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)
	)
	public void wildfiregender$renderNametag(PlayerEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
		PlayerNametagRenderEvent.EVENT.invoker().onRenderNameTag(state, matrixStack, (text) -> {
			queue.submitLabel(
					matrixStack,
					state.nameLabelPos,
					state.extraEars ? -10 : 0,
					text,
					!state.sneaking,
					state.light,
					state.squaredDistanceToCamera,
					cameraState
			);
		});
	}
}

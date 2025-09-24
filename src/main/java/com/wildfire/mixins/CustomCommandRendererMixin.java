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
import com.wildfire.mixins.accessors.GameRendererAccessor;
import com.wildfire.mixins.accessors.RenderDispatcherAccessor;
import com.wildfire.render.BreastRenderCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.CustomCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(CustomCommandRenderer.class)
class CustomCommandRendererMixin {
	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue$Custom;render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;)V"
			)
	)
	public void wildfiregender$dodgyRenderingHackToRenderBreastLayerWithOutline(
			OrderedRenderCommandQueue.Custom instance,
			MatrixStack.Entry entry,
			VertexConsumer vertexConsumer,
			Operation<Void> original,
			@Local Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.CustomCommand>> mapEntry
	) {
		original.call(instance, entry, vertexConsumer);

		// effectively a copy of what mojang does for Model rendering, but applied to our custom vertex rendering
		// wherever applicable.
		if(instance instanceof BreastRenderCommand breastRenderCommand && breastRenderCommand.outline() != 0) {
			var layer = mapEntry.getKey();
			if(layer.getAffectedOutline().isPresent()) {
				var featureDispatcher = ((GameRendererAccessor) MinecraftClient.getInstance().gameRenderer).getRenderDispatcher();
				var outlineVertexProvider = ((RenderDispatcherAccessor) featureDispatcher).getOutlineVertexConsumerProvider();
				original.call(instance, entry, outlineVertexProvider.getBuffer(layer));
			}
		}
	}
}

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
import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.events.PlayerNametagRenderEvent;
import com.wildfire.main.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
@Environment(EnvType.CLIENT)
abstract class AvatarRendererMixin extends LivingEntityRenderer<Avatar, AvatarRenderState, HumanoidModel<AvatarRenderState>> {
    private AvatarRendererMixin(EntityRendererProvider.Context ctx, HumanoidModel<AvatarRenderState> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @ModifyReturnValue(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at = @At("RETURN"))
    public boolean wildfiregender$forceLabel(boolean original, @Local(argsOnly = true) Avatar entity) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if(entity instanceof LocalPlayer && ClientConfig.INSTANCE.get(ClientConfig.DISPLAY_OWN_NAMETAG)) {
                return true;
            }
        }
        return original;
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Inject(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
            shift = At.Shift.AFTER
        )
    )
    public void wildfiregender$renderNametag(
        final AvatarRenderState state,
        final PoseStack poseStack,
        final SubmitNodeCollector collector,
        final CameraRenderState camera,
        CallbackInfo ci
    ) {
        PlayerNametagRenderEvent.EVENT.invoker().onRenderNameTag(state, poseStack, (text) -> {
            collector.submitNameTag(
                poseStack,
                state.nameTagAttachment,
                state.showExtraEars ? -10 : 0,
                text,
                !state.isDiscrete,
                state.lightCoords,
                //? if 26.1
                //state.distanceToCameraSq,
                camera
            );
        });
    }
}

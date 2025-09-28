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

package com.wildfire.render;

import com.wildfire.api.IGenderArmor;
import com.wildfire.gui.screen.WildfireBreastUVEditorScreen;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.mixins.accessors.LivingEntityRendererAccessor;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.render.WildfireModelRenderer.OverlayModelBox;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class GenderLayer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>> extends FeatureRenderer<S, M> {

	private static final float DEG_TO_RAD = (float) (Math.PI / 180);

	private BreastModelBox lBreast, rBreast;
	private OverlayModelBox lBreastWear, rBreastWear;

	private final FeatureRendererContext<S, M> context;

	private float preBreastSize, preBreastOffsetZ;
	private boolean isUniboob;
	protected ItemStack armorStack; // although ItemStacks are mutable, this is safe as it is a copy of the real one
	protected IGenderArmor genderArmor;
	protected boolean isChestplateOccupied, bounceEnabled, breathingAnimation;
	protected float breastOffsetX, breastOffsetY, breastOffsetZ, lPhysPositionY, lPhysPositionX, rPhysPositionY, rPhysPositionX,
			lPhysBounceRotation, rPhysBounceRotation, breastSize, zOffset, outwardAngle;


	public GenderLayer(FeatureRendererContext<S, M> render) {
		super(render);
		this.context = render;
	}

	/**
	 * Convenience method around {@link LivingEntityRendererAccessor#invokeGetRenderLayer}
	 */
	private @Nullable RenderLayer getRenderLayer(S state) {
		boolean bodyVisible = !state.invisible;
		boolean translucent = state.invisible && !state.invisibleToPlayer;
		boolean glowing = state.hasOutline();

		var renderer = (LivingEntityRenderer<?, ?, ?>) context;
		return ((LivingEntityRendererAccessor) renderer).invokeGetRenderLayer(state, bodyVisible, translucent, glowing);
	}

	@Override
	public void render(MatrixStack matrixStack, OrderedRenderCommandQueue queue, int light, S state, float limbAngle, float limbDistance) {
		if(MinecraftClient.getInstance().world == null) {
			// TODO rendering in a menu is harder to support as we only tick physics when in a world,
			//		and entities rendered in the main menu are naturally not in a world
			return;
		}

		var entityConfigState = GenderRenderState.get(state);
		if(entityConfigState == null) return;

		//TODO: Better way for this?
		if(this.lBreast == null || this.rBreast == null || this.lBreastWear == null || this.rBreastWear == null) {
			this.lBreast = new BreastModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 4, 0.0F, entityConfigState.leftBreastUVLayout);
			this.rBreast = new BreastModelBox(64, 64, 0.0F, 0.0F, 0F, 4, 5, 4, 0.0F, entityConfigState.rightBreastUVLayout);
			this.lBreastWear = new OverlayModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, entityConfigState.leftBreastOverlayUVLayout);
			this.rBreastWear = new OverlayModelBox(64, 64,  0, 0.0F, 0F, 4, 5, 3, 0.0F, entityConfigState.rightBreastOverlayUVLayout);
		}

		try {
			if(!setupRender(state, entityConfigState)) return;
			int overlay = LivingEntityRenderer.getOverlay(state, 0);

			if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
				//TODO: REMOVE THIS FOR PRODUCTION -- THIS IS FOR DEBUGGING UV MAPPING. !! IT WILL CAUSE FRAME DROPS !!
				boolean isBreastsDebugMode = ClientConfig.BREASTS_DEBUG || (MinecraftClient.getInstance().currentScreen instanceof WildfireBreastUVEditorScreen);
				entityConfigState.leftBreastUVLayout = new int[][] {
						{24, 21, 27, 26},  // EAST
						{16, 21, 20, 26},   // WEST
						{20, 17, 24, 21},    // DOWN
						{20, 25, 24, 27},   // UP
						{20, 21, 24, 26}    // NORTH
				};
				entityConfigState.rightBreastUVLayout = new int[][] {
						{28, 21, 32, 26},  // EAST
						{21, 21, 24, 26},    // WEST
						{24, 17, 28, 21},    // DOWN
						{24,25, 28, 27},   // UP
						{24, 21, 28, 26}    // NORTH
				};
				this.lBreast = new BreastModelBox(64, 64, isBreastsDebugMode ? -6F : -4F, 0.0F, isBreastsDebugMode ? -5F : 0F, 4, 5, 3, 0.0F, entityConfigState.leftBreastUVLayout);
				this.rBreast = new BreastModelBox(64, 64, isBreastsDebugMode ? 2F : 0F, 0.0F, isBreastsDebugMode ? -5F : 0F, 4, 5, 3, 0.0F, entityConfigState.rightBreastUVLayout);
				lBreastWear = new OverlayModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, entityConfigState.leftBreastOverlayUVLayout);
				rBreastWear = new OverlayModelBox(64, 64, 0, 0.0F, 0F, 4, 5, 3, 0.0F, entityConfigState.rightBreastOverlayUVLayout);
			}

			//noinspection CodeBlock2Expr
			renderSides(state, getContextModel(), matrixStack, side -> {
				renderBreast(state, matrixStack, queue, overlay, side);
			});
		} catch(Exception e) {
			WildfireGender.LOGGER.error("Failed to render breast layer", e);
		}
	}

	/**
	 * Common logic for setting up breast rendering
	 *
	 * @return {@code true} if rendering should continue
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	protected boolean setupRender(S state, GenderRenderState genderRenderState) {
		if(!ClientConfig.RENDER_BREASTS) return false;

		armorStack = state.equippedChestStack;
		//Note: When the stack is empty the helper will fall back to an implementation that returns the proper data
		genderArmor = WildfireHelper.getArmorConfig(armorStack);
		isChestplateOccupied = genderArmor.coversBreasts() && !genderRenderState.armorPhysicsOverride;
		if(genderArmor.alwaysHidesBreasts() || !genderRenderState.showBreastsInArmor && isChestplateOccupied) {
			//If the armor always hides breasts or there is armor and the player configured breasts
			// to be hidden when wearing armor, we can just exit early rather than doing any calculations
			return false;
		}

		if(!isLayerVisible(state)) {
			return false;
		}

		GenderRenderState.BreastState breasts = genderRenderState.breasts;
		breastOffsetX = WildfireHelper.round(breasts.xOffset, 1);
		breastOffsetY = -WildfireHelper.round(breasts.yOffset, 1);
		breastOffsetZ = -WildfireHelper.round(breasts.zOffset, 1);

		isUniboob = breasts.uniboob;

		GenderRenderState.BreastPhysicsState leftPhysicsState = genderRenderState.leftBreastPhysics;
		final float bSize = leftPhysicsState.getBreastSize();
		outwardAngle = Math.round(breasts.cleavage * 100f);
		outwardAngle = Math.min(outwardAngle, 10);

		resizeBox(genderRenderState, bSize);

		lPhysPositionY = leftPhysicsState.getPositionY();
		lPhysPositionX = leftPhysicsState.getPositionX();
		lPhysBounceRotation = leftPhysicsState.getBounceRotation();
		if(isUniboob) {
			rPhysPositionY = lPhysPositionY;
			rPhysPositionX = lPhysPositionX;
			rPhysBounceRotation = lPhysBounceRotation;
		} else {
			GenderRenderState.BreastPhysicsState rightPhysicsState = genderRenderState.rightBreastPhysics;
			rPhysPositionY = rightPhysicsState.getPositionY();
			rPhysPositionX = rightPhysicsState.getPositionX();
			rPhysBounceRotation = rightPhysicsState.getBounceRotation();
		}

		breastSize = Math.min(bSize * 1.5f, 0.7f); // Limit the max size to 0.7f

		if (bSize > 0.7f) {
			breastSize = bSize; // If bSize exceeds 0.7f, use bSize
		}

		if (breastSize < 0.02f) {
			return false; // Return false if breastSize is too small
		}

		zOffset = 0.0625f - (bSize * 0.0625f); // Calculate zOffset
		breastSize += 0.5f * Math.abs(bSize - 0.7f) * 2f; // Adjust breastSize based on bSize

		float resistance = MathHelper.clamp(genderArmor.physicsResistance(), 0, 1);
		breathingAnimation = ((genderRenderState.armorPhysicsOverride || resistance <= 0.5F) && genderRenderState.isBreathing);
		bounceEnabled = genderRenderState.hasBreastPhysics && (!isChestplateOccupied || resistance < 1); //oh, you found this?
		return true;
	}

	protected boolean isLayerVisible(S state) {
		return !state.invisibleToPlayer || state.hasOutline();
	}

	protected void resizeBox(GenderRenderState genderRenderState, float breastSize) {
		float reducer = -1;
		if(breastSize < 0.84f) reducer++;
		if(breastSize < 0.72f) reducer++;

		if(preBreastSize != breastSize) {
			this.lBreast = new BreastModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 4, 0.0F, genderRenderState.leftBreastUVLayout);
			this.rBreast = new BreastModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 4, 0.0F, genderRenderState.rightBreastUVLayout);
			/*lBreast = new BreastModelBox(64, 64, 16, 17, -4F, 0.0F, 0F, 4, 5, (int) (4 - breastOffsetZ - reducer), 0.0F, false);
			rBreast = new BreastModelBox(64, 64, 20, 17, 0, 0.0F, 0F, 4, 5, (int) (4 - breastOffsetZ - reducer), 0.0F, false);*/
			preBreastSize = breastSize;
		}
	}

	protected void setupTransformations(S state, M model, MatrixStack matrixStack, BreastSide side) {
		if(state.baby) {
			matrixStack.scale(state.ageScale, state.ageScale, state.ageScale);
			matrixStack.translate(0f, 0.75f, 0f);
		}

		ModelPart body = model.body;
		matrixStack.translate(body.originX * 0.0625f, body.originY * 0.0625f, body.originZ * 0.0625f);
		if(body.roll != 0.0F || body.yaw != 0.0F || body.pitch != 0.0F) {
			matrixStack.multiply(new Quaternionf().rotationZYX(body.roll, body.yaw, body.pitch));
		}

		if(bounceEnabled) {
			matrixStack.translate((side.isLeft ? lPhysPositionX : rPhysPositionX) / 32f, 0, 0);
			matrixStack.translate(0, (side.isLeft ? lPhysPositionY : rPhysPositionY) / 32f, 0);
		}

		matrixStack.translate((side.isLeft ? breastOffsetX : -breastOffsetX) * 0.0625f, 0.05625f + (breastOffsetY * 0.0625f), zOffset - 0.0625f * 2f + (breastOffsetZ * 0.0425f)); //shift down to correct position

		if(!isUniboob) {
			matrixStack.translate(-0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
		}
		if(bounceEnabled) {
			matrixStack.multiply(new Quaternionf().rotationXYZ(0, (float)((side.isLeft ? lPhysBounceRotation : rPhysBounceRotation) * (Math.PI / 180f)), 0));
		}
		if(!isUniboob) {
			matrixStack.translate(0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
		}

		float rotation = breastSize;
		if(bounceEnabled) {
			matrixStack.translate(0, -0.035f * breastSize, 0); //shift down to correct position
			rotation -= (side.isLeft ? lPhysPositionY : rPhysPositionY) / 12f;
		}

		rotation = Math.min(rotation, breastSize + 0.2f);
		rotation = Math.min(rotation, 1); //hard limit for MAX

		if(isChestplateOccupied) {
			matrixStack.translate(0, 0, 0.01f);
		}

		Quaternionf rotationTransform = new Quaternionf()
				.rotationY((side.isLeft ? outwardAngle : -outwardAngle) * DEG_TO_RAD)
				.rotateX(-35f * rotation * DEG_TO_RAD);

		if(breathingAnimation) {
			float f5 = -MathHelper.cos(state.age * 0.09F) * 0.45F + 0.45F;
			rotationTransform.rotateX(f5 * DEG_TO_RAD);
		}

		matrixStack.multiply(rotationTransform);
		matrixStack.scale(0.9995f, 1f, 1f); //z-fighting FIXXX
	}

	private void renderBreast(S state, MatrixStack matrixStack, OrderedRenderCommandQueue queue, int overlay, BreastSide side) {
		RenderLayer renderLayer = getRenderLayer(state);
		if(renderLayer == null) return; // only render if the player is visible in some capacity

		int alpha = state.invisible ? ColorHelper.channelFromFloat(0.15f) : 255;
		int color = ColorHelper.getArgb(alpha, 255, 255, 255);

		var model = side.isLeft ? lBreast : rBreast;
		queue.submitCustom(matrixStack, renderLayer, new BreastRenderCommand(model, state, overlay, color));

		if(state instanceof PlayerEntityRenderState playerState && playerState.jacketVisible) {
			matrixStack.translate(0, 0, -0.015f);
			matrixStack.scale(1.05f, 1.05f, 1.05f);
			var jacketModel = side.isLeft ? lBreastWear : rBreastWear;
			queue.submitCustom(matrixStack, renderLayer, new BreastRenderCommand(jacketModel, state, overlay, color));
		}
	}

	protected void renderSides(S state, M model, MatrixStack matrixStack, Consumer<BreastSide> renderer) {
		matrixStack.push();
		try {
			setupTransformations(state, model, matrixStack, BreastSide.LEFT);
			renderer.accept(BreastSide.LEFT);
		} finally {
			matrixStack.pop();
		}

		matrixStack.push();
		try {
			setupTransformations(state, model, matrixStack, BreastSide.RIGHT);
			renderer.accept(BreastSide.RIGHT);
		} finally {
			matrixStack.pop();
		}
	}

	public static void renderBox(WildfireModelRenderer.ModelBox model, MatrixStack.Entry entry, VertexConsumer vertexConsumer,
									int light, int overlay, int color) {
		Matrix4f matrix4f = entry.getPositionMatrix();
		Matrix3f matrix3f = entry.getNormalMatrix();
		for(var quad : model.quads) {
			if(quad != null) {
				Vector3f vector3f = new Vector3f(quad.normal.x, quad.normal.y, quad.normal.z).mul(matrix3f);
				float normalX = vector3f.x;
				float normalY = vector3f.y;
				float normalZ = vector3f.z;
				for (var vertex : quad.vertexPositions) {
					float j = vertex.x() / 16.0F;
					float k = vertex.y() / 16.0F;
					float l = vertex.z() / 16.0F;
					Vector4f vector4f = new Vector4f(j, k, l, 1.0F).mul(matrix4f);
					vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), color, vertex.u(), vertex.v(),
							overlay, light, normalX, normalY, normalZ);
				}
			}
		}
	}
}

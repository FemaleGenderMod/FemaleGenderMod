package com.wildfire.render;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.physics.BreastPhysics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenderLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private WildfireModelRenderer.BreastModelBox lBreast;
	private WildfireModelRenderer.BreastModelBox rBreast;
	private final RenderLayerParent<T, M> context;

	private static final WildfireModelRenderer.OverlayModelBox lBreastWear = new WildfireModelRenderer.OverlayModelBox(true, 64, 64, 17, 34, -4.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);
	private static final WildfireModelRenderer.OverlayModelBox rBreastWear = new WildfireModelRenderer.OverlayModelBox(false, 64, 64, 21, 34, 0.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);

	protected ItemStack armorStack;
	protected IGenderArmor genderArmor;
	protected boolean isChestplateOccupied;
	protected boolean bounceEnabled;
	protected boolean breathingAnimation;
	protected float breastOffsetX, breastOffsetY, breastOffsetZ;
	protected float lPhysPositionY, lPhysPositionX, rPhysPositionY, rPhysPositionX;
	protected float lPhysBounceRotation, rPhysBounceRotation;
	protected float breastSize, zOffset, outwardAngle, preBreastSize, preBreastOffsetZ;
	protected Breasts breasts;

	public GenderLayer(RenderLayerParent<T, M> render) {
		super(render);
		this.context = render;
		this.lBreast = new WildfireModelRenderer.BreastModelBox(64, 64, 16, 17, -4.0F, 0.0F, 0.0F, 4, 5, 4, 0.0F, false);
		this.rBreast = new WildfireModelRenderer.BreastModelBox(64, 64, 20, 17, 0.0F, 0.0F, 0.0F, 4, 5, 4, 0.0F, false);
	}

	@Nullable
	private RenderType getRenderLayer(T entity) {
		if (this.context instanceof LivingEntityRenderer<T, M> renderer) {
			return renderer.getModel().renderType(renderer.getTextureLocation(entity));
		}
		return null;
	}

	@Override
	public void render(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int light, @NotNull T entity, float limbAngle, float limbDistance, float partialTicks, float animationProgress, float headYaw, float headPitch) {
		if (Minecraft.getInstance().player == null) return;

		EntityConfig config = EntityConfig.getEntity(entity);
		if (config != null) {
			try {
				if (!this.setupRender(entity, config, partialTicks)) return;

				int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
				this.renderSides(entity, this.getParentModel(), matrixStack, (side) ->
						this.renderBreast(entity, matrixStack, buffer, light, overlay, side)
				);
			} catch (Exception e) {
				WildfireGender.LOGGER.error("Failed to render breast layer", e);
			}
		}
	}

	protected boolean setupRender(T entity, EntityConfig config, float partialTicks) {
		this.armorStack = entity.getItemBySlot(EquipmentSlot.CHEST);
		this.genderArmor = WildfireHelper.getArmorConfig(this.armorStack);
		this.isChestplateOccupied = this.genderArmor.coversBreasts() && !config.getArmorPhysicsOverride();

		if (this.genderArmor.alwaysHidesBreasts() || (!config.showBreastsInArmor() && this.isChestplateOccupied)) {
			return false;
		}

		this.breasts = config.getBreasts();
		this.breastOffsetX = (float) Math.round(breasts.getXOffset() * 10.0F) / 10.0F;
		this.breastOffsetY = (float) -Math.round(breasts.getYOffset() * 10.0F) / 10.0F;
		this.breastOffsetZ = (float) -Math.round(breasts.getZOffset() * 10.0F) / 10.0F;

		BreastPhysics lPhys = config.getLeftBreastPhysics();
		float bSize = lPhys.getBreastSize(partialTicks);
		this.outwardAngle = Math.min(Math.round(breasts.getCleavage() * 100.0F), 10.0F);

		this.resizeBox(bSize);
		this.lPhysPositionY = Mth.lerp(partialTicks, lPhys.getPrePositionY(), lPhys.getPositionY());
		this.lPhysPositionX = Mth.lerp(partialTicks, lPhys.getPrePositionX(), lPhys.getPositionX());
		this.lPhysBounceRotation = Mth.lerp(partialTicks, lPhys.getPreBounceRotation(), lPhys.getBounceRotation());

		if (breasts.isUniboob()) {
			this.rPhysPositionY = lPhysPositionY;
			this.rPhysPositionX = lPhysPositionX;
			this.rPhysBounceRotation = lPhysBounceRotation;
		} else {
			BreastPhysics rPhys = config.getRightBreastPhysics();
			this.rPhysPositionY = Mth.lerp(partialTicks, rPhys.getPrePositionY(), rPhys.getPositionY());
			this.rPhysPositionX = Mth.lerp(partialTicks, rPhys.getPrePositionX(), rPhys.getPositionX());
			this.rPhysBounceRotation = Mth.lerp(partialTicks, rPhys.getPreBounceRotation(), rPhys.getBounceRotation());
		}

		this.breastSize = Math.max(bSize * 1.5F, bSize);
		if (this.breastSize < 0.02F) return false;

		this.zOffset = 0.0625F - bSize * 0.0625F;
		float resistance = Mth.clamp(this.genderArmor.physicsResistance(), 0.0F, 1.0F);

		// Lógica de animación de respiración (activada si no está sumergido o en fuego)
		this.breathingAnimation = (config.getArmorPhysicsOverride() || resistance <= 0.5F) &&
				(!entity.isInWater() || entity.level().getBlockState(entity.blockPosition()).is(Blocks.BUBBLE_COLUMN));

		this.bounceEnabled = config.hasBreastPhysics() && (!this.isChestplateOccupied || resistance < 1.0F);
		return true;
	}

	protected void resizeBox(float bSize) {
		float reducer = (bSize < 0.84F ? 1 : 0) + (bSize < 0.72F ? 1 : 0) - 1.0F;
		if (this.preBreastSize != bSize || this.preBreastOffsetZ != this.breastOffsetZ) {
			int depth = (int) (4.0F - this.breastOffsetZ - reducer);
			this.lBreast = new WildfireModelRenderer.BreastModelBox(64, 64, 16, 17, -4.0F, 0.0F, 0.0F, 4, 5, depth, 0.0F, false);
			this.rBreast = new WildfireModelRenderer.BreastModelBox(64, 64, 20, 17, 0.0F, 0.0F, 0.0F, 4, 5, depth, 0.0F, false);
			this.preBreastSize = bSize;
			this.preBreastOffsetZ = this.breastOffsetZ;
		}
	}

	protected void setupTransformations(T entity, M model, PoseStack matrixStack, BreastSide side) {
		if (entity.isBaby()) {
			float f = 0.5F;
			matrixStack.scale(f, f, f);
			matrixStack.translate(0.0F, 24.0F / 16.0F, 0.0F);
		}

		ModelPart body = model.body;
		matrixStack.translate(body.x * 0.0625F, body.y * 0.0625F, body.z * 0.0625F);
		if (body.zRot != 0.0F) matrixStack.mulPose((new Quaternionf()).rotationZ(body.zRot));
		if (body.yRot != 0.0F) matrixStack.mulPose((new Quaternionf()).rotationY(body.yRot));
		if (body.xRot != 0.0F) matrixStack.mulPose((new Quaternionf()).rotationX(body.xRot));

		if (this.bounceEnabled) {
			matrixStack.translate((side.isLeft ? lPhysPositionX : rPhysPositionX) / 32.0F, (side.isLeft ? lPhysPositionY : rPhysPositionY) / 32.0F, 0.0F);
		}

		matrixStack.translate((side.isLeft ? breastOffsetX : -breastOffsetX) * 0.0625F, 0.05625F + breastOffsetY * 0.0625F, zOffset - 0.125F + breastOffsetZ * 0.0625F);

		if (!breasts.isUniboob()) {
			float sign = side.isLeft ? 1 : -1;
			matrixStack.translate(-0.125F * sign, 0.0F, 0.0F);
			if (this.bounceEnabled) {
				matrixStack.mulPose((new Quaternionf()).rotationY((side.isLeft ? lPhysBounceRotation : rPhysBounceRotation) * (float)(Math.PI / 180.0)));
			}
			matrixStack.translate(0.125F * sign, 0.0F, 0.0F);
		}

		float totalRotation = Math.min(this.breastSize + (this.bounceEnabled ? -(side.isLeft ? lPhysPositionY : rPhysPositionY) / 12.0F : 0), 1.0F);

		matrixStack.mulPose((new Quaternionf()).rotationY((side.isLeft ? outwardAngle : -outwardAngle) * (float)(Math.PI / 180.0)));
		matrixStack.mulPose((new Quaternionf()).rotationX((-35.0F * totalRotation) * (float)(Math.PI / 180.0)));

		if (this.breathingAnimation) {
			float breath = -Mth.sin((float)entity.tickCount * 0.09F) * 0.45F + 0.45F;
			matrixStack.mulPose((new Quaternionf()).rotationX(breath * (float)(Math.PI / 180.0)));
		}
		matrixStack.scale(0.9995F, 1.0F, 1.0F);
	}

	private void renderBreast(T entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BreastSide side) {
		RenderType type = this.getRenderLayer(entity);
		if (type != null) {
			int color = FastColor.ARGB32.color(entity.isInvisible() ? 38 : 255, 255, 255, 255);
			VertexConsumer consumer = buffer.getBuffer(type);
			renderBox(side.isLeft ? lBreast : rBreast, matrixStack, consumer, light, overlay, color);

			if (entity instanceof AbstractClientPlayer player && player.isModelPartShown(PlayerModelPart.JACKET)) {
				matrixStack.pushPose();
				matrixStack.translate(0.0F, 0.0F, -0.015F);
				matrixStack.scale(1.05F, 1.05F, 1.05F);
				renderBox(side.isLeft ? lBreastWear : rBreastWear, matrixStack, consumer, light, overlay, color);
				matrixStack.popPose();
			}
		}
	}

	protected void renderSides(T entity, M model, PoseStack matrixStack, Consumer<BreastSide> renderer) {
		matrixStack.pushPose();
		this.setupTransformations(entity, model, matrixStack, BreastSide.LEFT);
		renderer.accept(BreastSide.LEFT);
		matrixStack.popPose();

		matrixStack.pushPose();
		this.setupTransformations(entity, model, matrixStack, BreastSide.RIGHT);
		renderer.accept(BreastSide.RIGHT);
		matrixStack.popPose();
	}

	protected static void renderBox(WildfireModelRenderer.ModelBox model, PoseStack matrixStack, VertexConsumer consumer, int light, int overlay, int color) {
	Matrix4f pose = matrixStack.last().pose();
	Matrix3f normalMatrix = matrixStack.last().normal();
	for (WildfireModelRenderer.TexturedQuad quad : model.quads) {
		Vector3f norm = new Vector3f(quad.normal).mul(normalMatrix);
		for (WildfireModelRenderer.PositionTextureVertex vertex : quad.vertexPositions) {
			consumer.addVertex(pose, vertex.x() / 16.0F, vertex.y() / 16.0F, vertex.z() / 16.0F)
					.setColor(color)
					.setUv(vertex.u(), vertex.v())
					.setOverlay(overlay)
					.setLight(light)
					.setNormal(norm.x(), norm.y(), norm.z());
		}
	}
}
}
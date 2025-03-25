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

import com.wildfire.api.IBreastArmorTexture;
import com.wildfire.api.impl.BreastArmorTexture;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.mixins.accessors.EquipmentRendererAccessor;
import com.wildfire.mixins.accessors.TextureManagerAccessor;
import com.wildfire.mixins.accessors.TrimSpriteKeyConstructorAccessor;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class GenderArmorLayer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>> extends GenderLayer<S, M> {

	private final EquipmentRenderer equipmentRenderer;
	private final EquipmentModelLoader equipmentModelLoader;
	protected BreastModelBox lBoobArmor, rBoobArmor;
	protected static final BreastModelBox lTrim, rTrim;
	private EntityConfig entityConfig;
	private @NotNull IBreastArmorTexture textureData = BreastArmorTexture.DEFAULT;

	private static boolean textureExists(Identifier id) {
		var texManager = MinecraftClient.getInstance().getTextureManager();
		var resourceManager = ((TextureManagerAccessor) texManager).getResourceContainer();
		return resourceManager.getResource(id).isPresent();
	}

	static {
		// apply a very slight delta to fix z-fighting with the armor
		lTrim = new BreastModelBox(64, 32, 16, 17, -4F, 0.0F, 0F, 4, 5, 4, 0.001F, false);
		rTrim = new BreastModelBox(64, 32, 20, 17, 0, 0.0F, 0F, 4, 5, 4, 0.001F, false);
	}

	public GenderArmorLayer(FeatureRendererContext<S, M> render, EquipmentModelLoader equipmentModelLoader, EquipmentRenderer equipmentRenderer) {
		super(render);
		this.equipmentRenderer = equipmentRenderer;
		this.equipmentModelLoader = equipmentModelLoader;
		lBoobArmor = new BreastModelBox(64, 32, 16, 17, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, false);
		rBoobArmor = new BreastModelBox(64, 32, 20, 17, 0, 0.0F, 0F, 4, 5, 3, 0.0F, false);
	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, S state, float limbAngle, float limbDistance) {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.player == null) {
			// TODO is it possible to remove this check? does anything this invoke still check
			//		the client player or world?
			return;
		}

		LivingEntity ent = getEntity(state);
		if(ent == null) return;

		final ItemStack chestplate = state.equippedChestStack;
		// Check if the worn item in the chest slot is actually equippable in the chest slot, and has a model to render
		var component = chestplate.get(DataComponentTypes.EQUIPPABLE);
		if(component == null || component.slot() != EquipmentSlot.CHEST) return;
		var asset = component.assetId().orElse(null);
		if(asset == null) return;
		var layers = equipmentModelLoader.get(asset).getLayers(EquipmentModel.LayerType.HUMANOID);
		if(layers.isEmpty()) return;

		try {
			entityConfig = EntityConfig.getEntity(ent);

			if(!setupRender(state, entityConfig)) return;
			if(ent instanceof ArmorStandEntity && !genderArmor.armorStandsCopySettings()) return;

			int color = DyedColorComponent.getColor(chestplate, 0);
			boolean glint = chestplate.hasGlint();

			renderSides(state, getContextModel(), matrixStack, side -> {
				// TODO is there still a need to allow for overriding the armor texture identifier?
				layers.forEach(layer -> {
					int layerColor = EquipmentRendererAccessor.invokeGetDyeColor(layer, color);
					var texture = layer.getFullTextureId(EquipmentModel.LayerType.HUMANOID);
					renderBreastArmor(texture, matrixStack, vertexConsumerProvider, light, side, layerColor, glint);
				});

				var trim = armorStack.get(DataComponentTypes.TRIM);
				if(trim != null) {
					renderArmorTrim(asset, matrixStack, vertexConsumerProvider, light, trim, glint, side);
				}
			});
		} catch(Exception e) {
			WildfireGender.LOGGER.error("Failed to render breast armor", e);
		}
	}

	@Override
	protected boolean isLayerVisible(S state) {
		return genderArmor.coversBreasts();
	}

	@Override
	protected void resizeBox(float breastSize) {
		if(genderArmor == null || Objects.equals(textureData, genderArmor.texture())) {
			return;
		}

		textureData = genderArmor.texture();
		var texSize = textureData.textureSize();
		var lUV = textureData.leftUv();
		var dim = textureData.dimensions();
		lBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), lUV.x(), lUV.y(), -4F, 0.0F, 0F, dim.x(), dim.y(), 3, 0.0F, false);
		var rUV = textureData.rightUv();
		rBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), rUV.x(), rUV.y(), 0, 0.0F, 0F, dim.x(), dim.y(), 3, 0.0F, false);
	}

	@Override
	protected void setupTransformations(S state, M model, MatrixStack matrixStack, BreastSide side) {
		super.setupTransformations(state, model, matrixStack, side);
		if((state instanceof PlayerEntityRenderState playerState && playerState.jacketVisible) ||
				(state instanceof ArmorStandEntityRenderState && entityConfig.hasJacketLayer())) {
			matrixStack.translate(0, 0, -0.015f);
			matrixStack.scale(1.05f, 1.05f, 1.05f);
		}
		matrixStack.translate(side.isLeft ? 0.001f : -0.001f, 0.015f, -0.015f);
		matrixStack.scale(1.05f, 1, 1);
	}

	// TODO eventually expose some way for mods to override this, maybe through a default impl in IGenderArmor or similar
	protected void renderBreastArmor(Identifier texture, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
	                                 int light, BreastSide side, int color, boolean glint) {
		if(!textureExists(texture)) {
			return;
		}

		BreastModelBox armor = side.isLeft ? lBoobArmor : rBoobArmor;
		RenderLayer armorType = RenderLayer.getArmorCutoutNoCull(texture);
		VertexConsumer armorVertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, armorType, glint);
		renderBox(armor, matrixStack, armorVertexConsumer, light, OverlayTexture.DEFAULT_UV, ColorHelper.fullAlpha(color));
	}

	protected void renderArmorTrim(RegistryKey<EquipmentAsset> armorModel, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
								   int light, ArmorTrim trim, boolean hasGlint, BreastSide side) {
		BreastModelBox trimModelBox = side.isLeft ? lTrim : rTrim;

		// this sucks, but it sucks less than simply copy/pasting the entire relevant block of code, and is
		// (at least theoretically) more compatible with other mods, assuming they simply mixin to TrimSpriteKey
		// to modify the armor trim sprite location.
		var key = TrimSpriteKeyConstructorAccessor.newKey(trim, EquipmentModel.LayerType.HUMANOID, armorModel);
		Sprite sprite = ((EquipmentRendererAccessor)equipmentRenderer).getTrimSprites().apply(key);

		var buffer = vertexConsumerProvider.getBuffer(TexturedRenderLayers.getArmorTrims(trim.pattern().value().decal()));
		var vertexConsumer = sprite.getTextureSpecificVertexConsumer(buffer);
		// Render the armor trim itself
		renderBox(trimModelBox, matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, -1);
		// The enchantment glint however requires special handling; due to how Minecraft's enchant glint rendering works, rendering
		// it at the same time as the trim itself results in the glint not rendering in sync with the rest of the armor.
		// We *also* can't simply render the glint for both the trim and armor at the same time, due to the slight delta we apply
		// to fix z-fighting between the trim and armor - and as such - a glint has to be rendered for each respective layer.
		if(hasGlint) {
			var glintBuffer = vertexConsumerProvider.getBuffer(RenderLayer.getArmorEntityGlint());
			renderBox(trimModelBox, matrixStack, glintBuffer, light, OverlayTexture.DEFAULT_UV, -1);
		}
	}
}

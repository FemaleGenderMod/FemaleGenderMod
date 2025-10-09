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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.mixins.accessors.EquipmentRendererAccessor;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.render.ducks.MissingTextureLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GenderArmorLayer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>> extends GenderLayer<S, M> {

	private final EquipmentRenderer equipmentRenderer;
	private final EquipmentModelLoader equipmentModelLoader;
	protected BreastModelBox lBoobArmor, rBoobArmor;
	protected static final BreastModelBox lTrim, rTrim;
	private GenderRenderState genderRenderState;
	private @NotNull IBreastArmorTexture textureData = IBreastArmorTexture.DEFAULT;

	static {
		var left = new UVLayout(
				new UVQuad(24, 21, 28, 26),  // EAST
				new UVQuad(16, 21, 20, 26),  // WEST
				new UVQuad(20, 17, 24, 21),  // DOWN
				new UVQuad(20, 25, 24, 27),  // UP
				new UVQuad(20, 21, 24, 26)   // NORTH
		);

		var right = new UVLayout(
				new UVQuad(28, 21, 32, 26),  // EAST
				new UVQuad(20, 21, 24, 26),  // WEST
				new UVQuad(24, 17, 28, 21),  // DOWN
				new UVQuad(24, 25, 28, 27),  // UP
				new UVQuad(24, 21, 28, 26)   // NORTH
		);

		// apply a very slight delta to fix rare layering issues with the normal armor layer
		// TODO look into how difficult it'd be to replicate Model render priority here
		lTrim = new BreastModelBox(64, 32, -4F, 0.0F, 0F, 4, 5, 4, 0.001F, left);
		rTrim = new BreastModelBox(64, 32, 0, 0.0F, 0F, 4, 5, 4, 0.001F, right);
	}

	private static boolean textureExists(Identifier texture) {
		var texManager = MinecraftClient.getInstance().getTextureManager();
		return !((MissingTextureLogger) texManager).wildfire_gender$missingTextures().contains(texture);
	}

	public GenderArmorLayer(FeatureRendererContext<S, M> render, EquipmentModelLoader equipmentModelLoader, EquipmentRenderer equipmentRenderer) {
		super(render);
		this.equipmentRenderer = equipmentRenderer;
		this.equipmentModelLoader = equipmentModelLoader;
	}

	@Override
	public void render(MatrixStack matrixStack, OrderedRenderCommandQueue queue, int light, S state, float limbAngle, float limbDistance) {
		if(MinecraftClient.getInstance().world == null) {
			// TODO rendering in a menu is harder to support as we only tick physics when in a world,
			//		and entities rendered in the main menu are naturally not in a world
			return;
		}

		this.genderRenderState = GenderRenderState.get(state);
		if (this.genderRenderState == null) return;

		final ItemStack chestplate = state.equippedChestStack;
		// Check if the worn item in the chest slot is actually equippable in the chest slot, and has a model to render
		var component = chestplate.get(DataComponentTypes.EQUIPPABLE);
		if(component == null || component.slot() != EquipmentSlot.CHEST) return;
		var asset = component.assetId().orElse(null);
		if(asset == null) return;
		var layers = equipmentModelLoader.get(asset).getLayers(EquipmentModel.LayerType.HUMANOID);
		if(layers.isEmpty()) return;

		try {
			if(!setupRender(state, this.genderRenderState)) return;
			if(state instanceof ArmorStandEntityRenderState && !genderArmor.armorStandsCopySettings()) return;

			int color = DyedColorComponent.getColor(chestplate, 0);
			boolean glint = chestplate.hasGlint();

			renderSides(state, getContextModel(), matrixStack, side -> {
				// TODO is there still a need to allow for overriding the armor texture identifier?
				layers.forEach(layer -> {
					int layerColor = EquipmentRenderer.getDyeColor(layer, color);
					var texture = layer.getFullTextureId(EquipmentModel.LayerType.HUMANOID);
					renderBreastArmor(texture, matrixStack, queue, state, side, layerColor, glint);
				});

				var trim = armorStack.get(DataComponentTypes.TRIM);
				if(trim != null) {
					renderArmorTrim(asset, matrixStack, queue, state, trim, side, glint);
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
	protected void resizeBox(GenderRenderState state, float breastSize) {
		/*if(genderArmor == null || Objects.equals(textureData, genderArmor.texture())) {
			return;
		}

		textureData = genderArmor.texture();
		var texSize = textureData.textureSize();
		var lUV = textureData.leftUv();
		var rUV = textureData.rightUv();
		var dim = textureData.dimensions();*/

		//lBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), lUV.x(), lUV.y(), -4F, 0.0F, 0F, dim.x(), dim.y(), 4, 0.0F, false);
		//rBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), rUV.x(), rUV.y(), 0, 0.0F, 0F, dim.x(), dim.y(), 4, 0.0F, false);

		// FIXME make this work with armor configs
		if(this.lBoobArmor == null || this.rBoobArmor == null) {
			lBoobArmor = new BreastModelBox(64, 32, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, state.leftBreastArmorUVLayout);
			rBoobArmor = new BreastModelBox(64, 32, 0, 0.0F, 0F, 4, 5, 3, 0.0F, state.rightBreastArmorUVLayout);
		}
	}

	@Override
	protected void setupTransformations(S state, M model, MatrixStack matrixStack, BreastSide side) {
		super.setupTransformations(state, model, matrixStack, side);
		if (genderRenderState.hasJacketLayer) {
			matrixStack.translate(0, 0, -0.015f);
			matrixStack.scale(1.05f, 1.05f, 1.05f);
		}
		matrixStack.translate(side.isLeft ? 0.001f : -0.001f, 0.015f, -0.015f);
		matrixStack.scale(1.05f, 1, 1);
	}

	// TODO eventually expose some way for mods to override this, maybe through a default impl in IGenderArmor or similar
	protected void renderBreastArmor(Identifier texture, MatrixStack matrixStack, OrderedRenderCommandQueue queue,
	                                 S state, BreastSide side, int color, boolean glint) {
		if(!textureExists(texture)) {
			return;
		}

		var model = side.isLeft ? lBoobArmor : rBoobArmor;
		var layer = RenderLayer.getArmorCutoutNoCull(texture);
		queue.submitCustom(matrixStack, layer, new BreastRenderCommand(model, state, OverlayTexture.DEFAULT_UV, ColorHelper.fullAlpha(color)));

		if(glint) {
			renderGlint(matrixStack, queue, state, model);
		}
	}

	protected void renderArmorTrim(RegistryKey<EquipmentAsset> armorModel, MatrixStack matrixStack, OrderedRenderCommandQueue queue,
								   S state, ArmorTrim trim, BreastSide side, boolean glint) {
		var model = side.isLeft ? lTrim : rTrim;

		// this sucks, but it sucks less than simply copy/pasting the entire relevant block of code, and is
		// (at least theoretically) more compatible with other mods, assuming they simply mixin to TrimSpriteKey
		// to modify the armor trim sprite location.
		var key = new EquipmentRenderer.TrimSpriteKey(trim, EquipmentModel.LayerType.HUMANOID, armorModel);
		Sprite sprite = ((EquipmentRendererAccessor)equipmentRenderer).getTrimSprites().apply(key);

		var layer = TexturedRenderLayers.getArmorTrims(trim.pattern().value().decal());
		queue.submitCustom(matrixStack, layer, BreastRenderCommand.trim(model, state, sprite));

		if(glint) {
			renderGlint(matrixStack, queue, state, model);
		}
	}

	protected void renderGlint(MatrixStack matrixStack, OrderedRenderCommandQueue renderQueue, S state, BreastModelBox box) {
		var glintLayer = RenderLayer.getArmorEntityGlint();
		renderQueue.submitCustom(matrixStack, glintLayer, new BreastRenderCommand(box, state, OverlayTexture.DEFAULT_UV, -1));
	}
}

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

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.api.IBreastArmorTexture;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.mixins.accessors.EquipmentLayerRendererAccessor;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.render.ducks.MissingTextureLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.UnknownNullability;

@Environment(EnvType.CLIENT)
public class GenderArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends GenderLayer<S, M> {

    private final EquipmentLayerRenderer equipmentRenderer;
    private final EquipmentAssetManager equipmentModelLoader;
    protected static final BreastModelBox lTrim, rTrim;

    @UnknownNullability("null until #resizeBox() is first called")
    protected BreastModelBox lBoobArmor, rBoobArmor;
    @UnknownNullability("null until first render pass")
    private GenderRenderState genderRenderState;

    @SuppressWarnings({"unused", "FieldMayBeFinal"}) // TODO fix this
    private IBreastArmorTexture textureData = IBreastArmorTexture.DEFAULT;

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
        var texManager = Minecraft.getInstance().getTextureManager();
        return !((MissingTextureLogger) texManager).wildfire_gender$missingTextures().contains(texture);
    }

    public GenderArmorLayer(RenderLayerParent<S, M> render, EquipmentAssetManager equipmentModelLoader, EquipmentLayerRenderer equipmentRenderer) {
        super(render);
        this.equipmentRenderer = equipmentRenderer;
        this.equipmentModelLoader = equipmentModelLoader;
    }

    @Override
    public void submit(PoseStack matrixStack, SubmitNodeCollector queue, int light, S state, float limbAngle, float limbDistance) {
        this.genderRenderState = GenderRenderState.get(state);
        if (this.genderRenderState == null) return;

        final ItemStack chestplate = state.chestEquipment;
        // Check if the worn item in the chest slot is actually equippable in the chest slot, and has a model to render
        var component = chestplate.get(DataComponents.EQUIPPABLE);
        if(component == null || component.slot() != EquipmentSlot.CHEST) return;
        var asset = component.assetId().orElse(null);
        if(asset == null) return;
        var layers = equipmentModelLoader.get(asset).getLayers(EquipmentClientInfo.LayerType.HUMANOID);
        if(layers.isEmpty()) return;

        try {
            if(!setupRender(state, this.genderRenderState)) return;
            if(state instanceof ArmorStandRenderState && !genderArmor.armorStandsCopySettings()) return;

            int color = DyedItemColor.getOrDefault(chestplate, 0);
            boolean glint = chestplate.hasFoil();

            renderSides(state, getParentModel(), matrixStack, side -> {
                // TODO is there still a need to allow for overriding the armor texture identifier?
                layers.forEach(layer -> {
                    int layerColor = EquipmentLayerRenderer.getColorForLayer(layer, color);
                    var texture = layer.getTextureLocation(EquipmentClientInfo.LayerType.HUMANOID);
                    renderBreastArmor(texture, matrixStack, queue, state, side, layerColor, glint);
                });

                var trim = armorStack.get(DataComponents.TRIM);
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
    protected void setupTransformations(S state, M model, PoseStack matrixStack, BreastSide side) {
        super.setupTransformations(state, model, matrixStack, side);
        if(genderRenderState.hasJacketLayer) {
            matrixStack.translate(0, 0, -0.015f);
            matrixStack.scale(1.05f, 1.05f, 1.05f);
        }
        matrixStack.translate(side.isLeft ? 0.001f : -0.001f, 0.015f, -0.015f);
        matrixStack.scale(1.05f, 1, 1);
    }

    // TODO eventually expose some way for mods to override this, maybe through a default impl in IGenderArmor or similar
    protected void renderBreastArmor(Identifier texture, PoseStack poseStack, SubmitNodeCollector collector,
                                     S state, BreastSide side, int color, boolean glint) {
        if(!textureExists(texture)) {
            return;
        }

        var model = side.isLeft ? lBoobArmor : rBoobArmor;
        RenderType type = RenderTypes.armorCutoutNoCull(texture);
        collector.submitModel(new BreastModel(model), state, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, ARGB.opaque(color), null, state.outlineColor, null);

        if(glint) {
            renderGlint(poseStack, collector, state, model);
        }
    }

    protected void renderArmorTrim(ResourceKey<EquipmentAsset> armorModel, PoseStack poseStack, SubmitNodeCollector collector,
                                   S state, ArmorTrim trim, BreastSide side, boolean glint) {
        var model = side.isLeft ? lTrim : rTrim;

        var key = new EquipmentLayerRenderer.TrimSpriteKey(trim, EquipmentClientInfo.LayerType.HUMANOID, armorModel);
        TextureAtlasSprite sprite = ((EquipmentLayerRendererAccessor) equipmentRenderer).getTrimSpriteLookup().apply(key);

        RenderType type = Sheets.armorTrimsSheet(trim.pattern().value().decal());
        collector.submitModel(new BreastModel(model), state, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, 0, null);

        if(glint) {
            renderGlint(poseStack, collector, state, model);
        }
    }

    protected void renderGlint(PoseStack poseStack, SubmitNodeCollector collector, S state, BreastModelBox model) {
        RenderType type = RenderTypes.armorEntityGlint();
//        collector.wildfire_gender$submitBreastModel(box, matrixStack, glintLayer, null, state, OverlayTexture.NO_OVERLAY, -1, 0);
        collector.submitModel(new BreastModel(model), state, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
    }
}

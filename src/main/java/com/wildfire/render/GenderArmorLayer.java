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
import com.wildfire.main.uvs.UVMap;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import org.joml.Vector2ic;

/// @apiNote Only use this on the client side
public class GenderArmorLayer<STATE extends HumanoidRenderState, MODEL extends HumanoidModel<STATE>> extends GenderLayer<STATE, MODEL> {

    private final EquipmentLayerRenderer equipmentRenderer;

    @UnknownNullability("null until #resizeBox() is first called")
    private BreastModelBox lBoobArmor, rBoobArmor;

    private IBreastArmorTexture textureData = IBreastArmorTexture.DEFAULT;

    public GenderArmorLayer(RenderLayerParent<STATE, MODEL> render, EquipmentLayerRenderer equipmentRenderer) {
        super(render);
        this.equipmentRenderer = equipmentRenderer;
    }

    @Override
    public void submit(PoseStack matrixStack, SubmitNodeCollector nodeCollector, int light, STATE state, float limbAngle, float limbDistance) {
        GenderRenderState genderRenderState = GenderRenderState.get(state);
        if (genderRenderState == null || !HumanoidArmorLayer.shouldRender(state.chestEquipment, EquipmentSlot.CHEST)) return;
        try {
            if (!setupRender(state, genderRenderState)) return;
            if (isArmorStand(state) && !genderRenderState.armor.armorStandsCopySettings()) return;

            renderSides(state, getParentModel(), genderRenderState, matrixStack, nodeCollector, light, OverlayTexture.NO_OVERLAY, this::renderArmor);
        } catch(Exception e) {
            WildfireGender.LOGGER.error("Failed to render breast armor", e);
        }
    }

    private boolean isArmorStand(STATE state) {
        //~ if >=26.2 'net.minecraft.world.entity.EntityType' -> 'net.minecraft.world.entity.EntityTypes'
        return state.entityType == net.minecraft.world.entity.EntityTypes.ARMOR_STAND;
    }

    private void renderArmor(STATE state, GenderRenderState genderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, BreastSide side) {
        Equippable equippable = Objects.requireNonNull(state.chestEquipment.get(DataComponents.EQUIPPABLE), "Chest slot is null after having not been null at the start of render call");
        EquipmentClientInfo.LayerType layerType = state.isBaby && !isArmorStand(state) ? EquipmentClientInfo.LayerType.HUMANOID_BABY : EquipmentClientInfo.LayerType.HUMANOID;
        var model = new BreastModel(side.forSide(lBoobArmor, rBoobArmor));
        equipmentRenderer.renderLayers(layerType, equippable.assetId().orElseThrow(), model, state, state.chestEquipment, poseStack, nodeCollector, lightCoords,
            null, state.outlineColor, genderState.hasJacketLayer ? 2 : 1);
    }

    @Override
    protected boolean isLayerVisible(STATE state, GenderRenderState genderState) {
        return genderState.armor.coversBreasts();
    }

    @Override
    protected void resizeBox(GenderRenderState state, float breastSize) {
        if (lBoobArmor != null && rBoobArmor != null && Objects.equals(textureData, state.armor.texture())) {
            return;
        }

        textureData = state.armor.texture();
        Vector2ic texSize = textureData.textureSize();
        UVMap uvs = textureData.uvs();

        lBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), -4F, 0.0F, 0F, 4, 5, 3, 0.0F, uvs.left());
        rBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), 0, 0.0F, 0F, 4, 5, 3, 0.0F, uvs.right());
    }

    @Override
    protected void setupTransformations(STATE state, MODEL model, GenderRenderState genderState, PoseStack matrixStack, BreastSide side) {
        super.setupTransformations(state, model, genderState, matrixStack, side);
        if (genderState.hasJacketLayer) {
            matrixStack.translate(0, 0, -0.015f);
            matrixStack.scale(1.05f, 1.05f, 1.05f);
        }
        matrixStack.translate(side.leftOrNegate(0.001f), 0.015f, -0.015f);
        matrixStack.scale(1.05f, 1, 1);
    }
}

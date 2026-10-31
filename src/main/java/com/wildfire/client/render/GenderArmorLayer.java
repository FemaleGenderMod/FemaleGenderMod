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

package com.wildfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildfire.api.IBreastArmorTexture;
import com.wildfire.api.IGenderArmor;
import com.wildfire.api.client.WildfireClientAPI;
import com.wildfire.api.uvs.UVMap;
import com.wildfire.client.WildfireClientHelper;
import com.wildfire.client.gui.FakeGUIPlayer;
import com.wildfire.client.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.entities.EntityConfigHolder;
import java.util.Objects;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.tags.ItemTags;
import net.minecraft.client.renderer.Sheets;
import net.neoforged.neoforge.client.ClientHooks;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector2ic;

/// @apiNote Only use this on the client side
public class GenderArmorLayer<ENTITY extends LivingEntity, MODEL extends HumanoidModel<ENTITY>> extends GenderLayer<ENTITY, MODEL> {

    @UnknownNullability("null until #resizeArmorBoxes() is first called")
    private BreastModelBox lBoobArmor, rBoobArmor;

    private IBreastArmorTexture textureData = IBreastArmorTexture.DEFAULT;

    private final TextureAtlas armorTrimAtlas;

    public GenderArmorLayer(RenderLayerParent<ENTITY, MODEL> render, ModelManager modelManager) {
        super(render);
        this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int lightCoords, ENTITY entity, float limbAngle,
        float limbDistance, float partialTicks, float animationProgress, float headYaw, float headPitch) {
        ItemStack armorStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem)) {
            return;
        }
        try {
            EntityConfigHolder<?> entityConfig = FakeGUIPlayer.isFakePlayer(entity)
                ? FakeGUIPlayer.getConfigOf(entity)
                : WildfireClientAPI.getConfig(entity);
            if (entityConfig == null) return;

            IGenderArmor genderArmor = WildfireClientHelper.getArmorConfig(armorStack);
            if (!genderArmor.coversBreasts()) return;
            if (entity instanceof ArmorStand && !genderArmor.armorStandsCopySettings()) return;

            if (!setupRender(entity, entityConfig, partialTicks, genderArmor)) return;
            resizeArmorBoxes(genderArmor);

            renderSides(entity, getParentModel(), matrixStack, bufferSource, lightCoords, OverlayTexture.NO_OVERLAY,
                (armorEntity, poseStack, buffers, light, overlay, side) ->
                    renderArmor(armorEntity, armorStack, poseStack, buffers, light, side));
        } catch(Exception e) {
            WildfireGender.LOGGER.error("Failed to render breast armor", e);
        }
    }

    private void renderArmor(ENTITY entity, ItemStack armorStack, PoseStack matrixStack, MultiBufferSource bufferSource,
        int lightCoords, BreastSide side) {
        matrixStack.pushPose();
        try {
            WildfireModelRenderer.BreastModelBox armor = side.forSide(lBoobArmor, rBoobArmor);

            Holder<ArmorMaterial> material = ((ArmorItem) armorStack.getItem()).getMaterial();
            int color = armorStack.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(armorStack, 0xFFA06540) : 0xFFFFFFFF;
            for (ArmorMaterial.Layer layer : material.value().layers()) {
                ResourceLocation armorTexture = ClientHooks.getArmorTexture(entity, armorStack, layer, false, EquipmentSlot.CHEST);

                RenderType armorType = RenderType.armorCutoutNoCull(armorTexture);
                VertexConsumer armorVertexConsumer = bufferSource.getBuffer(armorType);
                renderBox(armor, matrixStack, armorVertexConsumer, lightCoords, OverlayTexture.NO_OVERLAY, layer.dyeable() ? color : 0xFFFFFFFF);
            }

            ArmorTrim trim = armorStack.get(DataComponents.TRIM);
            if (trim != null) {
                TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(trim.outerTexture(material));
                VertexConsumer trimVertexConsumer = sprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
                renderBox(armor, matrixStack, trimVertexConsumer, lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }

            if (armorStack.hasFoil()) {
                renderBox(armor, matrixStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        } finally {
            matrixStack.popPose();
        }
    }

    @Override
    protected void setupTransformations(ENTITY entity, MODEL model, PoseStack matrixStack, BreastSide side) {
        super.setupTransformations(entity, model, matrixStack, side);
        if (hasJacketLayer) {
            shiftForJacket(matrixStack);
        }
        matrixStack.translate(side.leftOrNegate(0.001f), 0.015f, -0.015f);
        matrixStack.scale(1.05f, 1, 1);
    }

    private void resizeArmorBoxes(IGenderArmor genderArmor) {
        if (lBoobArmor != null && rBoobArmor != null && Objects.equals(textureData, genderArmor.texture())) {
            return;
        }

        textureData = genderArmor.texture();
        Vector2ic texSize = textureData.textureSize();
        UVMap uvs = textureData.uvs();

        lBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), -4F, 0.0F, 0F, 4, 5, 3, 0.0F, uvs.left());
        rBoobArmor = new BreastModelBox(texSize.x(), texSize.y(), 0, 0.0F, 0F, 4, 5, 3, 0.0F, uvs.right());
    }
}

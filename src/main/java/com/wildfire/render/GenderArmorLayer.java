package com.wildfire.render;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.EntityConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;

public class GenderArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends GenderLayer<T, M> {
    private final TextureAtlas armorTrimsAtlas;

    protected static final WildfireModelRenderer.BreastModelBox lBoobArmor = new WildfireModelRenderer.BreastModelBox(64, 32, 16, 17, -4.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);
    protected static final WildfireModelRenderer.BreastModelBox rBoobArmor = new WildfireModelRenderer.BreastModelBox(64, 32, 20, 17, 0.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);
    protected static final WildfireModelRenderer.BreastModelBox lTrim = new WildfireModelRenderer.BreastModelBox(64, 32, 16, 17, -4.0F, 0.0F, 0.0F, 4, 5, 4, 0.001F, false);
    protected static final WildfireModelRenderer.BreastModelBox rTrim = new WildfireModelRenderer.BreastModelBox(64, 32, 20, 17, 0.0F, 0.0F, 0.0F, 4, 5, 4, 0.001F, false);

    public GenderArmorLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.armorTrimsAtlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int light, @NotNull T entity, float limbAngle, float limbDistance, float partialTicks, float animationProgress, float headYaw, float headPitch) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST);

        if (!chestplate.isEmpty() && chestplate.getItem() instanceof ArmorItem armorItem) {
            try {
                EntityConfig config = EntityConfig.getEntity(entity);
                if (config == null || !this.setupRender(entity, config, partialTicks)) return;

                if (entity instanceof ArmorStand && !this.genderArmor.armorStandsCopySettings()) return;

                Holder<ArmorMaterial> material = armorItem.getMaterial();
                int color = DyedItemColor.getOrDefault(chestplate, -1);
                boolean hasGlint = chestplate.hasFoil();

                // CORRECCIÓN: Asegúrate de que el lambda reciba 'matrixStack' correctamente
                this.renderSides(entity, this.getParentModel(), matrixStack, (side) -> {
                    material.value().layers().forEach((layer) -> {
                        // 1.21.1 utiliza 'dyeable()' en lugar de 'useTint()'
                        int layerColor = layer.dyeable() ? color : -1;
                        this.renderBreastArmor(layer.texture(false), matrixStack, buffer, light, side, layerColor, hasGlint);
                    });

                    // 1.21.1 utiliza DataComponents.TRIM
                    ArmorTrim trim = chestplate.get(DataComponents.TRIM);
                    if (trim != null) {
                        this.renderArmorTrim(material, matrixStack, buffer, light, trim, hasGlint, side);
                    }
                });
            } catch (Exception e) {
                WildfireGender.LOGGER.error("Failed to render breast armor layer", e);
            }
        }
    }

    /**
     * CORRECCIÓN: Esta firma debe ser EXACTA a la de GenderLayer.
     */
    @Override
    protected void setupTransformations(T entity, M model, PoseStack matrixStack, BreastSide side) {
        super.setupTransformations(entity, model, matrixStack, side);

        boolean hasJacket = false;
        if (entity instanceof AbstractClientPlayer player) {
            hasJacket = player.isModelPartShown(PlayerModelPart.JACKET);
        } else if (entity instanceof ArmorStand) {
            EntityConfig config = EntityConfig.getEntity(entity);
            hasJacket = config != null && config.hasJacketLayer();
        }

        if (hasJacket) {
            matrixStack.translate(0.0F, 0.0F, -0.015F);
            matrixStack.scale(1.05F, 1.05F, 1.05F);
        }

        matrixStack.translate(side.isLeft ? 0.001F : -0.001F, 0.015F, -0.015F);
        matrixStack.scale(1.05F, 1.0F, 1.0F);
    }

    protected void renderBreastArmor(ResourceLocation texture, PoseStack matrixStack, MultiBufferSource buffer, int light, BreastSide side, int color, boolean glint) {
        WildfireModelRenderer.BreastModelBox box = side.isLeft ? lBoobArmor : rBoobArmor;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));

        // El método renderBox es estático en GenderLayer, asegúrate de que use los mismos imports
        GenderLayer.renderBox(box, matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
    }

    protected void renderArmorTrim(Holder<ArmorMaterial> material, PoseStack matrixStack, MultiBufferSource buffer, int light, ArmorTrim trim, boolean glint, BreastSide side) {
        WildfireModelRenderer.BreastModelBox trimBox = side.isLeft ? lTrim : rTrim;

        // 1.21.1 utiliza 'outerTexture' para obtener la textura del trim
        TextureAtlasSprite sprite = this.armorTrimsAtlas.getSprite(trim.outerTexture(material));
        VertexConsumer vertexConsumer = sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet(false)));

        GenderLayer.renderBox(trimBox, matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -1);

        if (glint) {
            VertexConsumer glintConsumer = buffer.getBuffer(RenderType.armorEntityGlint());
            GenderLayer.renderBox(trimBox, matrixStack, glintConsumer, light, OverlayTexture.NO_OVERLAY, -1);
        }
    }

    /**
     * Método público para permitir que otros mods rendericen armaduras personalizadas en el modelo de pecho.
     * Este método permite renderizar armaduras con texturas personalizadas, independientemente del tipo.
     * @param entity La entidad que lleva la armadura
     * @param texture La textura de la armadura a renderizar
     * @param matrixStack La pila de matrices para transformaciones
     * @param buffer El buffer de renderizado
     * @param light La iluminación
     * @param side El lado del pecho (izquierdo o derecho)
     * @param color El color de tinte de la armadura (-1 si no tiene)
     * @param glint Si la armadura tiene efecto de brillo
     * @param partialTicks Ticks parciales para animación
     */
    public void renderCustomArmor(T entity, ResourceLocation texture, PoseStack matrixStack, MultiBufferSource buffer, int light, BreastSide side, int color, boolean glint, float partialTicks) {
        EntityConfig config = EntityConfig.getEntity(entity);
        if (config == null || !this.setupRender(entity, config, partialTicks)) return;

        if (entity instanceof ArmorStand && !this.genderArmor.armorStandsCopySettings()) return;

        this.setupTransformations(entity, this.getParentModel(), matrixStack, side);
        this.renderBreastArmor(texture, matrixStack, buffer, light, side, color, glint);
    }
}
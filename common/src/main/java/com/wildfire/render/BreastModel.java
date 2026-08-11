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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Util;

public class BreastModel extends Model<HumanoidRenderState> {
    private static final ModelPart DUMMY_PART = Util.make(() -> {
        var part = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        part.getRoot().clearRecursively();
        return part.getRoot().bake(64, 64);
    });

    private final WildfireModelRenderer.ModelBox model;

    public BreastModel(WildfireModelRenderer.ModelBox model) {
        super(DUMMY_PART, RenderTypes::entityCutout);
        this.model = model;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        WildfireModelRenderer.renderBox(model, poseStack.last(), vertexConsumer, light, overlay, color);
    }
}

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

package com.wildfire.main;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.wildfire.api.IGenderArmor;
import com.wildfire.api.WildfireAPI;
import com.wildfire.main.config.types.FloatConfigKey;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import com.wildfire.resources.GenderArmorResourceManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TriState;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class WildfireHelper {
    private WildfireHelper() {
        throw new UnsupportedOperationException();
    }

    public static final PrimitiveCodec<TriState> TRISTATE = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<TriState> read(final DynamicOps<T> ops, final T input) {
            return DataResult.success(ops.getBooleanValue(input)
                    .map(v -> v ? TriState.TRUE : TriState.FALSE)
                    .result().orElse(TriState.DEFAULT));
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final TriState value) {
            if(value == TriState.DEFAULT) {
                return ops.empty();
            }
            return ops.createBoolean(value == TriState.TRUE);
        }

        @Override
        public String toString() {
            return "TriState";
        }
    };

    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, (double) max + 1);
    }

    @SuppressWarnings("removal")
    @Environment(EnvType.CLIENT)
    public static IGenderArmor getArmorConfig(ItemStack stack) {
        if(stack.isEmpty()) {
            return IGenderArmor.EMPTY;
        }

        return GenderArmorResourceManager.get(stack).orElseGet(() -> {
            var fallback = stack.contains(DataComponentTypes.EQUIPPABLE) ? IGenderArmor.DEFAULT : IGenderArmor.EMPTY;
            return WildfireAPI.getGenderArmors().getOrDefault(stack.getItem(), fallback);
        });
    }

    public static Codec<Float> boundedFloat(float minInclusive, float maxInclusive) {
        return Codec.FLOAT.xmap(val -> MathHelper.clamp(val, minInclusive, maxInclusive), Function.identity());
    }

    public static Codec<Float> boundedFloat(FloatConfigKey configKey) {
        return boundedFloat(configKey.getMinInclusive(), configKey.getMaxInclusive());
    }

    public static String getModVersion(String modId) {
        var mod = FabricLoader.getInstance().getModContainer(modId).orElseThrow();
        return mod.getMetadata().getVersion().getFriendlyString();
    }

    public static boolean onClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static void logCommand(CommandContext<FabricClientCommandSource> ctx, String text) {

        Text fgmText =
            Text.literal("F")
                .formatted(Formatting.LIGHT_PURPLE)
            .append(Text.literal("GM")
                .formatted(Formatting.WHITE)
            );

        ctx.getSource().sendFeedback(Text.literal("[").formatted(Formatting.GRAY).append(fgmText).append(Text.literal("] ").formatted(Formatting.GRAY)).formatted(Formatting.RESET).append(Text.literal(text)));
    }
}

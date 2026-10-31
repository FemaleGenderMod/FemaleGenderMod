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

package com.wildfire.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wildfire.api.server.WildfireServerAPI;
import com.wildfire.common.LoaderAgnostics;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.entities.BreastDataComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.ChatFormatting;

public final class WildfireServerCommand extends AbstractWildfireCommand<ServerCommandHelper, CommandSourceStack> {

    public WildfireServerCommand(final ServerCommandHelper helper) {
        super(helper);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var node = helper.literal("fgmserver")
            .requires(helper.requirePermissions(ServerCommandHelper.COMMAND_PERMISSION))
            .executes(this::syncStats);

        // only register these in a dev env as these debug commands are just used as convenient shorthands to
        // actions that could otherwise be done manually
        if(LoaderAgnostics.INSTANCE.isDevelopmentEnv()) {
            node.then(createDebugNode());
        }

        dispatcher.register(node);
    }

    private LiteralArgumentBuilder<CommandSourceStack> createDebugNode() {
        return helper.literal("debug")
            .executes(ctx -> {
                sendHelp(ctx, WildfireLang.DEBUG_COMMAND,
                    WildfireLang.COMMAND_TRIM,
                    WildfireLang.COMMAND_ARMOR_STAND
                );
                return Command.SINGLE_SUCCESS;
            })
            .then(helper.literal("trim")
                .then(helper.argument("glint", BoolArgumentType.bool())
                    .executes(this::equipTrimmedChestplate))
                .executes(this::equipTrimmedChestplate))
            .then(helper.literal("armorstand")
                .executes(this::spawnArmorStand));
    }

    private int syncStats(CommandContext<CommandSourceStack> ctx) {
        helper.sendSystemMessage(ctx.getSource(), WildfireLang.GENERIC_COMMA.translateColored(ChatFormatting.GRAY,
            WildfireLang.MOD_NAME.translateColored(ChatFormatting.LIGHT_PURPLE),
            WildfireLang.COMMAND_VERSION_INFO.translateColored(ChatFormatting.GRAY,
                Component.literal(WildfireGender.getModVersion()).withStyle(ChatFormatting.GREEN))));

        final var server = ctx.getSource().getServer();
        final int online = server.getPlayerList().getPlayerCount();
        int synced = 0;
        for(var player : server.getPlayerList().getPlayers()) {
            if(WildfireServerAPI.players().get(player) != null) {
                synced++;
            }
        }

        helper.sendSystemMessage(ctx.getSource(), WildfireLang.COMMAND_SYNCED_PLAYER_COUNT.translateColored(ChatFormatting.GRAY,
            Component.literal(String.valueOf(synced)).withStyle(ChatFormatting.GREEN),
            Component.literal(String.valueOf(online)).withStyle(ChatFormatting.GREEN)));

        return Command.SINGLE_SUCCESS;
    }

    private int equipTrimmedChestplate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var item = new ItemStack(Items.IRON_CHESTPLATE);
        Boolean glint = getOrDefault(ctx, "glint", null, Boolean.class);

        var material = player.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL).getHolderOrThrow(TrimMaterials.AMETHYST);
        var pattern = player.registryAccess().registryOrThrow(Registries.TRIM_PATTERN).getHolderOrThrow(TrimPatterns.COAST);
        item.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        if(glint != null) {
            item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
        }
        player.setItemSlot(EquipmentSlot.CHEST, item);

        return Command.SINGLE_SUCCESS;
    }

    private int spawnArmorStand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var level = player.level();

        var item = new ItemStack(Items.IRON_CHESTPLATE);
        var config = WildfireServerAPI.players().getOrCreate(player);
        var component = BreastDataComponent.fromPlayer(player, config);
        if(component == null) {
            helper.sendFailure(ctx.getSource(), WildfireLang.COMMAND_ARMOR_STAND_NO_COMPONENT.translateColored(ChatFormatting.RED));
            return 0;
        }
        component.write(item);

        var stand = new ArmorStand(level, player.getBlockX(), player.getBlockY(), player.getBlockZ());
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        stand.setItemSlot(EquipmentSlot.CHEST, item);
        stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        level.addFreshEntity(stand);

        return Command.SINGLE_SUCCESS;
    }
}

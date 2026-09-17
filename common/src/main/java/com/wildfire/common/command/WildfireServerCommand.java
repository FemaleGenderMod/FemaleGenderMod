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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wildfire.api.server.WildfireServerAPI;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.entities.BreastDataComponent;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public final class WildfireServerCommand<S extends SharedSuggestionProvider> extends AbstractWildfireCommand<ServerCommandHelper<S>, S> {
    public WildfireServerCommand(final ServerCommandHelper<S> helper) {
        super(helper);
    }

    public void register(CommandDispatcher<S> dispatcher) {
        dispatcher.register(helper.literal("fgmserver")
            .requires(helper::hasCommandPermission)
            .executes(this::syncStats)
            //<editor-fold desc="Debug">
            .then(helper.literal("debug")
                .requires(helper::hasDebugCommandPermission)
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
                    .executes(this::spawnArmorStand)))
            //</editor-fold>
        );
    }

    private int syncStats(CommandContext<S> ctx) {
        helper.sendSystemMessage(ctx.getSource(), WildfireLang.GENERIC_COMMA.translateColored(TextColor.GRAY,
            WildfireLang.MOD_NAME.translateColored(TextColor.LIGHT_PURPLE),
            WildfireLang.COMMAND_VERSION_INFO.translateColored(TextColor.GRAY,
                Component.literal(WildfireGender.getModVersion()).withColor(TextColor.GREEN))));

        final var server = helper.getServer(ctx.getSource());
        final int online = server.getPlayerList().getPlayerCount();
        int synced = 0;
        for(var player : server.getPlayerList().getPlayers()) {
            if(WildfireServerAPI.players().get(player) != null) {
                synced++;
            }
        }

        helper.sendSystemMessage(ctx.getSource(), WildfireLang.COMMAND_SYNCED_PLAYER_COUNT.translateColored(TextColor.GRAY,
            Component.literal(String.valueOf(synced)).withColor(TextColor.GREEN),
            Component.literal(String.valueOf(online)).withColor(TextColor.GREEN)));

        return Command.SINGLE_SUCCESS;
    }

    private int equipTrimmedChestplate(CommandContext<S> ctx) throws CommandSyntaxException {
        var player = helper.getPlayer(ctx.getSource());
        var item = new ItemStack(Items.IRON_CHESTPLATE);
        var glint = getOrDefault(ctx, "glint", null, Boolean.class);

        var material = player.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.AMETHYST);
        var pattern = player.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.COAST);
        item.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        if(glint != null) {
            item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
        }
        player.setItemSlot(EquipmentSlot.CHEST, item);

        return Command.SINGLE_SUCCESS;
    }

    private int spawnArmorStand(CommandContext<S> ctx) throws CommandSyntaxException {
        var player = helper.getPlayer(ctx.getSource());
        var world = player.level();

        var item = new ItemStack(Items.IRON_CHESTPLATE);
        var config = WildfireServerAPI.players().getOrCreate(player);
        var component = BreastDataComponent.fromPlayer(player, config);
        if (component == null) {
            helper.sendFailure(ctx.getSource(), WildfireLang.COMMAND_ARMOR_STAND_NO_COMPONENT.translateColored(TextColor.RED));
            return 0;
        }
        component.write(item);

        var stand = new ArmorStand(world, player.getBlockX(), player.getBlockY(), player.getBlockZ());
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        stand.setItemSlot(EquipmentSlot.CHEST, item);
        stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        world.addFreshEntity(stand);

        return Command.SINGLE_SUCCESS;
    }
}

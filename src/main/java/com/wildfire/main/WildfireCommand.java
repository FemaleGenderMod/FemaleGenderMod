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

import com.google.common.cache.Cache;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.wildfire.gui.screen.WardrobeBrowserScreen;
import com.wildfire.gui.screen.WildfireFirstTimeSetupScreen;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.entitydata.BreastDataComponent;
import com.wildfire.main.entitydata.EntityConfigHolder;
import com.wildfire.main.entitydata.PlayerConfigHolder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/// @apiNote Only use this on the client side
public class WildfireCommand {

    //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor' {
    private static final Component COMMAND_PREFIX = WildfireLang.GENERIC_BRACKETS.translateColored(TextColor.GRAY, WildfireLang.GENERIC_CONCAT.translate(
        WildfireLang.MISC_F.translateColored(TextColor.LIGHT_PURPLE),
        WildfireLang.MISC_GM.translateColored(TextColor.WHITE)
    ));
    //~}

    static void init() {
        ClientCommandRegistrationCallback.EVENT.register(WildfireCommand::register);
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        Minecraft client = Minecraft.getInstance();

        var debug = ClientCommands.literal("debug")
                .executes(ctx -> {
                    sendHelp(ctx, WildfireLang.DEBUG_COMMAND,
                        WildfireLang.COMMAND_INVALIDATE_CACHE,
                        WildfireLang.COMMAND_TARGET,
                        WildfireLang.COMMAND_CACHE,
                        WildfireLang.COMMAND_FIRST_TIME,
                        WildfireLang.COMMAND_SYNC_VERBOSITY
                    );
                    ctx.getSource().sendFeedback(CommonComponents.EMPTY);
                    sendHelp(ctx, WildfireLang.SINGLE_PLAYER_COMMAND,
                        WildfireLang.COMMAND_TRIM,
                        WildfireLang.COMMAND_ARMOR_STAND
                    );
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommands.literal("invalidatecache")
                        .executes(WildfireCommand::invalidateCache))
                .then(ClientCommands.literal("target")
                        .executes(WildfireCommand::getEntityLookingAt))
                .then(ClientCommands.literal("firsttime")
                        .executes(_ -> {
                            client.execute(() -> {
                                //~ if >=26.2 'client.setScreen' -> 'client.gui.setScreen'
                                client.schedule(() -> client.gui.setScreen(new WildfireFirstTimeSetupScreen(null, client.player.getUUID())));
                            });
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(ClientCommands.literal("cache")
                        .then(ClientCommands.argument("allPlayers", BoolArgumentType.bool())
                                .executes(WildfireCommand::getUsers)
                                .then(ClientCommands.argument("showEntities", BoolArgumentType.bool())
                                        .executes(WildfireCommand::getUsers)))
                        .executes(WildfireCommand::getUsers))
                .then(ClientCommands.literal("syncverbosity")
                        .then(ClientCommands.argument("level", new SyncVerbosity.SyncVerbosityArgumentType())
                                .executes(WildfireCommand::setLogLevel)));

        if(Minecraft.getInstance().isLocalServer()) {
            debug
                    .then(ClientCommands.literal("trim")
                            .then(ClientCommands.argument("glint", BoolArgumentType.bool())
                                    .executes(WildfireCommand::equipTrimmedChestplate))
                            .executes(WildfireCommand::equipTrimmedChestplate))
                    .then(ClientCommands.literal("armorstand").executes(WildfireCommand::spawnArmorStand));
        }

        var root = dispatcher.register(ClientCommands.literal("femalegender")
                .executes(WildfireCommand::openConfig)
                .then(debug));

        dispatcher.register(ClientCommands.literal("fgm")
                .executes(WildfireCommand::openConfig)
                .redirect(root));
    }

    @SuppressWarnings("SameParameterValue")
    @UnknownNullability("nullability depends on the relevant ArgumentType & defaultValue")
    private static <T> T getOrDefault(CommandContext<FabricClientCommandSource> ctx, String name, @UnknownNullability T defaultValue, Class<T> clazz) {
        T value = defaultValue;
        try {
            value = ctx.getArgument(name, clazz);
        } catch(IllegalArgumentException _) {}
        return value;
    }

    public static void send(CommandContext<FabricClientCommandSource> ctx, Component text) {
        ctx.getSource().sendFeedback(WildfireLang.GENERIC_SPACE.translate(COMMAND_PREFIX, text));
    }

    public static void sendHelp(CommandContext<FabricClientCommandSource> ctx, WildfireLang header, WildfireLang... usageToDescription) {
        List<Component> lines = new ArrayList<>();
        lines.add(WildfireLang.GENERIC_SPACE.translate(COMMAND_PREFIX, header.translate().withStyle(style -> style.withUnderlined(true))));

        for (WildfireLang langEntry : usageToDescription) {
            //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor' {
            lines.add(WildfireLang.GENERIC_SPACE.translate(COMMAND_PREFIX, WildfireLang.GENERIC_DASH_EXPLANATION.translateColored(TextColor.GRAY,
                langEntry.translateColored(TextColor.AQUA),
                //~}
                //~ if >=26.2 'withStyle(net.minecraft.ChatFormatting.' -> 'withColor(TextColor.'
                langEntry.translateDescription().withColor(TextColor.WHITE)
            )));

        }

        ctx.getSource().sendFeedback(ComponentUtils.formatList(lines, CommonComponents.NEW_LINE));
    }

    private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
        final var client = ctx.getSource().getClient();
        final var player = ctx.getSource().getPlayer();
        // the .schedule() is necessary as otherwise the chat screen will simply immediately close the opened screen
        client.schedule(() -> WardrobeBrowserScreen.open(client, player));
        return Command.SINGLE_SUCCESS;
    }

    private static int getEntityLookingAt(CommandContext<FabricClientCommandSource> ctx) {
        var target = ctx.getSource().getClient().crosshairPickEntity;

        if(target != null) {
            send(ctx, WildfireLang.COMMAND_LOOKING_AT.translate(target.getName()));
            send(ctx, WildfireLang.COMMAND_LOOKING_AT_UUID.translate(target.getStringUUID()));
            send(ctx, WildfireLang.COMMAND_LOOKING_AT_TYPE.translate(target.getType()));
            send(ctx, WildfireLang.COMMAND_LOOKING_AT_CLASS.translate(target.getClass()));
            send(ctx, WildfireLang.COMMAND_LOOKING_AT_RENDERER.translate( Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(target)));
        } else {
            send(ctx, WildfireLang.COMMAND_LOOKING_AT_NONE.translate());
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int setLogLevel(CommandContext<FabricClientCommandSource> ctx) {
        SyncVerbosity level = ctx.getArgument("level", SyncVerbosity.class);

        if (ClientConfig.config().syncVerbosity.update(level)) {//Should always be true
            ClientConfig.save();

            send(ctx, WildfireLang.COMMAND_LOG_LEVEL.translate(level));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int getUsers(CommandContext<FabricClientCommandSource> ctx) {
        boolean allPlayers = getOrDefault(ctx, "allPlayers", false, Boolean.class);
        boolean showEntities = getOrDefault(ctx, "showEntities", false, Boolean.class);

        var players = dump(WildfireGender.CACHE, ctx.getSource().getLevel(), !allPlayers);
        if(!players.isEmpty()) {
            send(ctx, WildfireLang.COMMAND_SYNCED_PLAYERS.translate(players.size()));
            for(var line : players) {
                send(ctx, line);
            }
        }

        if(showEntities) {
            var entities = dump(EntityConfigHolder.CACHE, ctx.getSource().getLevel(), false);
            if(!entities.isEmpty()) {
                send(ctx, WildfireLang.COMMAND_ENTITIES.translate(entities.size()));
                for(var line : entities) {
                    send(ctx, line);
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static List<Component> dump(Cache<UUID, ? extends EntityConfigHolder<?>> cache, Level world, boolean ignoreEmptyConfig) {
        List<Component> lines = new ArrayList<>();
        for(var entry : cache.asMap().entrySet()) {
            var uuid = entry.getKey();
            var config = entry.getValue();
            if(config == null) {
                continue;
            }
            if(config instanceof PlayerConfigHolder playerConfig && playerConfig.getSyncStatus() == PlayerConfigHolder.SyncStatus.UNKNOWN && ignoreEmptyConfig) {
                continue;
            }
            var entity = world.getEntity(uuid);
            if(entity == null) continue;

            var info = ComponentUtils.formatList(config.getDebugInfo(), CommonComponents.NEW_LINE, Component::literal);

            lines.add(WildfireLang.GENERIC_DASH_EXPLANATION.translate(entity.getDisplayName(), config.gender().get().getDisplayName())
                    .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(info))));
        }
        return lines;
    }

    private static int invalidateCache(CommandContext<FabricClientCommandSource> ctx) {
        WildfireGender.CACHE.invalidateAll();
        EntityConfigHolder.CACHE.invalidateAll();

        send(ctx, WildfireLang.COMMAND_INVALIDATE_CACHE_SUCCESS.translate());
        return Command.SINGLE_SUCCESS;
    }

    /// Takes a client-sided [CommandContext] and returns the [ServerPlayer] for the invoking player
    /// when in singleplayer, or throws an error.
    private static ServerPlayer getIntegratedServerPlayer(CommandContext<FabricClientCommandSource> ctx) {
        var integratedServer = Objects.requireNonNull(Minecraft.getInstance().getSingleplayerServer());
        var playerManager = Objects.requireNonNull(integratedServer.getPlayerList());
        return Objects.requireNonNull(playerManager.getPlayer(ctx.getSource().getPlayer().getUUID()));
    }

    private static int equipTrimmedChestplate(CommandContext<FabricClientCommandSource> ctx) {
        Boolean glint = getOrDefault(ctx, "glint", null, Boolean.class);
        var player = getIntegratedServerPlayer(ctx);
        if(!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return 0;
        var item = new ItemStack(Items.IRON_CHESTPLATE);
        var material = player.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.AMETHYST);
        var pattern = player.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.COAST);
        item.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
        if(glint != null) {
            item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
        }
        player.setItemSlot(EquipmentSlot.CHEST, item);
        return Command.SINGLE_SUCCESS;
    }

    private static int spawnArmorStand(CommandContext<FabricClientCommandSource> ctx) {
        var player = getIntegratedServerPlayer(ctx);
        if(!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return 0;
        var world = player.level();

        var item = new ItemStack(Items.IRON_CHESTPLATE);
        var config = WildfireGender.getOrAddPlayerById(player.getUUID());
        var component = BreastDataComponent.fromPlayer(player, config);
        if(component == null) {
            ctx.getSource().sendError(WildfireLang.COMMAND_ARMOR_STAND_NO_COMPONENT.translate());
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

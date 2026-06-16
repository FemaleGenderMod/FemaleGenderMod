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
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
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

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

@Environment(EnvType.CLIENT)
public class WildfireCommand {
    private static final Component COMMAND_PREFIX = Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("F").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("GM").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("] ").withStyle(ChatFormatting.GRAY));

    static void init() {
        ClientCommandRegistrationCallback.EVENT.register(WildfireCommand::register);
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        Minecraft client = Minecraft.getInstance();

        var debug = ClientCommands.literal("debug")
                .executes((ctx) -> {
                    sendHelp(ctx, Component.literal("Debug Commands:"),
                            "invalidatecache", "Clears the player & entity caches",
                            "target", "Show debug info for entity you are looking at",
                            "cache [allPlayers] [showEntities]", "Display cached entities/players",
                            "firsttime", "Display the first time setup screen",
                            "syncverbosity [level]", "Change how verbose the sync log is");
                    ctx.getSource().sendFeedback(Component.empty());
                    sendHelp(ctx, Component.literal("Singleplayer Commands:"),
                            "trim [glint]", "Equips a chestplate with a trim pre-applied onto yourself",
                            "armorstand", "Spawns an armor stand with armor copying your breast settings pre-equipped");
                    return 1;
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
                        .then(argument("allPlayers", BoolArgumentType.bool())
                                .executes(WildfireCommand::getUsers)
                                .then(argument("showEntities", BoolArgumentType.bool())
                                        .executes(WildfireCommand::getUsers)))
                        .executes(WildfireCommand::getUsers))
                .then(ClientCommands.literal("syncverbosity")
                        .then(argument("level", new SyncVerbosity.SyncVerbosityArgumentType())
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

    public static void send(CommandContext<FabricClientCommandSource> ctx, String text) {
        ctx.getSource().sendFeedback(Component.empty().append(COMMAND_PREFIX).append(text));
    }

    public static void send(CommandContext<FabricClientCommandSource> ctx, Component text) {
        ctx.getSource().sendFeedback(Component.empty().append(COMMAND_PREFIX).append(text));
    }

    public static void sendHelp(CommandContext<FabricClientCommandSource> ctx, Component header, String... nameToDescription) {
        assert nameToDescription.length % 2 == 0;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty().append(COMMAND_PREFIX).append(header).withStyle(ChatFormatting.UNDERLINE));

        for(int i = 0; i < nameToDescription.length / 2; i++) {
            var name = nameToDescription[i * 2];
            var description = nameToDescription[(i * 2) + 1];
            lines.add(Component.empty().append(COMMAND_PREFIX)
                .append(Component.literal(name).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(description)));
        }

        ctx.getSource().sendFeedback(ComponentUtils.formatList(lines, Component.literal("\n")));
    }

    private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
        final var client = ctx.getSource().getClient();
        final var player = ctx.getSource().getPlayer();
        // the .schedule() is necessary as otherwise the chat screen will simply immediately close the opened screen
        client.schedule(() -> WardrobeBrowserScreen.open(client, player));
        return 1;
    }

    private static int getEntityLookingAt(CommandContext<FabricClientCommandSource> ctx) {
        var target = ctx.getSource().getClient().crosshairPickEntity;

        if(target != null) {
            send(ctx, "Looking at: " + target.getName().getString());
            send(ctx, "UUID: " + target.getStringUUID());
            send(ctx, "Type: " + target.getType());
            send(ctx, "Class: " + target.getClass());
            send(ctx, "Renderer: " + Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(target));
        } else {
            send(ctx, "No entity in sight.");
        }
        return 1;
    }

    public static int setLogLevel(CommandContext<FabricClientCommandSource> ctx) {
        SyncVerbosity level = ctx.getArgument("level", SyncVerbosity.class);

        ClientConfig.INSTANCE.set(ClientConfig.SYNC_VERBOSITY, level);
        ClientConfig.INSTANCE.save();

        send(ctx, "Log level set to: " + level);
        return 1;
    }

    private static int getUsers(CommandContext<FabricClientCommandSource> ctx) {
        boolean allPlayers = getOrDefault(ctx, "allPlayers", false, Boolean.class);
        boolean showEntities = getOrDefault(ctx, "showEntities", false, Boolean.class);

        var players = dump(WildfireGender.CACHE, ctx.getSource().getLevel(), !allPlayers);
        if(!players.isEmpty()) {
            send(ctx, "Synced Players (" + players.size() + "):");
            for(var line : players) {
                send(ctx, line);
            }
        }

        if(showEntities) {
            var entities = dump(EntityConfig.CACHE, ctx.getSource().getLevel(), false);
            if(!entities.isEmpty()) {
                send(ctx, "Entities (" + players.size() + "):");
                for(var line : entities) {
                    send(ctx, line);
                }
            }
        }

        return 1;
    }

    private static List<Component> dump(Cache<UUID, ? extends EntityConfig> cache, Level world, boolean ignoreEmptyConfig) {
        List<Component> lines = new ArrayList<>();
        for(var entry : cache.asMap().entrySet()) {
            var uuid = entry.getKey();
            var config = entry.getValue();
            if(config == null) {
                continue;
            }
            if(config instanceof PlayerConfig playerConfig && playerConfig.getSyncStatus() == PlayerConfig.SyncStatus.UNKNOWN && ignoreEmptyConfig) {
                continue;
            }
            var entity = world.getEntity(uuid);
            if(entity == null) continue;

            var info = ComponentUtils.formatList(config.getDebugInfo(), Component.literal("\n"), Component::literal);

            lines.add(Component.empty()
                    .append(entity.getDisplayName())
                    .append(" - ")
                    .append(config.getGender().getDisplayName())
                    .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(info))));
        }
        return lines;
    }

    private static int invalidateCache(CommandContext<FabricClientCommandSource> ctx) {
        WildfireGender.CACHE.invalidateAll();
        EntityConfig.CACHE.invalidateAll();

        send(ctx, "Cache has been invalidated!");
        return 1;
    }

    /**
     * Takes a client-sided {@link CommandContext} and returns the {@link ServerPlayer} for the invoking player
     * when in singleplayer, or throws an error.
     */
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
        return 1;
    }

    private static int spawnArmorStand(CommandContext<FabricClientCommandSource> ctx) {
        var player = getIntegratedServerPlayer(ctx);
        if(!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return 0;
        var world = player.level();

        var item = new ItemStack(Items.IRON_CHESTPLATE);
        var config = WildfireGender.getOrAddPlayerById(player.getUUID());
        var component = BreastDataComponent.fromPlayer(player, config);
        if(component == null) {
            ctx.getSource().sendError(Component.literal("Returned breast data component was null; do you have Hide in Armor on?"));
            return 0;
        }
        component.write(item);

        var stand = new ArmorStand(world, player.getBlockX(), player.getBlockY(), player.getBlockZ());
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        stand.setItemSlot(EquipmentSlot.CHEST, item);
        stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        world.addFreshEntity(stand);

        return 1;
    }
}

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
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.item.equipment.trim.ArmorTrimPatterns;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

@Environment(EnvType.CLIENT)
public class WildfireCommand {
	private static final Text COMMAND_PREFIX = Text.empty()
			.append(Text.literal("[").formatted(Formatting.GRAY))
			.append(Text.literal("F").formatted(Formatting.LIGHT_PURPLE))
			.append(Text.literal("GM").formatted(Formatting.WHITE))
			.append(Text.literal("] ").formatted(Formatting.GRAY));

	static void init() {
		ClientCommandRegistrationCallback.EVENT.register(WildfireCommand::register);
	}

	private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registry) {
		MinecraftClient client = MinecraftClient.getInstance();

		var debug = ClientCommandManager.literal("debug")
				.executes((ctx) -> {
					sendHelp(ctx, Text.literal("Debug Commands:"),
							"invalidatecache", "Clears the player & entity caches",
							"target", "Show debug info for entity you are looking at",
							"cache [allPlayers] [showEntities]", "Display cached entities/players",
							"firsttime", "Display the first time setup screen",
							"syncverbosity [level]", "Change how verbose the sync log is");
					ctx.getSource().sendFeedback(Text.empty());
					sendHelp(ctx, Text.literal("Singleplayer Commands:"),
							"trim [glint]", "Equips a chestplate with a trim pre-applied onto yourself",
							"armorstand", "Spawns an armor stand with armor copying your breast settings pre-equipped");
					return 1;
				})
				.then(ClientCommandManager.literal("invalidatecache")
						.executes(WildfireCommand::invalidateCache))
				.then(ClientCommandManager.literal("target")
						.executes(WildfireCommand::getEntityLookingAt))
				.then(ClientCommandManager.literal("firsttime")
						.executes(ctx -> {
							client.execute(() -> {
								client.send(() -> client.setScreen(new WildfireFirstTimeSetupScreen(null, client.player.getUuid())));
							});
							return Command.SINGLE_SUCCESS;
						}))
				.then(ClientCommandManager.literal("cache")
						.then(argument("allPlayers", BoolArgumentType.bool())
								.executes(WildfireCommand::getUsers)
								.then(argument("showEntities", BoolArgumentType.bool())
										.executes(WildfireCommand::getUsers)))
						.executes(WildfireCommand::getUsers))
				.then(ClientCommandManager.literal("syncverbosity")
						.then(argument("level", new SyncVerbosity.SyncVerbosityArgumentType())
								.executes(WildfireCommand::setLogLevel)));

		if(MinecraftClient.getInstance().isInSingleplayer()) {
			debug
					.then(ClientCommandManager.literal("trim")
							.then(ClientCommandManager.argument("glint", BoolArgumentType.bool())
									.executes(WildfireCommand::equipTrimmedChestplate))
							.executes(WildfireCommand::equipTrimmedChestplate))
					.then(ClientCommandManager.literal("armorstand").executes(WildfireCommand::spawnArmorStand));
		}

		var root = dispatcher.register(ClientCommandManager.literal("femalegender")
				.executes(WildfireCommand::openConfig)
				.then(debug));

		dispatcher.register(ClientCommandManager.literal("fgm")
				.executes(WildfireCommand::openConfig)
				.redirect(root));
	}

	@SuppressWarnings("SameParameterValue")
	private static <T> T getOrDefault(CommandContext<FabricClientCommandSource> ctx, String name, T defaultValue, Class<T> clazz) {
		T value = defaultValue;
		try {
			value = ctx.getArgument(name, clazz);
		} catch(IllegalArgumentException ignored) {}
		return value;
	}

	public static void send(CommandContext<FabricClientCommandSource> ctx, String text) {
		ctx.getSource().sendFeedback(Text.empty().append(COMMAND_PREFIX).append(text));
	}

	public static void send(CommandContext<FabricClientCommandSource> ctx, Text text) {
		ctx.getSource().sendFeedback(Text.empty().append(COMMAND_PREFIX).append(text));
	}

	public static void sendHelp(CommandContext<FabricClientCommandSource> ctx, Text header, String... nameToDescription) {
		assert nameToDescription.length % 2 == 0;
		List<Text> lines = new ArrayList<>();
		lines.add(Text.empty().append(COMMAND_PREFIX).append(header).formatted(Formatting.UNDERLINE));

		for(int i = 0; i < nameToDescription.length / 2; i++) {
			var name = nameToDescription[i * 2];
			var description = nameToDescription[(i * 2) + 1];
			lines.add(Text.empty().append(COMMAND_PREFIX)
				.append(Text.literal(name).formatted(Formatting.AQUA))
				.append(Text.literal(" - ").formatted(Formatting.GRAY))
				.append(Text.literal(description)));
		}

		ctx.getSource().sendFeedback(Texts.join(lines, Text.literal("\n")));
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		final var client = ctx.getSource().getClient();
		final var player = ctx.getSource().getPlayer();
		// the .send() is necessary as otherwise the chat screen will simply immediately close the opened screen
		client.send(() -> WardrobeBrowserScreen.open(client, player));
		return 1;
	}

	private static int getEntityLookingAt(CommandContext<FabricClientCommandSource> ctx) {
		var target = ctx.getSource().getClient().targetedEntity;

		if(target != null) {
			send(ctx, "Looking at: " + target.getName().getString());
			send(ctx, "UUID: " + target.getUuidAsString());
			send(ctx, "Type: " + target.getType());
			send(ctx, "Class: " + target.getClass());
			send(ctx, "Renderer: " + MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(target));
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

		var players = dump(WildfireGender.CACHE, ctx.getSource().getWorld(), !allPlayers);
		if(!players.isEmpty()) {
			send(ctx, "Synced Players (" + players.size() + "):");
			for(var line : players) {
				send(ctx, line);
			}
		}

		if(showEntities) {
			var entities = dump(EntityConfig.CACHE, ctx.getSource().getWorld(), false);
			if(!entities.isEmpty()) {
				send(ctx, "Entities (" + players.size() + "):");
				for(var line : entities) {
					send(ctx, line);
				}
			}
		}

		return 1;
	}

	private static List<Text> dump(Cache<UUID, ? extends EntityConfig> cache, @NotNull World world, boolean ignoreEmptyConfig) {
		List<Text> lines = new ArrayList<>();
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

			var info = Texts.join(config.getDebugInfo(), Text.literal("\n"), Text::literal);

			lines.add(Text.empty()
					.append(entity.getDisplayName())
					.append(" - ")
					.append(config.getGender().getDisplayName())
					.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(info))));
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
	 * Takes a client-sided {@link CommandContext} and returns the {@link ServerPlayerEntity} for the invoking player
	 * when in singleplayer, or throws an error.
	 */
	private static ServerPlayerEntity getIntegratedServerPlayer(CommandContext<FabricClientCommandSource> ctx) {
		var integratedServer = Objects.requireNonNull(MinecraftClient.getInstance().getServer());
		var playerManager = Objects.requireNonNull(integratedServer.getPlayerManager());
		return Objects.requireNonNull(playerManager.getPlayer(ctx.getSource().getPlayer().getUuid()));
	}

	private static int equipTrimmedChestplate(CommandContext<FabricClientCommandSource> ctx) {
		Boolean glint = getOrDefault(ctx, "glint", null, Boolean.class);
		var player = getIntegratedServerPlayer(ctx);
		if(!player.hasPermissionLevel(2)) return 0;
		var item = new ItemStack(Items.IRON_CHESTPLATE);
		var material = player.getRegistryManager().getOrThrow(RegistryKeys.TRIM_MATERIAL).getOrThrow(ArmorTrimMaterials.AMETHYST);
		var pattern = player.getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN).getOrThrow(ArmorTrimPatterns.COAST);
		item.set(DataComponentTypes.TRIM, new ArmorTrim(material, pattern));
		if(glint != null) {
			item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);
		}
		player.equipStack(EquipmentSlot.CHEST, item);
		return 1;
	}

	private static int spawnArmorStand(CommandContext<FabricClientCommandSource> ctx) {
		var player = getIntegratedServerPlayer(ctx);
		if(!player.hasPermissionLevel(2)) return 0;
		var world = player.getEntityWorld();

		var item = new ItemStack(Items.IRON_CHESTPLATE);
		var config = WildfireGender.getOrAddPlayerById(player.getUuid());
		var component = BreastDataComponent.fromPlayer(player, config);
		if(component == null) {
			ctx.getSource().sendError(Text.literal("Returned breast data component was null; do you have Hide in Armor on?"));
			return 0;
		}
		component.write(item);

		var stand = new ArmorStandEntity(world, player.getBlockX(), player.getBlockY(), player.getBlockZ());
		stand.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		stand.equipStack(EquipmentSlot.CHEST, item);
		stand.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
		stand.equipStack(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
		world.spawnEntity(stand);

		return 1;
	}
}

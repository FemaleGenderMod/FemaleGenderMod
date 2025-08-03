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
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.wildfire.gui.screen.WardrobeBrowserScreen;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
		var debug = ClientCommandManager.literal("debug")
				.then(ClientCommandManager.literal("invalidatecache")
						.executes(WildfireCommand::invalidateCache))
				.then(ClientCommandManager.literal("target")
						.executes(WildfireCommand::getEntityLookingAt))
				.then(ClientCommandManager.literal("cache")
						.then(argument("allPlayers", BoolArgumentType.bool())
								.executes(WildfireCommand::getUsers)
								.then(argument("showEntities", BoolArgumentType.bool())
										.executes(WildfireCommand::getUsers)))
						.executes(WildfireCommand::getUsers))
				.then(ClientCommandManager.literal("debughud")
						.executes(WildfireCommand::debugCommand))
				.then(ClientCommandManager.literal("syncverbosity")
						.then(argument("level", new SyncVerbosity.SyncVerbosityArgumentType())
								.executes(WildfireCommand::setLogLevel)));

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

	@Environment(EnvType.CLIENT)
	public static void send(CommandContext<FabricClientCommandSource> ctx, String text) {
		ctx.getSource().sendFeedback(Text.empty().append(COMMAND_PREFIX).append(text));
	}

	@Environment(EnvType.CLIENT)
	public static void send(CommandContext<FabricClientCommandSource> ctx, Text text) {
		ctx.getSource().sendFeedback(Text.empty().append(COMMAND_PREFIX).append(text));
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

	private static int debugCommand(CommandContext<FabricClientCommandSource> ctx) {
		ClientConfig.INSTANCE.set(ClientConfig.DEBUG_MODE, !ClientConfig.INSTANCE.get(ClientConfig.DEBUG_MODE));
		send(ctx, "Debug mode: " + (ClientConfig.INSTANCE.get(ClientConfig.DEBUG_MODE) ? "Enabled" : "Disabled"));
		ClientConfig.INSTANCE.save();
		return 1;
	}
}

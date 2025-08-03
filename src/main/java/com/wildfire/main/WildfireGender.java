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

import java.time.Duration;
import java.util.*;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class WildfireGender implements ModInitializer {
	public static final String MODID = "wildfire_gender";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final LoadingCache<UUID, PlayerConfig> CACHE;

	//TODO: Move to client cfg
	public static boolean DEBUG_MODE = true;

	static {
		var builder = CacheBuilder.newBuilder();
		// Only automatically expire cache entries on the client; a server may go a decent while without accessing
		// the player cache, and we can't easily re-cache a player's settings on a server, while a client
		// will typically either receive settings from the server in a sync, or simply re-fetch from
		// a local config file or from the cloud.
		// Note that servers will manually invalidate cache entries upon a player disconnecting
		// (see WildfireEventHandler#playerDisconnected).
		if(WildfireHelper.onClient()) {
			builder.expireAfterAccess(Duration.ofMinutes(15));
		}
		CACHE = builder.build(new CacheLoader<>() {
			@Override
			public @NotNull PlayerConfig load(@NotNull UUID key) {
				var config = new PlayerConfig(key);
				// only attempt to load player data on the client, and if the provided uuid is valid
				if(WildfireHelper.onClient() && key.version() == 4) {
					// markForSync being true will only ever do anything for the client player
					WildfireGenderClient.loadGenderInfo(config, true, false);
				}
				return config;
			}
		});
	}

	public static final UUID CREATOR_UUID = UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946");
	public static final List<UUID> CONTRIBUTOR_UUIDS = List.of(
			UUID.fromString("70336328-0de7-430e-8cba-2779e2a05ab5"), //celeste
			UUID.fromString("64e57307-72e5-4f43-be9c-181e8e35cc9b"), //pupnewfster
			UUID.fromString("618a8390-51b1-43b2-a53a-ab72c1bbd8bd"), //Kichura
			UUID.fromString("33feda66-c706-4725-8983-f62e5e6cbee7"), //BlueLight
			UUID.fromString("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc"), //Diademiemi
			UUID.fromString("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1"), //Arcti.cc
			UUID.fromString("3f36f7e9-7459-43fe-87ce-4e8a5d47da80"), //IzzyBizzy45
			UUID.fromString("525b0455-15e9-49b7-b61d-f291e8ee6c5b"), //Powerless001
			UUID.fromString("6e0e0db3-19e9-4fa7-af76-a6d3651c57b9") //A2 76
			//UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946") //WildfireFGM (I'm not a contributor, silly!)
	);

	@Override
	public void onInitialize() {
		WildfireSync.register();
		WildfireEventHandler.registerCommonEvents();
		ClientConfig.INSTANCE.load();


		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("fgm")
					.then(ClientCommandManager.literal("invalidatecache")
							.executes(WildfireGender::invalidateCache)
					)
					.then(ClientCommandManager.literal("lookentity")
							.executes(WildfireGender::getEntityLookingAt)
					)
					.then(ClientCommandManager.literal("cache")
							.executes(WildfireGender::getUsers)
					)
					.then(ClientCommandManager.literal("debug")
							.executes(WildfireGender::debugCommand)
					)
					.then(ClientCommandManager.literal("verbosity")
						.then(argument("level", StringArgumentType.string())
								.suggests((ctx, builder) -> {
									for (SyncVerbosity level : SyncVerbosity.values()) {
										builder.suggest(level.name());
									}
									return builder.buildFuture();
								})
								.executes(WildfireGender::setLogLevel)
						)
					)
			);
		});
	}

	private static int getEntityLookingAt(CommandContext<FabricClientCommandSource> ctx) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) return 0;

		double reachDistance = 10.0D; // how far to check
		Vec3d eyePos = client.player.getCameraPosVec(1.0F);
		Vec3d lookVec = client.player.getRotationVec(1.0F);
		Vec3d reachVec = eyePos.add(lookVec.multiply(reachDistance));

		EntityHitResult entityHitResult = ProjectileUtil.raycast(
				client.player,
				eyePos,
				reachVec,
				client.player.getBoundingBox().stretch(lookVec.multiply(reachDistance)).expand(1.0D),
				entity -> !entity.isSpectator() && entity.isAlive(),
				reachDistance * reachDistance
		);

		if (entityHitResult != null && entityHitResult.getEntity() != null) {
			Entity target = entityHitResult.getEntity();
			WildfireHelper.logCommand(ctx, "Looking at: " + target.getName().getString());
			WildfireHelper.logCommand(ctx, "UUID: " + target.getUuidAsString());
			WildfireHelper.logCommand(ctx, "Type: " + target.getType());
			WildfireHelper.logCommand(ctx, "Class: " + target.getClass());
			WildfireHelper.logCommand(ctx, "Renderer: " + MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(target));

		} else {
			WildfireHelper.logCommand(ctx, "No entity in sight.");
		}
		return 1;
	}

	//COMMANDS BELOW
	public static int setLogLevel(CommandContext<FabricClientCommandSource> ctx) {
		String level = StringArgumentType.getString(ctx, "level");

		ClientConfig.INSTANCE.set(ClientConfig.SYNC_VERBOSITY, SyncVerbosity.valueOf(level));
		ClientConfig.INSTANCE.save();

		WildfireHelper.logCommand(ctx, "Log level set to: " + level);
		return 1;
	}

	private static int getUsers(CommandContext<FabricClientCommandSource> ctx) {
		if (MinecraftClient.getInstance().world != null) {
			ctx.getSource().sendFeedback(Text.literal("Players Cached (" + WildfireGender.CACHE.size() + "):"));

			for (Map.Entry<UUID, PlayerConfig> entry : WildfireGender.CACHE.asMap().entrySet()) {
				PlayerConfig plrConfig = entry.getValue();
				if (plrConfig != null) {
					PlayerEntity plr = MinecraftClient.getInstance().world.getPlayerByUuid(plrConfig.uuid);
					if (plr != null) {
						ctx.getSource().sendFeedback(
								Text.empty().append(plr.getDisplayName()).append(" - ").append(plrConfig.getGender().getDisplayName())
						);
					}
				}
			}

			ctx.getSource().sendFeedback(Text.literal("Entities Cached (" + EntityConfig.CACHE.size() + "):"));

			for (Map.Entry<UUID, EntityConfig> entry : EntityConfig.CACHE.asMap().entrySet()) {
				EntityConfig entityConfig = entry.getValue();
				if (entityConfig != null) {
					Entity entity = MinecraftClient.getInstance().world.getEntity(entityConfig.uuid);
					if (entity != null) {
						ctx.getSource().sendFeedback(
								Text.empty().append(entity.getDisplayName()).append(" - ").append(entityConfig.getGender().getDisplayName())
						);
					}
				}
			}
		}
		return 1;
	}


	private static int invalidateCache(CommandContext<FabricClientCommandSource> ctx) {
		CACHE.invalidateAll();
		EntityConfig.CACHE.invalidateAll();

		WildfireHelper.logCommand(ctx, "Cache has been invalidated!");
		return 1;
	}

	private static int debugCommand(CommandContext<FabricClientCommandSource> ctx) {
		ClientConfig.INSTANCE.set(ClientConfig.DEBUG_MODE, !ClientConfig.INSTANCE.get(ClientConfig.DEBUG_MODE));
		WildfireHelper.logCommand(ctx, "Debug Mode: " + (ClientConfig.INSTANCE.get(ClientConfig.DEBUG_MODE)?"Enabled":"Disabled"));
		ClientConfig.INSTANCE.save();
		return 1;
	}

	//END COMMANDS BELOW

	public static @Nullable PlayerConfig getPlayerById(UUID id) {
		return CACHE.getIfPresent(id);
	}

	public static @NotNull PlayerConfig getOrAddPlayerById(UUID id) {
		return CACHE.getUnchecked(id);
	}
}

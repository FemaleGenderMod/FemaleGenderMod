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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

@Environment(EnvType.CLIENT)
public class WildfireCommand {
	static void init() {
		ClientCommandRegistrationCallback.EVENT.register(WildfireCommand::register);
	}

	private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registry) {
		dispatcher.register(
				ClientCommandManager.literal("fgm")
						.then(ClientCommandManager.literal("invalidatecache")
								.executes(WildfireCommand::invalidateCache))
						.then(ClientCommandManager.literal("lookentity")
								.executes(WildfireCommand::getEntityLookingAt))
						.then(ClientCommandManager.literal("cache")
								.executes(WildfireCommand::getUsers))
						.then(ClientCommandManager.literal("debug")
								.executes(WildfireCommand::debugCommand))
						.then(ClientCommandManager.literal("verbosity")
								.then(argument("level", new SyncVerbosity.SyncVerbosityArgumentType())
										.executes(WildfireCommand::setLogLevel)))
		);
	}

	private static int getEntityLookingAt(CommandContext<FabricClientCommandSource> ctx) {
		var player = ctx.getSource().getPlayer();

		double reachDistance = 10.0D; // how far to check
		Vec3d eyePos = player.getCameraPosVec(1.0F);
		Vec3d lookVec = player.getRotationVec(1.0F);
		Vec3d reachVec = eyePos.add(lookVec.multiply(reachDistance));

		EntityHitResult entityHitResult = ProjectileUtil.raycast(
				player,
				eyePos,
				reachVec,
				player.getBoundingBox().stretch(lookVec.multiply(reachDistance)).expand(1.0D),
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

	public static int setLogLevel(CommandContext<FabricClientCommandSource> ctx) {
		SyncVerbosity level = ctx.getArgument("level", SyncVerbosity.class);

		ClientConfig.INSTANCE.set(ClientConfig.SYNC_VERBOSITY, level);
		ClientConfig.INSTANCE.save();

		WildfireHelper.logCommand(ctx, "Log level set to: " + level);
		return 1;
	}

	private static int getUsers(CommandContext<FabricClientCommandSource> ctx) {
		var world = ctx.getSource().getWorld();
		ctx.getSource().sendFeedback(Text.literal("Players Cached (" + WildfireGender.CACHE.size() + "):"));

		for (Map.Entry<UUID, PlayerConfig> entry : WildfireGender.CACHE.asMap().entrySet()) {
			PlayerConfig plrConfig = entry.getValue();
			if (plrConfig != null) {
				PlayerEntity plr = world.getPlayerByUuid(plrConfig.uuid);
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
				Entity entity = world.getEntity(entityConfig.uuid);
				if (entity != null) {
					ctx.getSource().sendFeedback(
							Text.empty().append(entity.getDisplayName()).append(" - ").append(entityConfig.getGender().getDisplayName())
					);
				}
			}
		}

		return 1;
	}

	private static int invalidateCache(CommandContext<FabricClientCommandSource> ctx) {
		WildfireGender.CACHE.invalidateAll();
		EntityConfig.CACHE.invalidateAll();

		WildfireHelper.logCommand(ctx, "Cache has been invalidated!");
		return 1;
	}

	private static int debugCommand(CommandContext<FabricClientCommandSource> ctx) {
		ClientConfig.INSTANCE.set(ClientConfig.DEBUG_MODE, !ClientConfig.INSTANCE.get(ClientConfig.DEBUG_MODE));
		WildfireHelper.logCommand(ctx, "Debug mode: " + (ClientConfig.INSTANCE.get(ClientConfig.DEBUG_MODE) ? "Enabled" : "Disabled"));
		ClientConfig.INSTANCE.save();
		return 1;
	}
}

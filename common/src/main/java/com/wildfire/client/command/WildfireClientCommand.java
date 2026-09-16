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

package com.wildfire.client.command;

import com.google.common.cache.Cache;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.wildfire.api.EntityCache;
import com.wildfire.api.client.WildfireClientAPI;
import com.wildfire.api.impl.EntityCacheImpl;
import com.wildfire.api.server.WildfireServerAPI;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.client.gui.screen.WildfireFirstTimeSetupScreen;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.command.AbstractWildfireCommand;
import com.wildfire.common.config.enums.SyncVerbosity;
import com.wildfire.common.entities.EntityConfig;
import com.wildfire.common.entities.EntityConfigHolder;
import com.wildfire.common.entities.players.PlayerConfigHolder;
import com.wildfire.common.entities.players.SyncStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/// @apiNote Only use this on the client side
public final class WildfireClientCommand<S extends SharedSuggestionProvider> extends AbstractWildfireCommand<ClientCommandHelper<S>, S> {
    public WildfireClientCommand(ClientCommandHelper<S> helper) {
        super(helper);
    }

    public void register(final CommandDispatcher<S> dispatcher) {
        var debug = helper.literal("debug")
            .executes(ctx -> {
                sendHelp(ctx, WildfireLang.DEBUG_COMMAND,
                    WildfireLang.COMMAND_INVALIDATE_CACHE,
                    WildfireLang.COMMAND_TARGET,
                    WildfireLang.COMMAND_CACHE,
                    WildfireLang.COMMAND_FIRST_TIME,
                    WildfireLang.COMMAND_SYNC_VERBOSITY
                );
                return Command.SINGLE_SUCCESS;
            })
            .then(helper.literal("invalidatecache")
                .executes(this::invalidateCache))
            .then(helper.literal("target")
                .executes(this::getEntityLookingAt))
            .then(helper.literal("firsttime")
                .executes(this::openFirstTime))
            .then(helper.literal("cache")
                .then(helper.argument("allPlayers", BoolArgumentType.bool())
                    .executes(this::getUsers)
                    .then(helper.argument("showArmorStands", BoolArgumentType.bool())
                        .executes(this::getUsers)))
                .executes(this::getUsers))
            .then(helper.literal("syncverbosity")
                .then(helper.argument("level", new SyncVerbosity.SyncVerbosityArgumentType())
                    .executes(this::setLogLevel)));

        var root = dispatcher.register(helper.literal("femalegender")
            .executes(this::openConfig)
            .then(debug));

        dispatcher.register(helper.literal("fgm")
            .executes(this::openConfig)
            .redirect(root));
    }

    private int openConfig(CommandContext<S> ctx) {
        final var client = helper.getMinecraft(ctx.getSource());
        // the .schedule() is necessary as otherwise the chat screen will simply immediately close the opened screen
        helper.getMinecraft(ctx.getSource()).schedule(() -> {
            LocalPlayer player = helper.getPlayer(ctx.getSource());
            WardrobeBrowserScreen.open(client, player);
        });
        return Command.SINGLE_SUCCESS;
    }

    private int openFirstTime(CommandContext<S> ctx) {
        final var client = helper.getMinecraft(ctx.getSource());
        // the .schedule() is necessary as otherwise the chat screen will simply immediately close the opened screen
        helper.getMinecraft(ctx.getSource()).schedule(() -> {
            LocalPlayer player = helper.getPlayer(ctx.getSource());
            var screen = new WildfireFirstTimeSetupScreen(null, player.getUUID());
            client.gui.setScreen(screen);
        });
        return Command.SINGLE_SUCCESS;
    }

    private int getEntityLookingAt(CommandContext<S> ctx) {
        Minecraft minecraft = helper.getMinecraft(ctx.getSource());
        Entity target = minecraft.crosshairPickEntity;

        if (target != null) {
            send(ctx, WildfireLang.DEBUG_COMMAND_LOOKING_AT.translate(target.getName()));
            send(ctx, WildfireLang.DEBUG_COMMAND_LOOKING_AT_UUID.translate(target.getStringUUID()));
            send(ctx, WildfireLang.DEBUG_COMMAND_LOOKING_AT_TYPE.translate(target.getType()));
            send(ctx, WildfireLang.DEBUG_COMMAND_LOOKING_AT_CLASS.translate(target.getClass()));
            send(ctx, WildfireLang.DEBUG_COMMAND_LOOKING_AT_RENDERER.translate(minecraft.getEntityRenderDispatcher().getRenderer(target)));
        } else {
            send(ctx, WildfireLang.DEBUG_COMMAND_LOOKING_AT_NONE.translate());
        }
        return Command.SINGLE_SUCCESS;
    }

    public int setLogLevel(CommandContext<S> ctx) {
        SyncVerbosity level = ctx.getArgument("level", SyncVerbosity.class);

        if (ClientConfig.config().cloudSync().logVerbosity().update(level)) {//Should always be true
            ClientConfig.INSTANCE.save();

            send(ctx, WildfireLang.DEBUG_COMMAND_LOG_LEVEL.translate(level));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private int getUsers(CommandContext<S> ctx) {
        boolean allPlayers = getOrDefault(ctx, "allPlayers", false, Boolean.class);
        boolean showArmorStands = getOrDefault(ctx, "showArmorStands", false, Boolean.class);

        Level level = helper.getLevel(ctx.getSource());
        List<Component> players = dump(WildfireClientAPI.players(), level, !allPlayers);
        if (!players.isEmpty()) {
            send(ctx, WildfireLang.DEBUG_COMMAND_SYNCED_PLAYERS.translate(players.size()));
            for (Component line : players) {
                send(ctx, line);
            }
        }

        if (showArmorStands) {
            List<Component> entities = dump(WildfireClientAPI.armorStands(), level, false);
            if (!entities.isEmpty()) {
                send(ctx, WildfireLang.DEBUG_COMMAND_ENTITIES.translate(entities.size()));
                for (Component line : entities) {
                    send(ctx, line);
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private int getSingleplayerUsers(CommandContext<S> ctx) {
        Level level = helper.getLevel(ctx.getSource());
        List<Component> players = dump(WildfireServerAPI.players(), level, true);
        if (!players.isEmpty()) {
            send(ctx, WildfireLang.DEBUG_COMMAND_SYNCED_PLAYERS.translate(players.size()));
            for (Component line : players) {
                send(ctx, line);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static <HOLDER extends EntityConfigHolder<? extends EntityConfig>> List<Component> dump(
        EntityCache<HOLDER, ?> cache, Level world, boolean ignoreEmptyConfig
    ) {
        Cache<UUID, HOLDER> underlying = ((EntityCacheImpl<HOLDER, ?>) cache).cache();
        List<Component> lines = new ArrayList<>();
        for (var entry : underlying.asMap().entrySet()) {
            UUID uuid = entry.getKey();
            HOLDER config = entry.getValue();
            if (config instanceof PlayerConfigHolder playerConfig && playerConfig.getSyncStatus() == SyncStatus.UNKNOWN && ignoreEmptyConfig) {
                continue;
            }
            Entity entity = world.getEntity(uuid);
            if (entity == null) {
                continue;
            }

            var info = ComponentUtils.formatList(config.getDebugInfo(), CommonComponents.NEW_LINE, Component::literal);

            lines.add(WildfireLang.GENERIC_DASH_EXPLANATION.translate(entity.getDisplayName(), config.gender().get().getDisplayName())
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(info))));
        }
        return lines;
    }

    private int invalidateCache(CommandContext<S> ctx) {
        WildfireClientAPI.players().invalidateAll();
        WildfireClientAPI.mannequins().invalidateAll();
        WildfireClientAPI.armorStands().invalidateAll();

        send(ctx, WildfireLang.COMMAND_INVALIDATE_CACHE_SUCCESS.translate());
        return Command.SINGLE_SUCCESS;
    }
}

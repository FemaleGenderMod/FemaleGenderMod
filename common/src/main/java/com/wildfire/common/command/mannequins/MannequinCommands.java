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

package com.wildfire.common.command.mannequins;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.JavaOps;
import com.wildfire.api.server.WildfireServerAPI;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.command.AbstractWildfireCommand;
import com.wildfire.common.command.ServerCommandHelper;
import com.wildfire.common.entities.avatars.AbstractAvatarConfigHolder;
import com.wildfire.common.entities.avatars.AvatarConfig;
import com.wildfire.common.entities.avatars.MannequinConfigHolder;
import com.wildfire.common.networking.WildfireNetworking;
import com.wildfire.common.networking.packets.mannequins.ClientboundEditMannequinPacket;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Mannequin;

public final class MannequinCommands<S extends SharedSuggestionProvider> extends AbstractWildfireCommand<ServerCommandHelper<S>, S> {
    private static final SimpleCommandExceptionType NOT_A_MANNEQUIN = new SimpleCommandExceptionType(WildfireLang.COMMAND_ENTITY_MUST_BE_MANNEQUIN.translate());

    public MannequinCommands(final ServerCommandHelper<S> helper) {
        super(helper);
    }

    public LiteralArgumentBuilder<S> createNode() {
        return helper.literal("mannequin")
            .then(helper.argument("entity", EntityArgument.entity())
                .executes(this::editMannequin)
                .then(helper.literal("gui")
                    .executes(this::editMannequin))
                .then(helper.literal("copy")
                    .then(helper.argument("from", EntityArgument.entity())
                        .executes(this::copyOntoMannequin)))
                .then(helper.literal("set")
                    .then(setter("gender", MannequinComponent.GENDER))
                    .then(setter("size", MannequinComponent.BREAST_SIZE))
                    .then(setter("cleavage", MannequinComponent.BREAST_CLEAVAGE))
                    .then(setter("show_in_armor", MannequinComponent.SHOW_IN_ARMOR))
                    .then(helper.literal("physics")
                        .then(setter("enabled", MannequinComponent.PHYSICS))
                        .then(setter("intensity", MannequinComponent.PHYSICS_BOUNCE))
                        .then(setter("momentum", MannequinComponent.PHYSICS_FLOPPY))
                        .then(setter("uniboob", MannequinComponent.PHYSICS_UNIBOOB)))
                    .then(helper.literal("offset")
                        .then(setter("x", MannequinComponent.BREAST_OFFSET_X))
                        .then(setter("y", MannequinComponent.BREAST_OFFSET_Y))
                        .then(setter("z", MannequinComponent.BREAST_OFFSET_Z)))
                ));
    }

    private Mannequin getMannequin(CommandContext<S> ctx) throws CommandSyntaxException {
        Entity entity = helper.resolveSingleEntityArgument(ctx, "entity");
        if(!(entity instanceof Mannequin mannequin)) {
            throw NOT_A_MANNEQUIN.create();
        }
        return mannequin;
    }

    private int editMannequin(CommandContext<S> ctx) throws CommandSyntaxException {
        ServerPlayer player = helper.getPlayer(ctx.getSource());
        Mannequin mannequin = getMannequin(ctx);

        if(WildfireNetworking.INSTANCE.canSendToPlayer(player, ClientboundEditMannequinPacket.TYPE)) {
            WildfireNetworking.INSTANCE.sendToClient(player, new ClientboundEditMannequinPacket(mannequin.getUUID()));
        } else {
            helper.sendFailure(ctx.getSource(), WildfireLang.COMMAND_SERVER_NO_MOD_ON_CLIENT.translateColored(TextColor.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int copyOntoMannequin(CommandContext<S> ctx) throws CommandSyntaxException {
        Mannequin mannequin = getMannequin(ctx);

        Entity from = helper.resolveSingleEntityArgument(ctx, "from");
        if(!(from instanceof Avatar fromLiving)) {
            helper.sendFailure(ctx.getSource(), WildfireLang.COMMAND_ENTITY_MUST_BE_AVATAR_LIKE.translateColored(TextColor.RED));
            return 0;
        }
        var fromConfig = WildfireServerAPI.getConfig(fromLiving);
        if(!(fromConfig instanceof AbstractAvatarConfigHolder fromAvatarConfig)) {
            helper.sendFailure(ctx.getSource(), WildfireLang.COMMAND_ENTITY_MUST_BE_AVATAR_LIKE.translateColored(TextColor.RED));
            return 0;
        }

        MannequinConfigHolder config = WildfireServerAPI.mannequins().getOrCreate(mannequin);
        AvatarConfig copy = AvatarConfig.CODEC.decode(
            JavaOps.INSTANCE,
            AvatarConfig.CODEC.encodeStart(
                JavaOps.INSTANCE,
                fromAvatarConfig.config()
            ).result().orElseGet(AvatarConfig::createDefault)
        ).getOrThrow().getFirst();

        config.setConfigAndSync(mannequin, copy);
        helper.sendSystemMessage(ctx.getSource(), WildfireLang.COMMAND_MANNEQUIN_COPIED_DATA.translate(
            mannequin.getDisplayName(), fromLiving.getDisplayName()));

        return Command.SINGLE_SUCCESS;
    }

    private <A, T> LiteralArgumentBuilder<S> setter(final String name, final MannequinComponent<A, T> component) {
        return helper.literal(name)
            .then(Util.make(helper.argument("value", component.argument()), builder -> {
                var suggestionProvider = component.<S>suggestionProvider();
                if(suggestionProvider != null) {
                    builder.suggests(suggestionProvider);
                }

                builder.executes(ctx -> {
                    Mannequin mannequin = getMannequin(ctx);
                    MannequinConfigHolder config = WildfireServerAPI.mannequins().getOrCreate(mannequin);
                    T value = component.parse(ctx, "value", helper);
                    component.update(config, value);
                    config.sync(mannequin);
                    return Command.SINGLE_SUCCESS;
                });
            }));
    }
}

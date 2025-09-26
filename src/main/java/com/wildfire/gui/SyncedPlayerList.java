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

package com.wildfire.gui;

import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SyncedPlayerList {
	private SyncedPlayerList() {
		throw new UnsupportedOperationException();
	}

	private static int ticks = 0;
	private static volatile List<SyncedPlayer> syncedPlayers = Collections.emptyList();

	static {
		ClientTickEvents.END_CLIENT_TICK.register(SyncedPlayerList::onTick);
	}

	public static void drawSyncedPlayers(DrawContext context, TextRenderer textRenderer) {
		if(syncedPlayers.isEmpty()) {
			return;
		}

		var header = Text.translatable("wildfire_gender.wardrobe.players_using_mod").formatted(Formatting.AQUA);
		context.drawText(textRenderer, header, 5, 5, 0xFFFFFFFF, true);

		int yPos = 18;
		for(var entry : syncedPlayers) {
			var text = Text.empty()
					.append(Text.literal(entry.name()).withColor(entry.color()))
					.append(" - ")
					.append(entry.gender().getDisplayName());
			context.drawText(textRenderer, text, 10, yPos, 0xFFFFFFFF, false);
			yPos += 10;
		}
	}

	private static void onTick(MinecraftClient client) {
		if(ticks++ % 5 != 0) {
			return;
		}

		var clientPlayer = MinecraftClient.getInstance().player;
		if(clientPlayer == null) {
			syncedPlayers = Collections.emptyList();
			return;
		}

		var list = new ArrayList<SyncedPlayer>();

		for(var entry : clientPlayer.networkHandler.getListedPlayerListEntries()) {
			if(Objects.equals(entry.getProfile().id(), clientPlayer.getUuid())) {
				continue;
			}

			var config = WildfireGender.getPlayerById(entry.getProfile().id());
			if(config == null || config.syncStatus == PlayerConfig.SyncStatus.UNKNOWN) {
				continue;
			}

			var color = Contributors.getColor(entry.getProfile().id());
			list.add(new SyncedPlayer(entry.getProfile().name(), color == null ? 0xFFFFFF : color, config.getGender()));

			if(list.size() >= 40) {
				break;
			}
		}

		syncedPlayers = list;
	}

	private record SyncedPlayer(String name, int color, Gender gender) {
	}
}

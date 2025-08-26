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

package com.wildfire.render.debug;

import com.wildfire.main.WildfireGender;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PhysicsDebugHudEntry implements DebugHudEntry {
	public static final Identifier ID = WildfireGender.id("physics");

	@Override
	public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
		var player = MinecraftClient.getInstance().player;
		if(player == null) return;
		var config = WildfireGender.getPlayerById(player.getUuid());
		if(config == null) return;

		List<String> info = new ArrayList<>();
		if(config.getBreasts().isUniboob()) {
			info.add(Formatting.UNDERLINE + "Breast Physics");
			add(info, config.getLeftBreastPhysics());
		} else {
			info.add(Formatting.UNDERLINE + "Left Breast Physics");
			add(info, config.getLeftBreastPhysics());
			info.add("");
			info.add(Formatting.UNDERLINE + "Right Breast Physics");
			add(info, config.getRightBreastPhysics());
		}

		lines.addLinesToSection(ID, info);
	}

	private void add(List<String> lines, BreastPhysics physics) {
		lines.add("Breast size: " + physics.getBreastSize());
		lines.add("Position: (" + physics.getPositionX() + ", " + physics.getPositionY() + ")");
		lines.add("Rotation: " + physics.getBounceRotation());
	}
}

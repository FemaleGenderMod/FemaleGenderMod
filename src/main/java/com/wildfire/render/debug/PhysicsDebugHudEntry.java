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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PhysicsDebugHudEntry implements DebugScreenEntry {
	public static final Identifier ID = WildfireGender.id("physics");

	@Override
	public void display(DebugScreenDisplayer lines, @Nullable Level world, @Nullable LevelChunk clientChunk, @Nullable LevelChunk chunk) {
		var player = Minecraft.getInstance().player;
		if(player == null) return;
		var config = WildfireGender.getPlayerById(player.getUUID());
		if(config == null) return;

		List<String> info = new ArrayList<>();
		if(config.getBreasts().isUniboob()) {
			info.add(ChatFormatting.UNDERLINE + "Breast Physics");
			add(info, config.getLeftBreastPhysics());
		} else {
			info.add(ChatFormatting.UNDERLINE + "Left Breast Physics");
			add(info, config.getLeftBreastPhysics());
			info.add("");
			info.add(ChatFormatting.UNDERLINE + "Right Breast Physics");
			add(info, config.getRightBreastPhysics());
		}

		lines.addToGroup(ID, info);
	}

	private void add(List<String> lines, BreastPhysics physics) {
		lines.add("Breast size: " + physics.getBreastSize());
		lines.add("Position: (" + physics.getPositionX() + ", " + physics.getPositionY() + ")");
		lines.add("Rotation: " + physics.getBounceRotation());
	}
}

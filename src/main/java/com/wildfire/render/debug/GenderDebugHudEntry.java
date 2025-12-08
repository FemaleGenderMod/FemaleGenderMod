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

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.resources.GenderArmorResourceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GenderDebugHudEntry implements DebugScreenEntry {
	private final Identifier id;
	private final boolean clientPlayer;

	public static final Identifier SELF = WildfireGender.id("self_gender_info");
	public static final Identifier OTHER = WildfireGender.id("target_gender_info");

	private static final String PREFIX =
			ChatFormatting.GRAY + "" + ChatFormatting.UNDERLINE + "["
					+ ChatFormatting.LIGHT_PURPLE + ChatFormatting.UNDERLINE + "F"
					+ ChatFormatting.WHITE + ChatFormatting.UNDERLINE + "GM"
					+ ChatFormatting.GRAY + ChatFormatting.UNDERLINE + "]" +
					ChatFormatting.RESET + ChatFormatting.UNDERLINE;

	public GenderDebugHudEntry(boolean clientPlayer) {
		this.clientPlayer = clientPlayer;
		this.id = clientPlayer ? SELF : OTHER;
	}

	@Override
	public void display(DebugScreenDisplayer lines, @Nullable Level world, @Nullable LevelChunk clientChunk, @Nullable LevelChunk chunk) {
		var client = Minecraft.getInstance();
		var target = clientPlayer ? client.player : client.crosshairPickEntity;
		if(!(target instanceof LivingEntity living) || !EntityConfig.isSupportedEntity(living)) {
			return;
		}

		var config = EntityConfig.getEntity(living);
		List<String> info = new ArrayList<>();

		info.add(PREFIX + " Gender Data");
		info.add("UUID: " + target.getUUID());
		info.addAll(config.getDebugInfo());
		addEquippedChestplate(info, config, living);

		lines.addToGroup(id, info);
	}

	private void addEquippedChestplate(List<String> lines, EntityConfig config, LivingEntity entity) {
		var equippedChestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
		var equippable = equippedChestplate.get(DataComponents.EQUIPPABLE);
		// null is perfectly valid to return here
		//noinspection DataFlowIssue
		var asset = Optionull.map(equippable, (it) -> it.assetId().orElse(null));
		if(asset == null) return;

		lines.add("");
		lines.add(PREFIX + " Equipped Chestplate");

		var id = asset.identifier();
		var armorConfig = Optionull.mapOrDefault(GenderArmorResourceManager.get(id), Function.identity(), IGenderArmor.DEFAULT);
		lines.add("Material: " + id);
		if(!armorConfig.coversBreasts()) {
			lines.add("Covers breasts: false");
			return;
		} else if(armorConfig.alwaysHidesBreasts()) {
			lines.add("Covers breasts: true");
			return;
		}
		lines.add("Physics resistance: " + armorConfig.physicsResistance());
		lines.add("Tightness: " + armorConfig.tightness());
		lines.add("Armor stands copy: " + armorConfig.armorStandsCopySettings());
		if(armorConfig.tightness() > 0) {
			float renderedSize = config.getBustSize() * (1 - BreastPhysics.TIGHTNESS_REDUCTION_FACTOR * armorConfig.tightness());
			lines.add("Rendered breast size: " + renderedSize);
		}
	}
}

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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GenderDebugHudEntry implements DebugHudEntry {
	private final Identifier id;
	private final boolean clientPlayer;

	public static final Identifier SELF = WildfireGender.id("self_gender_info");
	public static final Identifier OTHER = WildfireGender.id("target_gender_info");

	private static final String PREFIX =
			Formatting.GRAY + "" + Formatting.UNDERLINE + "["
					+ Formatting.LIGHT_PURPLE + Formatting.UNDERLINE + "F"
					+ Formatting.WHITE + Formatting.UNDERLINE + "GM"
					+ Formatting.GRAY + Formatting.UNDERLINE + "]" +
					Formatting.RESET + Formatting.UNDERLINE;

	public GenderDebugHudEntry(boolean clientPlayer) {
		this.clientPlayer = clientPlayer;
		this.id = clientPlayer ? SELF : OTHER;
	}

	@Override
	public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
		var client = MinecraftClient.getInstance();
		var target = clientPlayer ? client.player : client.targetedEntity;
		if(!(target instanceof LivingEntity living) || !EntityConfig.isSupportedEntity(living)) {
			return;
		}

		var config = EntityConfig.getEntity(living);
		List<String> info = new ArrayList<>();

		info.add(PREFIX + " Gender Data");
		info.add("UUID: " + target.getUuid());
		info.addAll(config.getDebugInfo());
		addEquippedChestplate(info, config, living);

		lines.addLinesToSection(id, info);
	}

	private void addEquippedChestplate(List<String> lines, EntityConfig config, LivingEntity entity) {
		var equippedChestplate = entity.getEquippedStack(EquipmentSlot.CHEST);
		var equippable = equippedChestplate.get(DataComponentTypes.EQUIPPABLE);
		var asset = Nullables.map(equippable, (it) -> it.assetId().orElse(null));
		if(asset == null) return;

		lines.add("");
		lines.add(PREFIX + " Equipped Chestplate");

		var id = asset.getValue();
		var armorConfig = Nullables.mapOrElse(GenderArmorResourceManager.get(id), Function.identity(), IGenderArmor.DEFAULT);
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

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

package com.wildfire.main.entitydata;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.types.ConfigKey;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.physics.BreastPhysics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * <p>A stripped down version of a {@link PlayerConfig player's config}, intended for use with non-player entities.</p>
 *
 * <p>Unlike players, this has very minimal configuration support.</p>
 *
 * <p>Currently only used for {@link ArmorStand armor stands}, and as a superclass for {@link PlayerConfig player configs}.</p>
 */
public class EntityConfig {

	public static final LoadingCache<UUID, EntityConfig> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(5))
			.build(CacheLoader.from(EntityConfig::new));

	public final UUID uuid;
	protected Gender gender = Configuration.GENDER.getDefault();
	protected float pBustSize = Configuration.BUST_SIZE.getDefault();
	protected boolean breastPhysics = Configuration.BREAST_PHYSICS.getDefault();
	protected float bounceMultiplier = Configuration.BOUNCE_MULTIPLIER.getDefault();
	protected float floppyMultiplier = Configuration.FLOPPY_MULTIPLIER.getDefault();

	protected UVLayout leftBreastUVLayout = Configuration.LEFT_BREAST_UV_LAYOUT.getDefault();
	protected UVLayout rightBreastUVLayout = Configuration.RIGHT_BREAST_UV_LAYOUT.getDefault();

	protected UVLayout leftBreastOverlayUVLayout = Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT.getDefault();
	protected UVLayout rightBreastOverlayUVLayout = Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT.getDefault();

	protected UVLayout leftBreastArmorUVLayout = Configuration.LEFT_BREAST_ARMOR_UV_LAYOUT.getDefault();
	protected UVLayout rightBreastArmorUVLayout = Configuration.RIGHT_BREAST_ARMOR_UV_LAYOUT.getDefault();

	protected float voicePitch = Configuration.VOICE_PITCH.getDefault();

	// note: hurt sounds, armor physics override, and show in armor are not defined here, as they have no relevance
	// to entities, and are instead entirely in PlayerConfig

	// TODO ideally these physics objects would be made entirely client-sided, but this class is
	//      used on both the client and server (primarily through PlayerConfig), making it very
	//      difficult to do so without some major changes to split this up further into a common class
	//      with a client extension class (e.g. the PlayerEntity & AbstractClientPlayerEntity classes)
	protected final BreastPhysics lBreastPhysics, rBreastPhysics;
	protected final Breasts breasts;
	protected boolean jacketLayer = true;
	protected @Nullable BreastDataComponent fromComponent;

	@ApiStatus.Internal
	public boolean forceSimplifiedPhysics = false;

	protected EntityConfig(UUID uuid) {
		this.uuid = uuid;
		this.breasts = new Breasts();
		lBreastPhysics = new BreastPhysics(this);
		rBreastPhysics = new BreastPhysics(this);
	}

	/**
	 * Copy gender settings included in the given {@link ItemStack item NBT} to the current entity
	 *
	 * @see BreastDataComponent
	 */
	public void readFromStack(ItemStack chestplate) {
		CustomData component = chestplate.get(DataComponents.CUSTOM_DATA);
		if(chestplate.isEmpty() || component == null) {
			this.fromComponent = null;
			this.gender = Gender.MALE;
			return;
		} else if(fromComponent != null && Objects.equals(component, fromComponent.nbtComponent())) {
			// nothing's changed since the last time we checked, so there's no need to read from the
			// underlying nbt tag again
			return;
		}

		fromComponent = BreastDataComponent.fromComponent(component);
		if(fromComponent == null) {
			this.gender = Gender.MALE;
			return;
		}

		breastPhysics = false;
		pBustSize = fromComponent.breastSize();
		gender = pBustSize >= 0.02f ? Gender.FEMALE : Gender.MALE;
		breasts.updateCleavage(fromComponent.cleavage());
		breasts.updateOffsets(fromComponent.offsets());
		this.jacketLayer = fromComponent.jacket();
	}

	/**
	 * @return {@code true} if the mod has support for the provided entity
	 */
	public static boolean isSupportedEntity(LivingEntity entity) {
		// TODO mannequins are not properly supported right now; this method only returns true to indicate that
		//		our rendering does technically support it, despite the fact that there is no way to properly utilize
		//		them without using janky workarounds.
		return entity instanceof Avatar || entity instanceof ArmorStand;
	}

	/**
	 * Get the configuration for a given entity
	 *
	 * @apiNote Configuration settings for {@link PlayerConfig}s may not be immediately available upon being
	 *          returned, and may take several seconds to be populated if loaded from the
	 *          {@link com.wildfire.main.cloud.CloudSync cloud sync server}.
	 *
	 * @return The relevant {@link EntityConfig}, or {@link PlayerConfig} if given a {@link Player player}
	 */
	public static EntityConfig getEntity(LivingEntity entity) {
		if(entity instanceof Player) {
			return WildfireGender.getOrAddPlayerById(entity.getUUID());
		}
		return CACHE.getUnchecked(entity.getUUID());
	}

	public Gender getGender() {
		return gender;
	}

	public Breasts getBreasts() {
		return breasts;
	}

	public float getBustSize() {
		return pBustSize;
	}

	public boolean hasBreastPhysics() {
		return breastPhysics;
	}

	/**
	 * @apiNote See {@link PlayerConfig#getArmorPhysicsOverride()} for the reasoning behind this being {@link ApiStatus.Obsolete @Obsolete}
	 */
	@ApiStatus.Obsolete
	@Environment(EnvType.CLIENT)
	public boolean getArmorPhysicsOverride() {
		return false;
	}

	public boolean showBreastsInArmor() {
		return true;
	}

	public float getBounceMultiplier() {
		return bounceMultiplier;
	}

	public float getFloppiness() {
		return this.floppyMultiplier;
	}

	public float getVoicePitch() {
		return this.voicePitch;
	}

	public BreastPhysics getLeftBreastPhysics() {
		return lBreastPhysics;
	}
	public BreastPhysics getRightBreastPhysics() {
		return rBreastPhysics;
	}

	// FIXME these update methods should match the rest and be in PlayerConfig instead of here
	// FIXME this should really be redesigned to not have multiple methods with very similar names;
	//		ideally something like `getUVs().skin().left()` etc.
	public UVLayout getLeftBreastUVLayout() {
		return this.leftBreastUVLayout;
	}

	public boolean updateLeftBreastUVLayout(UVLayout layout) {
		return updateValue(Configuration.LEFT_BREAST_UV_LAYOUT, layout, v -> this.leftBreastUVLayout = v);
	}

	public UVLayout getRightBreastUVLayout() {
		return this.rightBreastUVLayout;
	}

	public boolean updateRightBreastUVLayout(UVLayout layout) {
		return updateValue(Configuration.RIGHT_BREAST_UV_LAYOUT, layout, v -> this.rightBreastUVLayout = v);
	}

	public UVLayout getLeftBreastOverlayUVLayout() {
		return this.leftBreastOverlayUVLayout;
	}

	public boolean updateLeftBreastOverlayUVLayout(UVLayout layout) {
		return updateValue(Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT, layout, v -> this.leftBreastOverlayUVLayout = v);
	}

	public UVLayout getRightBreastOverlayUVLayout() {
		return this.rightBreastOverlayUVLayout;
	}

	public boolean updateRightBreastOverlayUVLayout(UVLayout layout) {
		return updateValue(Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT, layout, v -> this.rightBreastOverlayUVLayout = v);
	}

	@Deprecated(forRemoval = true)
	public UVLayout getLeftBreastArmorUVLayout() {
		return this.leftBreastArmorUVLayout;
	}

	@Deprecated(forRemoval = true)
	public UVLayout getRightBreastArmorUVLayout() {
		return this.rightBreastArmorUVLayout;
	}

	protected <VALUE> boolean updateValue(ConfigKey<VALUE> key, VALUE value, Consumer<VALUE> setter) {
		if (key.validate(value)) {
			setter.accept(value);
			return true;
		}
		return false;
	}

	/**
	 * Only used in the case of {@link ArmorStand armor stands}; returns {@code true} if the player who equipped
	 * the armor stand's chestplate has their jacket layer visible.
	 */
	public boolean hasJacketLayer() {
		return jacketLayer;
	}

	@Environment(EnvType.CLIENT)
	public void tickBreastPhysics(LivingEntity entity) {
		IGenderArmor armor = WildfireHelper.getArmorConfig(entity.getItemBySlot(EquipmentSlot.CHEST));

		getLeftBreastPhysics().update(entity, armor);
		getRightBreastPhysics().update(entity, armor);
	}

	@Override
	public String toString() {
		return "%s(uuid=%s, gender=%s)".formatted(getClass().getCanonicalName(), uuid, gender);
	}

	public List<String> getDebugInfo() {
		List<String> info = new ArrayList<>();

		info.add("Gender: " + switch(getGender()) {
			case FEMALE -> ChatFormatting.LIGHT_PURPLE + "Female";
			case MALE -> ChatFormatting.BLUE + "Male";
			case OTHER -> ChatFormatting.GREEN + "Other";
		});
		info.add("Breast size: " + getBustSize());
		info.add("Physics enabled: " + hasBreastPhysics());
		var breasts = getBreasts();
		info.add("Uniboob: " + breasts.isUniboob());
		info.add("Cleavage: " + breasts.getCleavage());
		info.add("Offsets: (" + breasts.getXOffset() + ", " + breasts.getYOffset() + ", " + breasts.getZOffset() + ")");

		return info;
	}
}

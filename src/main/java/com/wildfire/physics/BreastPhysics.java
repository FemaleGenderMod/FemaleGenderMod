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

package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.WildfireHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BreastPhysics {

	public static final float TIGHTNESS_REDUCTION_FACTOR = 0.15F;

	//X-Axis
	private float bounceVelX = 0, targetBounceX = 0, velocityX = 0, positionX, prePositionX;
	//Y-Axis
	private float bounceVel = 0, targetBounceY = 0, velocity = 0, positionY, prePositionY;
	//Rotation
	private float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, wfg_bounceRotation, wfg_preBounceRotation;

	private float breastSize = 0, preBreastSize = 0;

	private EntityPose lastPose;
	private int lastSwingDuration = 6, lastSwingTick = 0;
	private Vec3d prePos;

	private final EntityConfig entityConfig;
	private int randomB = 1;
	private double lastVerticalMoveVelocity;

	public BreastPhysics(EntityConfig entityConfig) {
		this.entityConfig = entityConfig;
	}

	private static boolean vehicleSuppressesRotation(Entity vehicle) {
		return (
				// while you aren't able to normally ride chickens in vanilla, it is still possible through
				// means like /ride, and as chickens attempt to force the rider's body yaw to the same yaw
				// as the chicken (which is likely intended only for baby zombies), this results in unintended
				// behavior with what we're doing
				vehicle instanceof ChickenEntity
				// unsaddled horses (and llamas, which also extend AbstractDonkeyEntity?) also break rotation
				// physics, despite acting similarly to other entities where the rider's body yaw is allowed to
				// (somewhat) freely move around
				|| vehicle instanceof AbstractHorseEntity horseLike && !horseLike.hasSaddleEquipped()
				// camels also suffer from largely the same issue as unsaddled horses when sitting or standing up
				|| vehicle instanceof CamelEntity camel && camel.isStationary()
		);
	}

	private static boolean shouldUseVehicleYaw(LivingEntity rider, Entity vehicle) {
		return (
				vehicle.hasControllingPassenger()
				// boats will typically be caught by the above #hasControllingPassenger() check, but still
				// special case these to catch any weird modded cases that might arise
				|| vehicle instanceof BoatEntity
				// general catch-all for other entities that force the rider's body yaw to match theirs,
				// such as horses
				|| vehicle.getBodyYaw() == rider.getBodyYaw()
		);
	}

	private float calcRotation(LivingEntity entity, float bounceIntensity) {
		Entity vehicle = entity.getVehicle();
		if(vehicle != null) {
			if(vehicleSuppressesRotation(vehicle)) {
				return 0f;
			} else if(shouldUseVehicleYaw(entity, vehicle)) {
				float previous = vehicle instanceof LivingEntity living ? living.lastBodyYaw : vehicle.lastYaw;
				return -((vehicle.getBodyYaw() - previous) / 15f) * bounceIntensity;
			}
		}

		return -((entity.bodyYaw - entity.lastBodyYaw) / 15f) * bounceIntensity;
	}

	// this class cannot be blanket marked as client-side only, as this is referenced in the constructor for EntityConfig;
	// as such, the best we can get here is marking this method as such.
	@Environment(EnvType.CLIENT)
	public void update(LivingEntity entity, IGenderArmor armor) {
		// always suppress the full physics calculations on armor stands
		if(entity instanceof ArmorStandEntity || entityConfig.forceSimplifiedPhysics) {
			simplifiedTick(armor);
			return;
		}

		this.prePositionY = this.positionY;
		this.prePositionX = this.positionX;
		this.wfg_preBounceRotation = this.wfg_bounceRotation;
		this.preBreastSize = this.breastSize;

		if(this.prePos == null) {
			this.prePos = entity.getEntityPos();
			return;
		}

		float breastWeight = entityConfig.getBustSize() * 1.25f;
		float targetBreastSize = entityConfig.getBustSize();

		if (!entityConfig.getGender().canHaveBreasts()) {
			targetBreastSize = 0;
		} else {
			float tightness = MathHelper.clamp(armor.tightness(), 0, 1);
			if(entityConfig.getArmorPhysicsOverride()) tightness = 0; //override resistance
			//Scale breast size by how tight the armor is, clamping at a max adjustment of shrinking by 0.15
			targetBreastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
		}

		breastSize += (breastSize < targetBreastSize) ? Math.abs(breastSize - targetBreastSize) / 2f : -Math.abs(breastSize - targetBreastSize) / 2f;

		Vec3d motion = entity.getEntityPos().subtract(this.prePos);
		this.prePos = entity.getEntityPos();

		float bounceIntensity = (targetBreastSize * 3f) * Math.round((entityConfig.getBounceMultiplier() * 3) * 100) / 100f;
		float resistance = MathHelper.clamp(armor.physicsResistance(), 0, 1);
		if(entityConfig.getArmorPhysicsOverride()) resistance = 0; //override resistance

		//Adjust bounce intensity by physics resistance of the worn armor
		bounceIntensity *= 1 - resistance;

		if(!entityConfig.getBreasts().isUniboob()) {
			bounceIntensity = bounceIntensity * WildfireHelper.randFloat(0.5f, 1.5f);
		}

		tickMovement(entity, motion, bounceIntensity, breastWeight);
		tickPose(entity, bounceIntensity);
		tickVehicle(entity, bounceIntensity, breastWeight);
		tickArmSwing(entity, bounceIntensity);
		finishTick();
	}

	private void simplifiedTick(IGenderArmor armor) {
		if(entityConfig.getGender().canHaveBreasts()) {
			this.breastSize = entityConfig.getBustSize();
			if(!entityConfig.getArmorPhysicsOverride()) {
				float tightness = MathHelper.clamp(armor.tightness(), 0, 1);
				this.breastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
			}
			this.preBreastSize = this.breastSize;
		} else {
			this.preBreastSize = this.breastSize = 0f;
		}
	}

	private void tickMovement(final LivingEntity entity, final Vec3d motion, final float bounceIntensity, final float breastWeight) {
		double vertVelocity = entity.getVelocity().y;
		// Randomize which side the breast will angle toward when the player jumps/has upward velocity applied to them,
		// or stops falling
		if((lastVerticalMoveVelocity <= 0 && vertVelocity > 0) || (lastVerticalMoveVelocity < 0 && vertVelocity == 0)) {
			randomB = entity.getEntityWorld().random.nextBoolean() ? -1 : 1;
		}
		lastVerticalMoveVelocity = vertVelocity;

		this.targetBounceY = (float) motion.y * bounceIntensity;
		this.targetBounceY += breastWeight;

		this.targetRotVel = calcRotation(entity, bounceIntensity);
		this.targetRotVel += (float) motion.y * bounceIntensity * randomB;

		this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10f;

		float f2 = (float) entity.getVelocity().lengthSquared() / 0.2F;
		f2 = f2 * f2 * f2;
		if(f2 < 1.0F) f2 = 1.0F;
		this.targetBounceY += MathHelper.cos(entity.limbAnimator.getAnimationProgress() * 0.6662F + (float)Math.PI) * 0.5F * entity.limbAnimator.getSpeed() * 0.5F / f2;
	}

	private void tickPose(final LivingEntity entity, final float bounceIntensity) {
		EntityPose pose = entity.getPose();
		if(pose != lastPose) {
			if(pose == EntityPose.CROUCHING || lastPose == EntityPose.CROUCHING) {
				this.targetBounceY += bounceIntensity;
			} else if(pose == EntityPose.SLEEPING || lastPose == EntityPose.SLEEPING) {
				this.targetBounceY = bounceIntensity;
			}
			lastPose = pose;
		}
	}

	private void tickVehicle(LivingEntity entity, final float bounceIntensity, final float breastWeight) {
		switch(entity.getVehicle()) {
			case BoatEntity boat -> {
				int rowTime = (int) boat.lerpPaddlePhase(0, entity.limbAnimator.getAnimationProgress());
				int rowTime2 = (int) boat.lerpPaddlePhase(1, entity.limbAnimator.getAnimationProgress());

				float rotationL = (float) MathHelper.clampedLerp(-(float)Math.PI / 3F, -0.2617994F, (double) ((MathHelper.sin(-rowTime2) + 1.0F) / 2.0F));
				float rotationR = (float) MathHelper.clampedLerp(-(float)Math.PI / 4F, (float)Math.PI / 4F, (double) ((MathHelper.sin(-rowTime + 1.0F) + 1.0F) / 2.0F));
				if(rotationL < -1 || rotationR < -0.6f) {
					this.targetBounceY = bounceIntensity / 3.25f;
				}
			}
			case MinecartEntity cart -> {
				float speed = (float) cart.getVelocity().lengthSquared();
				if(Math.random() * speed < 0.5f && speed > 0.2f) {
					this.targetBounceY = (Math.random() > 0.5 ? -bounceIntensity : bounceIntensity) / 6f;
					this.targetBounceY += breastWeight;
				}
			}
			case AbstractHorseEntity horse -> {
				float movement = (float) horse.getVelocity().lengthSquared();
				if(horse.age % clampMovement(movement) == 5 && movement > 0.05f) {
					this.targetBounceY = bounceIntensity / 4f;
					this.targetBounceY += breastWeight;
				}
			}
			case PigEntity pig -> {
				float movement = (float) pig.getVelocity().lengthSquared();
				if(pig.age % clampMovement(movement) == 5 && movement > 0.002f) {
					this.targetBounceY = (bounceIntensity * MathHelper.clamp(movement * 75, 0.1f, 1f)) / 4f;
					this.targetBounceY += breastWeight;
				}
			}
			case StriderEntity strider -> {
				double heightOffset = (double)strider.getHeight() - 0.19
						+ (double)(0.12F * MathHelper.cos(strider.limbAnimator.getAnimationProgress() * 1.5f)
						* 2F * Math.min(0.25F, strider.limbAnimator.getSpeed()));
				this.targetBounceY += ((float) (heightOffset * 3f) - 4.5f) * bounceIntensity;
			}
			case null, default -> {}
		}
	}

	private void tickArmSwing(LivingEntity entity, final float bounceIntensity) {
		int swingDuration = entity.getHandSwingDuration();
		// Require that either the current swing duration is 2 ticks, or the swing duration from the previous tick is,
		// as any faster and the arm effectively doesn't swing at all; we check the previous tick's swing duration for
		// reasons explained later on in this block
		if((swingDuration > 1 || lastSwingDuration > 1) && entity.getPose() != EntityPose.SLEEPING) {
			float rawAmplifier = 0f;
			if(swingDuration < 6) {
				rawAmplifier = 0.15f * (6 - swingDuration);
			} else if(swingDuration > 6) {
				rawAmplifier = -0.055f * (swingDuration - 6);
			}
			// Cap our amplifier at the swing durations of Mining Fatigue IV/Haste II
			float amplifier = MathHelper.clamp(1 + rawAmplifier, 0.6f, 1.3f);

			Arm swingingArm = entity.preferredHand == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
			int swingTickDelta = entity.handSwingTicks - lastSwingTick;
			float swingProgress = distanceFromMedian(0, lastSwingDuration, MathHelper.clamp(lastSwingTick, 0, lastSwingDuration));
			Arm swingingToward = swingProgress > -0.2f ? swingingArm.getOpposite() : swingingArm;

			// consistently apply even with short swing durations, such as with haste
			int everyNthTick = MathHelper.clamp(swingDuration - 1, 1, 5);
			if(entity.handSwinging && entity.age % everyNthTick == 0) {
				this.targetBounceY += (Math.random() > 0.5 ? -0.25f : 0.25f) * amplifier * bounceIntensity;
				// The regular amplifier here makes this look relatively unnatural at high levels of mining fatigue,
				// so instead we're increasing the potency of negative amplifiers (and decreasing positive amplifiers),
				// and clamping this at a lower range than normal.
				// The effective range of these numbers is around the swing durations of Mining Fatigue V to Haste II.
				var xAmp = MathHelper.clamp(1 + (rawAmplifier * (rawAmplifier < 0 ? 1.625f : 0.8f)), 0.25f, 1.225f);
				this.targetBounceX = (0.325f * xAmp * bounceIntensity) * (swingingArm == Arm.RIGHT ? -1f : 1f);
			}

			if(swingTickDelta < 0 && lastSwingTick != lastSwingDuration - 1) {
				// Add a bit of counter-rotation back toward the currently swinging arm if the previous arm swing
				// animation is interrupted
				// Note that we don't check if the player's arm is currently swinging here to account for cases like
				// haste being used to reset a player's swing; one notable example of this is Wynncraft's spell casting,
				// which applies haste to the player when a spell is successfully cast.
				this.targetRotVel += (swingingArm == Arm.RIGHT ? -4f : 4f) * Math.abs(swingProgress) * bounceIntensity;
			} else if(entity.handSwinging && swingDuration > 1) {
				// Otherwise if the swing animation isn't interrupted, attempt to rotate slightly counter to the
				// direction that the body is currently moving
				this.targetRotVel += (swingingToward == Arm.RIGHT ? -0.2f : 0.2f) * amplifier * bounceIntensity;
			}
			lastSwingTick = entity.handSwingTicks;
		}
		if(!entity.handSwinging) {
			lastSwingTick = 0;
		}
		lastSwingDuration = Math.max(swingDuration, 1);
	}

	private void finishTick() {
		float percent = entityConfig.getFloppiness();
		float bounceAmount = 0.45f * (1f - percent) + 0.15f;
		bounceAmount = MathHelper.clamp(bounceAmount, 0.15f, 0.6f);
		float delta = 2.25f - bounceAmount;

		float distanceFromMin = Math.abs(bounceVel + 1.5f) * 0.5f;
		float distanceFromMax = Math.abs(bounceVel - 2.65f) * 0.5f;

		if(bounceVel < -0.5f) {
			targetBounceY += distanceFromMin;
		}
		if(bounceVel > 2.5f) {
			targetBounceY -= distanceFromMax;
		}

		targetBounceY = MathHelper.clamp(targetBounceY, -1.5f, 2.5f);
		targetRotVel = MathHelper.clamp(targetRotVel, -25f, 25f);

		this.velocity = MathHelper.lerp(bounceAmount, this.velocity, (this.targetBounceY - this.bounceVel) * delta);
		this.bounceVel += this.velocity * percent * 1.1625f;

		//X
		this.velocityX = MathHelper.lerp(bounceAmount, this.velocityX, (this.targetBounceX - this.bounceVelX) * delta);
		this.bounceVelX += this.velocityX * percent;

		this.rotVelocity = MathHelper.lerp(bounceAmount, this.rotVelocity, (this.targetRotVel - this.bounceRotVel) * delta);
		this.bounceRotVel += this.rotVelocity * percent;

		this.wfg_bounceRotation = this.bounceRotVel;
		this.positionX = this.bounceVelX;
		this.positionY = this.bounceVel;

		if(this.positionY < -0.5f) this.positionY = -0.5f;
		if(this.positionY > 1.5f) {
			this.positionY = 1.5f;
			this.velocity = 0;
		}
	}

	public float getPrePositionY() {
		return this.prePositionY;
	}
	public float getPositionY() {
		return this.positionY;
	}

	public float getPrePositionX() {
		return this.prePositionX;
	}
	public float getPositionX() {
		return this.positionX;
	}

	public float getBounceRotation() {
		return this.wfg_bounceRotation;
	}
	public float getPreBounceRotation() {
		return this.wfg_preBounceRotation;
	}

	public float getBreastSize() {
		return this.breastSize;
	}
	public float getPreBreastSize() {
		return this.preBreastSize;
	}

	private int clampMovement(float movement) {
		return Math.max((int) (10 - movement*2f), 1);
	}

	/**
	 * Return the distance from the median of the two provided boundary points from a given point
	 *
	 * @param p1    Lower boundary point (inclusive)
	 * @param p2    Upper boundary point (inclusive)
	 * @param point The target point within the range of {@code p1} and {@code p2} to get the distance from the median of
	 *
	 * @return A {@code float} indicating how far the provided {@code point} is from the median of the two boundary
	 *         points, with {@code 1f} being at the median exactly, and {@code 0f} being at either of the two
	 *         provided boundary points.<br>
	 *         If the provided point is in the latter half of the range between the two boundary points, the returned
	 *         float will be negative.
	 *
	 * @throws IllegalArgumentException If {@code p1} is equal to or greater than {@code p2},
	 *                                  or if {@code point} is not within the specified range.
	 */
	@SuppressWarnings("SameParameterValue")
	private static float distanceFromMedian(final int p1, final int p2, float point) {
		// sanity checks
		if(p1 >= p2) {
			throw new IllegalArgumentException("p2 must be greater than p1");
		}
		if(point < p1 || point > p2) {
			throw new IllegalArgumentException(point + " is not within bounds of (" + p1 + ", " + p2 + ")");
		}

		if(point == p1 || point == p2) {
			return 0;
		}
		// subtract p1 to get the actual inner range, then divide to get the median
		float median = (p2 - p1) / 2f;
		point -= p1;
		if(point > median) {
			// invert the provided point to instead become smaller the further we are away from the median
			// in the latter half of the specified range
			point = -(median - (point - median));
		}
		return point / median;
	}
}

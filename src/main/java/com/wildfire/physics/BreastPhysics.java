package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.entitydata.EntityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BreastPhysics {
    private float bounceVelX = 0.0F;
    private float targetBounceX = 0.0F;
    private float velocityX = 0.0F;
    private float positionX;
    private float prePositionX;
    private float bounceVel = 0.0F;
    private float targetBounceY = 0.0F;
    private float velocity = 0.0F;
    private float positionY;
    private float prePositionY;
    private float bounceRotVel = 0.0F;
    private float targetRotVel = 0.0F;
    private float rotVelocity = 0.0F;
    private float wfg_bounceRotation;
    private float wfg_preBounceRotation;
    private float breastSize = 0.0F;
    private float preBreastSize = 0.0F;
    private Pose lastPose;
    private int lastSwingDuration = 6;
    private int lastSwingTick = 0;
    private Vec3 prePos;
    private final EntityConfig entityConfig;
    private int randomB = 1;
    private boolean alreadyFalling = false;

    public BreastPhysics(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }

    private static boolean vehicleSuppressesRotation(Entity vehicle) {
        if (vehicle instanceof Boat) return true;
        if (vehicle instanceof AbstractHorse horse && !horse.isSaddled()) return true;
        if (vehicle instanceof Camel camel && camel.isCamelSitting()) return true;
        return false;
    }

    private static boolean shouldUseVehicleYaw(LivingEntity rider, Entity vehicle) {
        return vehicle.isControlledByLocalInstance() || vehicle instanceof Boat || vehicle.getControllingPassenger() == rider;
    }

    private static float calcRotation(LivingEntity entity, float bounceIntensity) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            if (vehicleSuppressesRotation(vehicle)) return 0.0F;
            if (shouldUseVehicleYaw(entity, vehicle)) {
                if (vehicle instanceof LivingEntity livingVehicle) {
                    return -((livingVehicle.yBodyRot - livingVehicle.yBodyRotO) / 15.0F) * bounceIntensity;
                }
                return -((vehicle.getYRot() - vehicle.yRotO) / 15.0F) * bounceIntensity;
            }
        }
        return -((entity.yBodyRot - entity.yBodyRotO) / 15.0F) * bounceIntensity;
    }

    @OnlyIn(Dist.CLIENT)
    public void update(LivingEntity entity, IGenderArmor armor) {
        if (entity instanceof ArmorStand) {
            if (this.entityConfig.getGender().canHaveBreasts()) {
                this.breastSize = this.entityConfig.getBustSize();
                if (!this.entityConfig.getArmorPhysicsOverride()) {
                    float tightness = Mth.clamp(armor.tightness(), 0.0F, 1.0F);
                    this.breastSize *= 1.0F - 0.15F * tightness;
                }
                this.preBreastSize = this.breastSize;
            } else {
                this.breastSize = 0.0F;
            }
        } else {
            this.prePositionY = this.positionY;
            this.prePositionX = this.positionX;
            this.wfg_preBounceRotation = this.wfg_bounceRotation;
            this.preBreastSize = this.breastSize;

            if (this.prePos == null) {
                this.prePos = entity.position();
            } else {
                float breastWeight = this.entityConfig.getBustSize() * 1.25F;
                float targetBreastSize = this.entityConfig.getBustSize();

                if (!this.entityConfig.getGender().canHaveBreasts()) {
                    targetBreastSize = 0.0F;
                } else {
                    float tightness = Mth.clamp(armor.tightness(), 0.0F, 1.0F);
                    if (this.entityConfig.getArmorPhysicsOverride()) tightness = 0.0F;
                    targetBreastSize *= 1.0F - 0.15F * tightness;
                }

                this.breastSize += (this.breastSize < targetBreastSize) ?
                        Math.abs(this.breastSize - targetBreastSize) / 2.0F :
                        -Math.abs(this.breastSize - targetBreastSize) / 2.0F;

                Vec3 motion = entity.position().subtract(this.prePos);
                this.prePos = entity.position();

                float bounceIntensity = targetBreastSize * 3.0F * (this.entityConfig.getBounceMultiplier() * 3.0F);
                float resistance = Mth.clamp(armor.physicsResistance(), 0.0F, 1.0F);
                if (this.entityConfig.getArmorPhysicsOverride()) resistance = 0.0F;

                bounceIntensity *= 1.0F - resistance;
                if (!this.entityConfig.getBreasts().isUniboob()) {
                    bounceIntensity *= WildfireHelper.randFloat(0.5F, 1.5F);
                }

                if (entity.fallDistance > 0.0F && !this.alreadyFalling) {
                    this.randomB = entity.getRandom().nextBoolean() ? -1 : 1;
                    this.alreadyFalling = true;
                }
                if (entity.fallDistance == 0.0F) this.alreadyFalling = false;

                this.targetBounceY = (float)motion.y * bounceIntensity + breastWeight;
                this.targetRotVel = calcRotation(entity, bounceIntensity);
                this.targetRotVel += (float)motion.y * bounceIntensity * (float)this.randomB;

                float speedLength = (float)entity.getDeltaMovement().horizontalDistance();
                float f2 = Math.max(1.0F, (float)Math.pow(speedLength / 0.2F, 3));

                // Lógica de rebote por caminata
                this.targetBounceY += Mth.cos(entity.walkAnimation.position() * 0.6662F + (float)Math.PI) * 0.5F * entity.walkAnimation.speed() * 0.5F / f2;

                Pose pose = entity.getPose();
                if (pose != this.lastPose) {
                    if (pose == Pose.CROUCHING || this.lastPose == Pose.CROUCHING) {
                        this.targetBounceY += bounceIntensity;
                    }
                    this.lastPose = pose;
                }

                // Lógica específica para vehículos
                Entity vehicle = entity.getVehicle();
                if (vehicle instanceof Boat boat) {
                    float rowL = boat.getRowingTime(0, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
                    if (rowL > 0) this.targetBounceY = bounceIntensity / 3.25F;
                } else if (vehicle instanceof AbstractMinecart cart) {
                    if (Math.random() * cart.getDeltaMovement().length() < 0.5F) {
                        this.targetBounceY = (Math.random() > 0.5D ? -bounceIntensity : bounceIntensity) / 6.0F + breastWeight;
                    }
                } else if (vehicle instanceof Horse horse) {
                    if (horse.tickCount % 10 == 5 && horse.getDeltaMovement().length() > 0.05F) {
                        this.targetBounceY = bounceIntensity / 4.0F + breastWeight;
                    }
                }

                // Lógica de Swing (Ataque/Uso)
                if (entity.swinging && entity.swingTime > 0) {
                    float swingProgress = (float)entity.swingTime / (float)this.lastSwingDuration;
                    HumanoidArm arm = entity.swingingArm == InteractionHand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
                    this.targetRotVel += (arm == HumanoidArm.RIGHT ? -2.5F : 2.5F) * swingProgress * bounceIntensity;
                }

                // Aplicación de la física de muelle (Spring Physics)
                float floppiness = this.entityConfig.getFloppiness();
                float damping = Mth.clamp(0.45F * (1.0F - floppiness) + 0.15F, 0.15F, 0.6F);
                float springForce = 2.25F - damping;

                this.targetBounceY = Mth.clamp(this.targetBounceY, -1.5F, 2.5F);
                this.velocity = Mth.lerp(damping, this.velocity, (this.targetBounceY - this.bounceVel) * springForce);
                this.bounceVel += this.velocity * floppiness * 1.1625F;

                this.wfg_bounceRotation = Mth.lerp(damping, this.wfg_bounceRotation, this.targetRotVel);
                this.positionY = Mth.clamp(this.bounceVel, -0.5F, 1.5F);
            }
        }
    }

    public float getBreastSize(float partialTicks) {
        return Mth.lerp(partialTicks, this.preBreastSize, this.breastSize);
    }

    public float getPositionY() { return this.positionY; }
    public float getPositionX() { return this.positionX; }
    public float getBounceRotation() { return this.wfg_bounceRotation; }
    public float getPrePositionY() { return this.prePositionY; }
    public float getPrePositionX() { return this.prePositionX; }
    public float getPreBounceRotation() { return this.wfg_preBounceRotation; }
}
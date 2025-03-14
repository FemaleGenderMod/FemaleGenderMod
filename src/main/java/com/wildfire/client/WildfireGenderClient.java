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

package com.wildfire.client;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.api.IGenderArmor;
import com.wildfire.client.gui.SyncedPlayersLayer;
import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.client.gui.screen.WildfireFirstTimeSetupScreen;
import com.wildfire.client.render.GenderLayer;
import com.wildfire.client.render.HolidayFeaturesRenderer;
import com.wildfire.client.resources.GenderArmorResourceManager;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.ContributorNametag;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.ServerboundSyncPacket;
import com.wildfire.main.text.IHasTranslationKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Mod(value = WildfireGender.MODID, dist = Dist.CLIENT)
public class WildfireGenderClient {

    //TODO - 1.21.4: Give this a name
    private static final Executor LOAD_EXECUTOR = Util.ioPool();//.forName("wildfire_gender$loadPlayerData");
    // TODO merge WildfireGender.CONTRIBUTOR_UUIDS into this?
    public static final CompletableFuture<Map<UUID, ContributorNametag>> CONTRIBUTOR_NAMETAGS = CloudSync.getContributors();
    public static WildfireGenderClient INSTANCE;

    //TODO - 1.21: Re-evaluate this conflict context
    public final KeyMapping configKey = createKey(WildfireLang.KEY_CONFIG, KeyConflictContext.UNIVERSAL, GLFW.GLFW_KEY_G, () -> {
        //When the key goes from not down to down try to open the wardrobe screen
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null && minecraft.player != null) {
            if (GeneralClientConfig.INSTANCE.firstTimeLoad.get() && CloudSync.isAvailable()) {
                minecraft.setScreen(new WildfireFirstTimeSetupScreen(null, minecraft.player.getUUID()));
            } else {
                minecraft.setScreen(new WardrobeBrowserScreen(null, minecraft.player.getUUID()));
            }
        }
    });
    public final KeyMapping toggleKey = createKey(WildfireLang.KEY_TOGGLE, KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN, () -> INSTANCE.renderBreasts ^= true);

    //TODO: Eventually we may want to replace this with a map or something and replace things like drowning sounds with other drowning sounds
    private final Set<SoundEvent> playerHurtSounds = Set.of(
          SoundEvents.PLAYER_HURT,
          SoundEvents.PLAYER_HURT_DROWN,
          SoundEvents.PLAYER_HURT_FREEZE,
          SoundEvents.PLAYER_HURT_ON_FIRE,
          SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH
    );
    //Note: this option is not intended to be saved in any persistent manner
    //TODO - 1.21: Figure out where fabric uses this option if anywhere?
    private boolean renderBreasts = true;
    private int timer = 0;

    public WildfireGenderClient(ModContainer modContainer, IEventBus modEventBus) {
        INSTANCE = this;
        modContainer.registerConfig(Type.CLIENT, GeneralClientConfig.INSTANCE.configSpec, "WildfireGender/client.toml");
        modEventBus.addListener(this::entityLayers);
        modEventBus.addListener(this::registerKeybindings);
        modEventBus.addListener(this::registerOverlays);
        modEventBus.addListener(this::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onGUI);
        NeoForge.EVENT_BUS.addListener(this::onEntityTick);
        NeoForge.EVENT_BUS.addListener(this::connect);
        NeoForge.EVENT_BUS.addListener(this::onEntityLeave);
        NeoForge.EVENT_BUS.addListener(this::disconnect);
        NeoForge.EVENT_BUS.addListener(this::onRenderNameTag);
        NeoForge.EVENT_BUS.addListener(this::onRenderAttributes);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onPlaySound);
    }

    private static KeyMapping createKey(IHasTranslationKey name, KeyConflictContext conflictContext, int keyCode, Runnable onPress) {
        return new KeyMapping(name.getTranslationKey(), conflictContext, InputConstants.Type.KEYSYM, keyCode, WildfireLang.KEY_CATEGORY.getTranslationKey()) {
            @Override
            public void setDown(boolean value) {
                if (value && !isDown()) {
                    onPress.run();
                }
                super.setDown(value);
            }
        };
    }

    public static CompletableFuture<@Nullable PlayerConfig> loadGenderInfo(UUID uuid, boolean markForSync, boolean bypassQueue) {
        PlayerConfig cache = WildfireGender.getPlayerById(uuid);
        if (cache == null) {
            return CompletableFuture.completedFuture(null);
        }
        return loadGenderInfo(cache, markForSync, bypassQueue);
    }

    public static CompletableFuture<@NotNull PlayerConfig> loadGenderInfo(PlayerConfig player, boolean markForSync, boolean bypassQueue) {
        return CompletableFuture.supplyAsync(() -> {
            UUID uuid = player.uuid;
            if (player.hasLocalConfig()) {
                player.loadFromDisk(markForSync);
            } else if (player.syncStatus == PlayerConfig.SyncStatus.UNKNOWN) {
                JsonObject data;
                try {
                    CompletableFuture<JsonObject> future = bypassQueue ? CloudSync.getProfile(uuid) : CloudSync.queueFetch(uuid);
                    data = future.join();
                } catch (Exception e) {
                    WildfireGender.LOGGER.error("Failed to fetch profile from sync server", e);
                    throw e;
                }
                // make sure the server we're connected to hasn't provided player data while we were fetching data from
                // the sync server
                if (data != null && player.syncStatus == PlayerConfig.SyncStatus.UNKNOWN) {
                    player.updateFromJson(data);
                    if (markForSync) {
                        player.needsSync = true;
                    }
                }
            }
            return player;
        }, LOAD_EXECUTOR);
    }

    @Nullable
    private Component getNametag(UUID uuid) {
        ContributorNametag custom;
        try {
            custom = CONTRIBUTOR_NAMETAGS.getNow(Collections.emptyMap()).get(uuid);
        } catch (Exception e) {
            custom = null;
        }
        if (custom != null) {
            return custom.asText();
        } else if (WildfireGender.CREATOR_UUID.equals(uuid)) {
            return WildfireLang.NAME_TAG_CREATOR.translateColored(ChatFormatting.LIGHT_PURPLE);
        } else if (WildfireGender.CONTRIBUTOR_UUIDS.contains(uuid)) {
            return WildfireLang.NAME_TAG_CONTRIBUTOR.translateColored(ChatFormatting.GOLD);
        }
        return null;
    }

    public static List<PlayerInfo> collectPlayerEntries() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return Collections.emptyList();
        }
        //TODO - 1.21: Should this be getOnlinePlayers instead?
        return player.connection.getListedOnlinePlayers().stream()
              .filter(entry -> !entry.getProfile().getId().equals(player.getUUID()))
              .filter(entry -> {
                  PlayerConfig cfg = WildfireGender.getPlayerById(entry.getProfile().getId());
                  return cfg != null && cfg.getSyncStatus() != PlayerConfig.SyncStatus.UNKNOWN;
              }).limit(40L)
              .toList();
    }

    private void entityLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model model : event.getSkins()) {
            if (event.getSkin(model) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new GenderLayer<>(renderer, event.getContext().getModelManager()));
                renderer.addLayer(new HolidayFeaturesRenderer(renderer));
            }
        }
        if (event.getRenderer(EntityType.ARMOR_STAND) instanceof ArmorStandRenderer renderer) {
            renderer.addLayer(new GenderLayer<>(renderer, event.getContext().getModelManager()));
        }
    }

    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(GenderArmorResourceManager.INSTANCE);
    }

    private void registerKeybindings(RegisterKeyMappingsEvent event) {
        event.register(configKey);
        event.register(toggleKey);
    }

    private void registerOverlays(RegisterGuiLayersEvent event) {
        //TODO - 1.21: Figure out whether this should be above, below, and where it should be in the stack
        event.registerAboveAll(WildfireGender.rl("synced_players"), SyncedPlayersLayer.INSTANCE);
    }

    private void onGUI(ClientTickEvent.Post evt) {
        Player player = Minecraft.getInstance().player;
        if (Minecraft.getInstance().level == null || player == null) {
            return;
        }
        PlayerConfig clientConfig = WildfireGender.getPlayerById(player.getUUID());
        timer++;

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        //20 ticks per second / 5 = 4 times per second
        if (clientConfig != null && clientConfig.needsSync && timer % 5 == 0 && connection != null && connection.hasChannel(ServerboundSyncPacket.TYPE)) {
            PacketDistributor.sendToServer(new ServerboundSyncPacket(clientConfig));
            clientConfig.needsSync = false;
        }

        if (timer % 40 == 0) {
            CloudSync.sendNextQueueBatch();
            if (clientConfig != null) {
                clientConfig.attemptCloudSync();
            }
        }
    }

    private void onEntityTick(EntityTickEvent.Post evt) {
        Entity entity = evt.getEntity();
        if (entity.level().isClientSide && entity instanceof LivingEntity living && EntityConfig.isSupportedEntity(living)) {
            EntityConfig cfg = EntityConfig.getEntity(living);
            if (entity instanceof ArmorStand) {
                cfg.readFromStack(living.getItemBySlot(EquipmentSlot.CHEST));
            }
            cfg.tickBreastPhysics(living);
        }
    }

    private void connect(ClientPlayerNetworkEvent.LoggingIn evt) {
        //TODO - 1.21: Re-evaluate what this is meant to be now I think this may just gets handled automatically now after the initial five ticks
        /*UUID uuid = evt.getPlayer().getUUID();
        PlayerConfig aPlr = WildfireGender.getPlayerById(uuid);
        if (aPlr == null) {
            aPlr = new PlayerConfig(uuid);
            WildfireGender.CACHE.put(uuid, aPlr);
            //Mark the player as needing sync if it is the client's own player
            Player player = Minecraft.getInstance().player;
            WildfireGender.loadGenderInfo(uuid, player != null && uuid.equals(player.getUUID()));
        }*/
    }

    private void onEntityLeave(EntityLeaveLevelEvent evt) {
        if (evt.getLevel().isClientSide) {
            // note that we don't attempt to unload players; they're instead only ever unloaded once we leave a world
            //TODO - 1.21: FIXME, we don't seem to be invalidating the cache of other players when they disconnect
            EntityConfig.CACHE.invalidate(evt.getEntity().getUUID());
        }
    }

    private void disconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        WildfireGender.CACHE.invalidateAll();
        EntityConfig.CACHE.invalidateAll();
    }

    private void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getEntityRenderer() instanceof PlayerRenderer renderer)) {
            return;
        }
        Component nametag = getNametag(player.getUUID());
        if (nametag == null) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        float translationAmt = switch (player.getPose()) {
            case Pose.CROUCHING -> 0.8F;
            case Pose.SLEEPING, Pose.DYING -> 0.125F;
            case Pose.SWIMMING, Pose.FALL_FLYING, Pose.SPIN_ATTACK -> 0.3F;
            default -> 0.95F;
        };
        //TODO - 1.21: Figure out the following poses
        //public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F)
        //        .withEyeHeight(1.62F)
        //        .withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT));
        //protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(0.2F);
        //.put(Pose.STANDING, STANDING_DIMENSIONS)
        //        .put(Pose.SLEEPING, SLEEPING_DIMENSIONS)
        //        .put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
        //        .put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
        //        .put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
        //        .put(
        //            Pose.CROUCHING,
        //            EntityDimensions.scalable(0.6F, 1.5F)
        //                .withEyeHeight(1.27F)
        //                .withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT))
        //        )
        //        .put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(1.62F))
        //Vec3 vec3 = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getViewYRot(event.getPartialTick()));

        poseStack.translate(0f, translationAmt, 0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        //TODO - 1.21: Figure this out
        //renderHelper.accept(nametag);
        poseStack.popPose();
        // shift the rest of the name tag up a little bit
        //TODO - 1.21: Re-evaluate this
        //poseStack.translate(0f, 2.15F * 1.15F * 0.025F, 0f);
        //renderer.renderNameTag();
    }

    private void onRenderAttributes(AddAttributeTooltipsEvent event) {
        ItemStack stack = event.getStack();
        if (stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).showInTooltip()) {
            //TODO - 1.21: Do we really only want to do this for armor items?
            if (stack.getItem() instanceof ArmorItem) {
                Player player = event.getContext().player();
                if (player == null || !GeneralClientConfig.INSTANCE.armorStat.get()) {
                    return;
                }
                PlayerConfig playerConfig = WildfireGender.getPlayerById(player.getUUID());
                if (playerConfig == null || !playerConfig.getGender().canHaveBreasts()) {
                    return;
                }

                EquipmentSlot slot = stack.getEquipmentSlot();
                if (slot == null) {
                    Equipable equipable = Equipable.get(stack);
                    if (equipable != null) {
                        slot = equipable.getEquipmentSlot();
                    }
                }
                //TODO - 1.21.4: Switch to this
                //Equippable equippable = stack.get(DataComponentTypes.EQUIPPABLE);
                //if (equippable != null && equippable.slot() == EquipmentSlot.CHEST) {
                if (slot == EquipmentSlot.CHEST) {
                    IGenderArmor armorConfig = WildfireHelper.getArmorConfig(stack);
                    //TODO - 1.21: should we skip this when the config always hides breasts?
                    if (armorConfig.coversBreasts()) {
                        float physResistance = armorConfig.physicsResistance();
                        event.addTooltipLines(WildfireLang.ARMOR_TOOLTIP.translateColored(ChatFormatting.LIGHT_PURPLE, Math.floor(physResistance * 100) / 100f));
                    }
                }
            }
        }
    }

    private void onPlaySound(PlayLevelSoundEvent.AtEntity event) {
        if (GeneralClientConfig.INSTANCE.disableSoundReplacement.get()) {
            return;
        }
        Holder<SoundEvent> soundHolder = event.getSound();
        if (soundHolder != null) {
            SoundEvent soundEvent = soundHolder.value();
            if (playerHurtSounds.contains(soundEvent) && event.getEntity() instanceof Player p && p.level().isClientSide) {
                //Cancel as we handle all hurt sounds manually so that we can
                event.setCanceled(true);
                if (p.hurtTime == p.hurtDuration && p.hurtTime > 0) {
                    //Note: We check hurtTime == hurtDuration and hurtTime > 0 or otherwise when the server sends a hurt sound to the client
                    // and the client will check itself instead of the player who was damaged.
                    PlayerConfig plr = WildfireGender.getPlayerById(p.getUUID());
                    if (plr != null && plr.hasHurtSounds()) {
                        SoundEvent soundOverride = plr.getGender().getHurtSound();
                        if (soundOverride != null) {
                            //If the player who produced the hurt sound is a female sound replace it
                            soundEvent = soundOverride;
                            event.setNewPitch(event.getNewPitch() + plr.getVoicePitch());
                        }
                    }
                } else {
                    Player player = Minecraft.getInstance().player;
                    if (player != null && p.getUUID().equals(player.getUUID())) {
                        //Skip playing remote hurt sounds. Note: sounds played via /playsound will not be intercepted
                        // as they are played directly
                        //Note: This might behave slightly strangely if a mod is manually firing a player damage sound
                        // only on the server and not also on the client
                        //TODO: Ideally we would fix that edge case but I find it highly unlikely it will ever actually occur
                        return;
                    }
                }
                p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), soundEvent, event.getSource(), event.getNewVolume(), event.getNewPitch(), false);
            }
        }
    }
}
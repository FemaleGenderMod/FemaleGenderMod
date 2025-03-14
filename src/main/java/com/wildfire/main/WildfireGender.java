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

package com.wildfire.main;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import com.wildfire.api.IGenderArmor;
import com.wildfire.api.WildfireAPI;
import com.wildfire.client.WildfireGenderClient;
import com.wildfire.main.entitydata.BreastDataComponent;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.ClientboundSyncPacket;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(WildfireGender.MODID)
public class WildfireGender {

    public static final String MODID = WildfireAPI.MODID;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final UUID CREATOR_UUID = UUID.fromString("33c937ae-6bfc-423e-a38e-3a613e7c1256");
    public static final List<UUID> CONTRIBUTOR_UUIDS = List.of(
          UUID.fromString("70336328-0de7-430e-8cba-2779e2a05ab5"), //celeste
          UUID.fromString("64e57307-72e5-4f43-be9c-181e8e35cc9b"), //pupnewfster
          UUID.fromString("618a8390-51b1-43b2-a53a-ab72c1bbd8bd"), //Kichura
          UUID.fromString("33feda66-c706-4725-8983-f62e5e6cbee7"), //BlueLight
          UUID.fromString("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc"), //Diademiemi
          UUID.fromString("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1"), //Arcti.cc
          UUID.fromString("3f36f7e9-7459-43fe-87ce-4e8a5d47da80"), //IzzyBizzy45
          UUID.fromString("525b0455-15e9-49b7-b61d-f291e8ee6c5b"), //Powerless001
          UUID.fromString("6e0e0db3-19e9-4fa7-af76-a6d3651c57b9") //A2 76
    );

    public static final LoadingCache<UUID, PlayerConfig> CACHE;

    static {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        // Only automatically expire cache entries on the client; a server may go a decent while without accessing
        // the player cache, and we can't easily re-cache a player's settings on a server, while a client
        // will typically either receive settings from the server in a sync, or simply re-fetch from
        // a local config file or from the cloud.
        // Note that servers will manually invalidate cache entries upon a player disconnecting
        // (see WildfireEventHandler#playerDisconnected).
        if (FMLEnvironment.dist.isClient()) {
            builder.expireAfterAccess(Duration.ofMinutes(15));
        }
        CACHE = builder.build(new CacheLoader<>() {
            @Override
            public @NotNull PlayerConfig load(@NotNull UUID key) {
                PlayerConfig config = new PlayerConfig(key);
                // only attempt to load player data on the client, and if the provided uuid is valid
                if (FMLEnvironment.dist.isClient() && key.version() == 4) {
                    // markForSync being true will only ever do anything for the client player
                    WildfireGenderClient.loadGenderInfo(config, true, false);
                }
                return config;
            }
        });
    }

    public static WildfireGender INSTANCE;

    //Tracked player to the set of tracking players
    private final Map<UUID, Set<ServerPlayer>> trackedPlayers = new HashMap<>();
    public final ModContainer container;

    public WildfireGender(ModContainer modContainer, IEventBus modEventBus) {
        INSTANCE = this;
        this.container = modContainer;

        modEventBus.addListener(WildfireHelper::registerCapabilities);
        modEventBus.addListener(WildfireHelper::registerPackets);
        NeoForge.EVENT_BUS.addListener(this::onStartTracking);
        NeoForge.EVENT_BUS.addListener(this::onStopTracking);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onEntitySpawn);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onRightClickArmorStand);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static Set<ServerPlayer> getTrackers(Player target) {
        return INSTANCE.trackedPlayers.getOrDefault(target.getUUID(), Collections.emptySet());
    }

    @Nullable
    public static PlayerConfig getPlayerById(UUID id) {
        return CACHE.getIfPresent(id);
    }

    public static PlayerConfig getOrAddPlayerById(UUID id) {
        return CACHE.getUnchecked(id);
    }

    private void onStartTracking(PlayerEvent.StartTracking evt) {
        if (evt.getTarget() instanceof Player toSync && evt.getEntity() instanceof ServerPlayer sendTo && sendTo.connection.hasChannel(ClientboundSyncPacket.TYPE)) {
            trackedPlayers.computeIfAbsent(toSync.getUUID(), uuid -> new HashSet<>()).add(sendTo);

            PlayerConfig genderToSync = WildfireGender.getPlayerById(toSync.getUUID());
            if (genderToSync == null) {
                return;
            }
            // Note that we intentionally don't check if we've previously synced a player with this code path;
            // because we use entity tracking to sync, it's entirely possible that one player would leave the
            // tracking distance of another, change their settings, and then re-enter their tracking distance;
            // we wouldn't sync while they're out of tracking distance, and as such, their settings would be out
            // of sync until they relog.
            PacketDistributor.sendToPlayer(sendTo, new ClientboundSyncPacket(genderToSync));
        }
    }

    private void onStopTracking(PlayerEvent.StopTracking evt) {
        if (evt.getTarget() instanceof Player toSync && evt.getEntity() instanceof ServerPlayer sendTo) {
            UUID uuid = toSync.getUUID();
            Set<ServerPlayer> trackers = trackedPlayers.get(uuid);
            if (trackers != null && trackers.remove(sendTo) && trackers.isEmpty()) {
                trackedPlayers.remove(uuid);
            }
        }
    }

    private static EquipmentSlot getEquipmentSlot(ItemStack stack) {
        EquipmentSlot slot = stack.getEquipmentSlot();
        if (slot == null) {
            Equipable equipable = Equipable.get(stack);
            return equipable == null ? EquipmentSlot.MAINHAND : equipable.getEquipmentSlot();
        }
        return slot;
    }

    private void onEntitySpawn(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ItemEntity entity && getEquipmentSlot(entity.getItem()) == EquipmentSlot.CHEST) {
            //Remove our tag if it is present when an item drops (such as from an armor stand being broken)
            ItemStack stack = entity.getItem();
            if (BreastDataComponent.removeFromStack(stack)) {
                entity.setItem(stack);
            }
        }
    }

    private void onRightClickArmorStand(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        //Copy of various checks from ArmorStand#interactAt, so that we can only apply it if a stack is being transferred
        if (!player.level().isClientSide && event.getTarget() instanceof ArmorStand armorStand && !armorStand.isMarker() && !player.isSpectator()) {
            ItemStack stack = player.getItemInHand(event.getHand());
            // Only apply to chestplates
            if (stack.isEmpty()) {
                EquipmentSlot clickedSlot = armorStand.getClickedSlot(event.getLocalPos());
                EquipmentSlot equipmentslot2 = armorStand.isDisabled(clickedSlot) ? getEquipmentSlot(stack) : clickedSlot;
                if (equipmentslot2 == EquipmentSlot.CHEST) {
                    //Copy of logic from ArmorStand#swapItem
                    ItemStack itemstack = armorStand.getItemBySlot(equipmentslot2);
                    if (!itemstack.isEmpty()) {
                        if ((armorStand.disabledSlots & 1 << equipmentslot2.getFilterFlag() + 8) == 0) {
                            //Stack is being removed from the armor stand, remove the corresponding tag key we added if it is present
                            BreastDataComponent.removeFromStack(itemstack);
                        }
                    }
                }
            } else if (getEquipmentSlot(stack) == EquipmentSlot.CHEST && WildfireHelper.getArmorConfig(stack).armorStandsCopySettings() &&
                       !armorStand.isDisabled(EquipmentSlot.CHEST)) {
                //Copy of logic from ArmorStand#swapItem
                ItemStack itemstack = armorStand.getItemBySlot(EquipmentSlot.CHEST);
                if (!itemstack.isEmpty() && (armorStand.disabledSlots & 1 << EquipmentSlot.CHEST.getFilterFlag() + 8) != 0) {
                    return;
                } else if (itemstack.isEmpty() && (armorStand.disabledSlots & 1 << EquipmentSlot.CHEST.getFilterFlag() + 16) != 0) {
                    return;
                } else if (player.getAbilities().instabuild && itemstack.isEmpty()) {
                    //Copy the stack and set it in the armor stand manually, cancelling the event so that it doesn't go through
                    // so that we can apply it but not set nbt on the held stack
                    stack = stack.copyWithCount(1);
                    event.setCanceled(true);
                } else if (!itemstack.isEmpty()) {
                    //Stack is being removed from the armor stand remove the corresponding tag key we added if it is present
                    BreastDataComponent.removeFromStack(itemstack);
                    if (stack.getCount() > 1) {
                        //If the held stack has a size greater than one, we are only removing so can exit. Otherwise we are swapping
                        // so need to add to the held stack
                        return;
                    }
                } else {
                    //Copy the stack and set it in the armor stand manually, cancelling the event so that it doesn't go through
                    // so that we can apply it but not set nbt on the held stack
                    stack = stack.split(1);
                    event.setCanceled(true);
                }

                PlayerConfig playerConfig = WildfireGender.getPlayerById(player.getUUID());
                if (playerConfig == null) {
                    BreastDataComponent.removeFromStack(itemstack);
                } else {
                    IGenderArmor armorConfig = WildfireHelper.getArmorConfig(stack);
                    if (armorConfig.armorStandsCopySettings()) {
                        BreastDataComponent component = BreastDataComponent.fromPlayer(player, playerConfig);
                        if (component != null) {
                            component.write(player.level().registryAccess(), stack);
                        }
                    }
                }
                if (event.isCanceled()) {
                    //We cancelled it, so we need to now actually set it as well
                    armorStand.setItemSlot(EquipmentSlot.CHEST, stack);
                }
            }
        }
    }
}
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

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.events.*;
import com.wildfire.gui.SyncedPlayerList;
import com.wildfire.gui.WildfireToast;
import com.wildfire.gui.screen.WardrobeBrowserScreen;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.entitydata.BreastDataComponent;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.ServerboundSyncPacket;
import com.wildfire.main.networking.WildfireSync;
import com.wildfire.render.GenderArmorLayer;
import com.wildfire.render.GenderLayer;
import com.wildfire.render.GenderRenderState;
import com.wildfire.render.HolidayFeaturesRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

public final class WildfireEventHandler {
	private WildfireEventHandler() {
		throw new UnsupportedOperationException();
	}

	private static final KeyMapping CONFIG_KEYBIND;
	private static final KeyMapping TOGGLE_KEYBIND;
	private static int timer = 0;

	public static KeyMapping getConfigKeybind() {
		return CONFIG_KEYBIND;
	}

	static {
		// note that all the Util.make()s are required, as otherwise a dedicated server will crash during
		// static class initialization due to references to classes that don't exist
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			var category = Util.make(() -> KeyMapping.Category.register(WildfireGender.id("generic")));
			CONFIG_KEYBIND = Util.make(() -> {
				KeyMapping keybind = new KeyMapping("key.wildfire_gender.gender_menu", GLFW.GLFW_KEY_H, category);
				KeyBindingHelper.registerKeyBinding(keybind);
				return keybind;
			});
			TOGGLE_KEYBIND = Util.make(() -> {
				KeyMapping keybind = new KeyMapping("key.wildfire_gender.toggle", GLFW.GLFW_KEY_UNKNOWN, category);
				KeyBindingHelper.registerKeyBinding(keybind);
				return keybind;
			});
		} else {
			CONFIG_KEYBIND = null;
			TOGGLE_KEYBIND = null;
		}
	}

	/**
	 * Register all events applicable to the server-side for both a dedicated server and singleplayer
	 */
	public static void registerCommonEvents() {
		EntityTrackingEvents.START_TRACKING.register(WildfireEventHandler::onBeginTracking);
		ServerPlayConnectionEvents.DISCONNECT.register(WildfireEventHandler::playerDisconnected);
		ArmorStandInteractEvents.EQUIP.register(WildfireEventHandler::onEquipArmorStand);
		ArmorStandInteractEvents.REMOVE.register(BreastDataComponent::removeFromStack);
	}

	/**
	 * Register all client-side events
	 */
	@Environment(EnvType.CLIENT)
	public static void registerClientEvents() {
		ClientEntityEvents.ENTITY_UNLOAD.register(WildfireEventHandler::onEntityUnload);
		ClientTickEvents.END_CLIENT_TICK.register(WildfireEventHandler::onClientTick);
		ClientPlayConnectionEvents.DISCONNECT.register(WildfireEventHandler::clientDisconnect);
		ClientPlayConnectionEvents.JOIN.register(WildfireEventHandler::clientJoin);
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(WildfireEventHandler::registerRenderLayers);
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				Identifier.fromNamespaceAndPath(WildfireGender.MODID, "player_list"),
				WildfireEventHandler::renderHud
		);
		ArmorStatsTooltipEvent.EVENT.register(WildfireEventHandler::renderTooltip);
		EntityHurtSoundEvent.EVENT.register(WildfireEventHandler::onEntityHurt);
		EntityTickEvent.EVENT.register(WildfireEventHandler::onEntityTick);
		PlayerNametagRenderEvent.EVENT.register(WildfireEventHandler::onPlayerNametag);
	}

	@Environment(EnvType.CLIENT)
	private static void onPlayerNametag(AvatarRenderState state, PoseStack matrixStack, Consumer<Component> renderHelper) {
		var genderRenderState = GenderRenderState.get(state);
		if(genderRenderState == null) return;

		@Nullable Component nametag = genderRenderState.nametag;
		if (nametag == null) return;

		matrixStack.pushPose();
		float translationAmt = switch(state.pose) {
			case Pose.CROUCHING -> 0.8f;
			case Pose.SLEEPING -> 0.125f;
			case Pose.SWIMMING, Pose.FALL_FLYING -> 0.3f;
			case Pose.SITTING -> 0.275f; //not tested; sitting on a pig doesn't work apparently.
			default -> 0.95f;
		};
		matrixStack.translate(0f, translationAmt, 0f);
		matrixStack.scale(0.5f, 0.5f, 0.5f);
		renderHelper.accept(nametag);
		matrixStack.popPose();
		// shift the rest of the name tag up a little bit
		matrixStack.translate(0f, 2.15F * 1.15F * 0.025F, 0f);
	}

	@Environment(EnvType.CLIENT)
	private static void renderTooltip(ItemStack item, Consumer<Component> tooltipAppender, @Nullable Player player) {
		if(player == null || !ClientConfig.INSTANCE.get(ClientConfig.ARMOR_STAT)) return;
		if(ClientConfig.INSTANCE.get(ClientConfig.ARMOR_PHYSICS_OVERRIDE)) return;

		var playerConfig = WildfireGender.getPlayerById(player.getUUID());
		if(playerConfig == null || !playerConfig.getGender().canHaveBreasts()) return;

		var equippableComponent = item.get(DataComponents.EQUIPPABLE);
		if(equippableComponent == null || equippableComponent.slot() != EquipmentSlot.CHEST) return;

		var config = WildfireHelper.getArmorConfig(item);
		// don't show a +0 tooltip on items that don't interact with physics (e.g. Elytra)
		if(!config.coversBreasts() || config.physicsResistance() == 0f) return;

		var formatted = WildfireHelper.toFormattedPercent(config.physicsResistance()) + "%";
		tooltipAppender.accept(Component.translatable("wildfire_gender.armor.tooltip", formatted).withStyle(ChatFormatting.LIGHT_PURPLE));
	}

	@Environment(EnvType.CLIENT)
	private static void renderHud(GuiGraphics context, DeltaTracker tickCounter) {
		var textRenderer = Objects.requireNonNull(Minecraft.getInstance().font, "textRenderer");
		if(Minecraft.getInstance().screen instanceof WardrobeBrowserScreen) {
			return;
		}

		if(ClientConfig.INSTANCE.get(ClientConfig.ALWAYS_SHOW_LIST).isVisible()) {
			SyncedPlayerList.drawSyncedPlayers(context, textRenderer);
		}
	}

	/**
	 * Attach breast render layers to players and armor stands
	 */
	@Environment(EnvType.CLIENT)
	private static void registerRenderLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?, ?> entityRenderer,
	                                         LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper,
	                                         EntityRendererProvider.Context context) {
		if(entityRenderer instanceof AvatarRenderer<?> playerRenderer) {
			registrationHelper.register(new GenderLayer<>(playerRenderer));
			registrationHelper.register(new GenderArmorLayer<>(playerRenderer, context.getEquipmentAssets(), context.getEquipmentRenderer()));
			registrationHelper.register(new HolidayFeaturesRenderer(playerRenderer));
		} else if(entityRenderer instanceof ArmorStandRenderer armorStandRenderer) {
			registrationHelper.register(new GenderArmorLayer<>(armorStandRenderer, context.getEquipmentAssets(), context.getEquipmentRenderer()));
		}
	}

	/**
	 * Remove (non-player) entities from the client cache when they're unloaded
	 */
	@Environment(EnvType.CLIENT)
	private static void onEntityUnload(Entity entity, Level world) {
		// note that we don't attempt to unload players; they're instead only ever unloaded once we leave a world,
		// or once they disconnect
		EntityConfig.CACHE.invalidate(entity.getUUID());
	}

	/**
	 * Perform various actions that should happen once per client tick, such as syncing client player settings
	 * to the server.
	 */
	@Environment(EnvType.CLIENT)
	private static void onClientTick(Minecraft client) {
		if(client.level == null || client.player == null) return;

		PlayerConfig clientConfig = WildfireGender.getPlayerById(client.player.getUUID());
		timer++;

		// Only attempt to sync if the server will accept the packet, and only once every 5 ticks, or around 4 times a second
		if(ServerboundSyncPacket.canSend() && timer % 5 == 0) {
			// sendToServer will only actually send a packet if any changes have been made that need to be synced,
			// or if we haven't synced before.
			if(clientConfig != null) WildfireSync.sendToServer(clientConfig);
		}

		if(timer % 40 == 0) {
			CloudSync.sendNextQueueBatch();
			if(clientConfig != null) clientConfig.attemptCloudSync();
		}

		if(TOGGLE_KEYBIND.consumeClick() && client.screen == null) {
			ClientConfig.RENDER_BREASTS ^= true;
		}
		if(CONFIG_KEYBIND.consumeClick() && client.screen == null) {
			WardrobeBrowserScreen.open(client, client.player);
		}
	}

	/**
	 * Clears all caches when the client player disconnects from a server/closes a singleplayer world
	 */
	@Environment(EnvType.CLIENT)
	private static void clientDisconnect(ClientPacketListener networkHandler, Minecraft client) {
		WildfireGender.CACHE.invalidateAll();
		EntityConfig.CACHE.invalidateAll();
	}

	@Environment(EnvType.CLIENT)
	private static void clientJoin(ClientPacketListener var1, PacketSender var2, Minecraft client) {
		if (client.player == null) return;

		if (ClientConfig.INSTANCE.get(ClientConfig.SHOW_TOAST)) {
			var button = WildfireEventHandler.CONFIG_KEYBIND.getTranslatedKeyMessage();
			ToastManager toastManager = client.getToastManager();
			toastManager.addToast(new WildfireToast(Minecraft.getInstance().font, Component.translatable("wildfire_gender.player_list.title"), Component.translatable("toast.wildfire_gender.get_started", button)));
		}
	}

	/**
	 * Removes a disconnecting player from the cache on a server
	 */
	private static void playerDisconnected(ServerGamePacketListenerImpl handler, MinecraftServer server) {
		WildfireGender.CACHE.invalidate(handler.getPlayer().getUUID());
	}

	/**
	 * Send a sync packet when a player enters the render distance of another player
	 */
	private static void onBeginTracking(Entity tracked, ServerPlayer syncTo) {
		if(tracked instanceof Player toSync) {
			PlayerConfig genderToSync = WildfireGender.getPlayerById(toSync.getUUID());
			if(genderToSync == null) return;
			// Note that we intentionally don't check if we've previously synced a player with this code path;
			// because we use entity tracking to sync, it's entirely possible that one player would leave the
			// tracking distance of another, change their settings, and then re-enter their tracking distance;
			// we wouldn't sync while they're out of tracking distance, and as such, their settings would be out
			// of sync until they relog.
			WildfireSync.sendToClient(syncTo, genderToSync);
		}
	}

	/**
	 * Play the relevant mod hurt sound when a player takes damage
	 */
	@Environment(EnvType.CLIENT)
	private static void onEntityHurt(LivingEntity entity, DamageSource damageSource) {
		Minecraft client = Minecraft.getInstance();
		if(client.player == null || client.level == null) return;
		if(!(entity instanceof Player player) || !player.level().isClientSide()) return;

		PlayerConfig genderPlayer = WildfireGender.getPlayerById(player.getUUID());
		if(genderPlayer == null || !genderPlayer.hasHurtSounds()) return;

		SoundEvent hurtSound = genderPlayer.getGender().getHurtSound();
		if(hurtSound != null) {
			float pitchVariation = (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F;
			player.playSound(hurtSound, 1f, pitchVariation + genderPlayer.getVoicePitch());
		}
	}

	/**
	 * Tick breast physics on entity tick
	 */
	@Environment(EnvType.CLIENT)
	private static void onEntityTick(LivingEntity entity) {
		if(EntityConfig.isSupportedEntity(entity)) {
			EntityConfig cfg = EntityConfig.getEntity(entity);
			if(entity instanceof ArmorStand) {
				cfg.readFromStack(entity.getItemBySlot(EquipmentSlot.CHEST));
			}
			cfg.tickBreastPhysics(entity);
		}
	}

	/**
	 * Apply player settings to chestplates equipped onto armor stands
	 */
	private static void onEquipArmorStand(Player player, ItemStack item) {
		PlayerConfig playerConfig = WildfireGender.getPlayerById(player.getUUID());
		if(playerConfig == null) {
			// while we shouldn't have our tag on the stack still, we're still checking to catch any armor
			// that may still have the tag from older versions, or from potential cross-mod interactions
			// which allow for removing items from armor stands without calling the vanilla
			// #equip and/or #onBreak methods
			BreastDataComponent.removeFromStack(item);
			return;
		}

		// Note that we always attach player data to the item stack as a server has no concept of resource packs,
		// making it impossible to compare against any armor data that isn't registered through the mod API.
		BreastDataComponent component = BreastDataComponent.fromPlayer(player, playerConfig);
		if(component != null) {
			component.write(item);
		}
	}
}

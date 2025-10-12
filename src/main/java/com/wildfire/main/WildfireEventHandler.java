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
import com.wildfire.render.*;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

public final class WildfireEventHandler {
	private WildfireEventHandler() {
		throw new UnsupportedOperationException();
	}

	private static final KeyBinding CONFIG_KEYBIND;
	private static final KeyBinding TOGGLE_KEYBIND;
	private static int timer = 0;

	public static KeyBinding getConfigKeybind() {
		return CONFIG_KEYBIND;
	}

	static {
		// note that all the Util.make()s are required, as otherwise a dedicated server will crash during
		// static class initialization due to references to classes that don't exist
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			var category = Util.make(() -> KeyBinding.Category.create(WildfireGender.id("generic")));
			CONFIG_KEYBIND = Util.make(() -> {
				KeyBinding keybind = new KeyBinding("key.wildfire_gender.gender_menu", GLFW.GLFW_KEY_H, category);
				KeyBindingHelper.registerKeyBinding(keybind);
				return keybind;
			});
			TOGGLE_KEYBIND = Util.make(() -> {
				KeyBinding keybind = new KeyBinding("key.wildfire_gender.toggle", GLFW.GLFW_KEY_UNKNOWN, category);
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
				Identifier.of(WildfireGender.MODID, "player_list"),
				WildfireEventHandler::renderHud
		);
		ArmorStatsTooltipEvent.EVENT.register(WildfireEventHandler::renderTooltip);
		EntityHurtSoundEvent.EVENT.register(WildfireEventHandler::onEntityHurt);
		EntityTickEvent.EVENT.register(WildfireEventHandler::onEntityTick);
		PlayerNametagRenderEvent.EVENT.register(WildfireEventHandler::onPlayerNametag);
	}

	@Environment(EnvType.CLIENT)
	private static void onPlayerNametag(PlayerEntityRenderState state, MatrixStack matrixStack, Consumer<Text> renderHelper) {
		var genderRenderState = GenderRenderState.get(state);
		if(genderRenderState == null) return;

		@Nullable Text nametag = genderRenderState.nametag;
		if (nametag == null) return;

		matrixStack.push();
		float translationAmt = switch(state.pose) {
			case EntityPose.CROUCHING -> 0.8f;
			case EntityPose.SLEEPING -> 0.125f;
			case EntityPose.SWIMMING, EntityPose.GLIDING -> 0.3f;
			case EntityPose.SITTING -> 0.275f; //not tested; sitting on a pig doesn't work apparently.
			default -> 0.95f;
		};
		matrixStack.translate(0f, translationAmt, 0f);
		matrixStack.scale(0.5f, 0.5f, 0.5f);
		renderHelper.accept(nametag);
		matrixStack.pop();
		// shift the rest of the name tag up a little bit
		matrixStack.translate(0f, 2.15F * 1.15F * 0.025F, 0f);
	}

	@Environment(EnvType.CLIENT)
	private static void renderTooltip(ItemStack item, Consumer<Text> tooltipAppender, @Nullable PlayerEntity player) {
		if(player == null || !ClientConfig.INSTANCE.get(ClientConfig.ARMOR_STAT)) return;
		if(ClientConfig.INSTANCE.get(ClientConfig.ARMOR_PHYSICS_OVERRIDE)) return;

		var playerConfig = WildfireGender.getPlayerById(player.getUuid());
		if(playerConfig == null || !playerConfig.getGender().canHaveBreasts()) return;

		var equippableComponent = item.get(DataComponentTypes.EQUIPPABLE);
		if(equippableComponent == null || equippableComponent.slot() != EquipmentSlot.CHEST) return;

		var config = WildfireHelper.getArmorConfig(item);
		// don't show a +0 tooltip on items that don't interact with physics (e.g. Elytra)
		if(!config.coversBreasts() || config.physicsResistance() == 0f) return;

		var formatted = WildfireHelper.toFormattedPercent(config.physicsResistance()) + "%";
		tooltipAppender.accept(Text.translatable("wildfire_gender.armor.tooltip", formatted).formatted(Formatting.LIGHT_PURPLE));
	}

	@Environment(EnvType.CLIENT)
	private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		var textRenderer = Objects.requireNonNull(MinecraftClient.getInstance().textRenderer, "textRenderer");
		if(MinecraftClient.getInstance().currentScreen instanceof WardrobeBrowserScreen) {
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
	                                         EntityRendererFactory.Context context) {
		if(entityRenderer instanceof PlayerEntityRenderer<?> playerRenderer) {
			registrationHelper.register(new GenderLayer<>(playerRenderer));
			registrationHelper.register(new GenderArmorLayer<>(playerRenderer, context.getEquipmentModelLoader(), context.getEquipmentRenderer()));
			registrationHelper.register(new HolidayFeaturesRenderer(playerRenderer));
		} else if(entityRenderer instanceof ArmorStandEntityRenderer armorStandRenderer) {
			registrationHelper.register(new GenderArmorLayer<>(armorStandRenderer, context.getEquipmentModelLoader(), context.getEquipmentRenderer()));
		}
	}

	/**
	 * Remove (non-player) entities from the client cache when they're unloaded
	 */
	@Environment(EnvType.CLIENT)
	private static void onEntityUnload(Entity entity, World world) {
		// note that we don't attempt to unload players; they're instead only ever unloaded once we leave a world,
		// or once they disconnect
		EntityConfig.CACHE.invalidate(entity.getUuid());
	}

	/**
	 * Perform various actions that should happen once per client tick, such as syncing client player settings
	 * to the server.
	 */
	@Environment(EnvType.CLIENT)
	private static void onClientTick(MinecraftClient client) {
		if(client.world == null || client.player == null) return;

		PlayerConfig clientConfig = WildfireGender.getPlayerById(client.player.getUuid());
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

		if(TOGGLE_KEYBIND.wasPressed() && client.currentScreen == null) {
			ClientConfig.RENDER_BREASTS ^= true;
		}
		if(CONFIG_KEYBIND.wasPressed() && client.currentScreen == null) {
			WardrobeBrowserScreen.open(client, client.player);
		}
	}

	/**
	 * Clears all caches when the client player disconnects from a server/closes a singleplayer world
	 */
	@Environment(EnvType.CLIENT)
	private static void clientDisconnect(ClientPlayNetworkHandler networkHandler, MinecraftClient client) {
		WildfireGender.CACHE.invalidateAll();
		EntityConfig.CACHE.invalidateAll();
	}

	@Environment(EnvType.CLIENT)
	private static void clientJoin(ClientPlayNetworkHandler var1, PacketSender var2, MinecraftClient client) {
		if (client.player == null) return;

		if (ClientConfig.INSTANCE.get(ClientConfig.SHOW_TOAST)) {
			var button = WildfireEventHandler.CONFIG_KEYBIND.getBoundKeyLocalizedText();
			ToastManager toastManager = client.getToastManager();
			toastManager.add(new WildfireToast(MinecraftClient.getInstance().textRenderer, Text.translatable("wildfire_gender.player_list.title"), Text.translatable("toast.wildfire_gender.get_started", button), false, 0));
		}
	}

	/**
	 * Removes a disconnecting player from the cache on a server
	 */
	private static void playerDisconnected(ServerPlayNetworkHandler handler, MinecraftServer server) {
		WildfireGender.CACHE.invalidate(handler.getPlayer().getUuid());
	}

	/**
	 * Send a sync packet when a player enters the render distance of another player
	 */
	private static void onBeginTracking(Entity tracked, ServerPlayerEntity syncTo) {
		if(tracked instanceof PlayerEntity toSync) {
			PlayerConfig genderToSync = WildfireGender.getPlayerById(toSync.getUuid());
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
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.player == null || client.world == null) return;
		if(!(entity instanceof PlayerEntity player) || !player.getEntityWorld().isClient()) return;

		PlayerConfig genderPlayer = WildfireGender.getPlayerById(player.getUuid());
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
			if(entity instanceof ArmorStandEntity) {
				cfg.readFromStack(entity.getEquippedStack(EquipmentSlot.CHEST));
			}
			cfg.tickBreastPhysics(entity);
		}
	}

	/**
	 * Apply player settings to chestplates equipped onto armor stands
	 */
	private static void onEquipArmorStand(PlayerEntity player, ItemStack item) {
		PlayerConfig playerConfig = WildfireGender.getPlayerById(player.getUuid());
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

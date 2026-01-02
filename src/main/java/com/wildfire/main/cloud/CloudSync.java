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

package com.wildfire.main.cloud;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.util.InstantTypeAdapter;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.entitydata.PlayerConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * <p>Utility class for managing syncing player data to/from the cloud, even if the current connected server doesn't
 * have the mod installed.</p>
 */
@Environment(EnvType.CLIENT)
public final class CloudSync {
	private CloudSync() {
		throw new UnsupportedOperationException();
	}

	private static Instant lastSync = Instant.EPOCH;
	private static final List<Instant> fetchErrors = new ArrayList<>();
	private static @Nullable Instant disableFetchingUntil;
	private static @Nullable CloudAuth auth;

	private static final Object AUTH_LOCK = new Object();
	private static final Object SYNC_LOCK = new Object();
	private static final Executor EXECUTOR = Util.ioPool().forName("wildfire_gender$cloudSync");
	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantTypeAdapter()).create();

	private static final HttpClient CLIENT = Util.make(() -> {
		var builder = HttpClient.newBuilder();
		// Use HTTP/1.1 in a development environment if the sync server is not running over https; this is because
		// HttpClient's default of HTTP/2.0 causes issues when making a PUT request to a FastAPI server running
		// over an unencrypted HTTP connection.
		if(FabricLoader.getInstance().isDevelopmentEnvironment() && getCloudServer().startsWith("http://")) {
			builder.version(HttpClient.Version.HTTP_1_1);
		}
		builder.connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL);
		return builder.build();
	});

	private static final String USER_AGENT =
			"WildfireGender/" + StringUtils.split(WildfireHelper.getModVersion(WildfireGender.MODID), '+')[0]
					+ " Minecraft/" + WildfireHelper.getModVersion("minecraft");

	private static final Queue<QueuedFetch> QUEUED = new ConcurrentLinkedDeque<>();
	private static final Cache<UUID, Optional<JsonObject>> FETCH_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(10)).concurrencyLevel(6).build();

	private static final String DEFAULT_CLOUD_URL = "https://wfgm.celestialfault.dev";
	private static final Duration SYNC_COOLDOWN = Duration.ofSeconds(10);

	/**
	 * @return {@code true} if the last {@link #sync(PlayerConfig) sync} was within the last 10 seconds
	 */
	public static boolean syncOnCooldown() {
		return lastSync.plus(SYNC_COOLDOWN).isAfter(Instant.now());
	}

	/**
	 * @return A {@link SyncUnavailable} enum indicating the reason for syncing being unavailable, or {@code null} if available
	 */
	public static @Nullable SyncUnavailable unavailableReason() {
		var sessionUuid = Minecraft.getInstance().getUser().getProfileId();
		// offline mode sessions use a version 3 UUID
		if(sessionUuid.version() != 4) {
			return SyncUnavailable.INVALID_ACCOUNT;
		}

		if(CloudUtils.hasTheSessionServiceBeenTamperedWith()) {
			return SyncUnavailable.INVALID_ACCOUNT;
		}

		var client = Minecraft.getInstance();
		var netHandler = client.getConnection();
		if(!client.isLocalServer() && netHandler != null && !netHandler.getConnection().isEncrypted()) {
			return SyncUnavailable.OFFLINE_SERVER;
		}
		return null;
	}

	/**
	 * @return {@code true} if syncing is available; this method is shorthand for {@code unavailableReason() == null}.
	 */
	public static boolean isAvailable() {
		return unavailableReason() == null;
	}

	/**
	 * @return {@code true} if syncing is enabled; this will always return {@code false} if {@link #isAvailable() syncing is unavailable}.
	 */
	public static boolean isEnabled() {
		return isAvailable() && ClientConfig.INSTANCE.get(ClientConfig.CLOUD_SYNC_ENABLED);
	}

	/**
	 * @return The URL of the sync server currently being used
	 */
	public static String getCloudServer() {
		var url = ClientConfig.INSTANCE.get(ClientConfig.CLOUD_SERVER);
		return url.isBlank() ? DEFAULT_CLOUD_URL : url;
	}

	private static void markFetchError() {
		fetchErrors.add(Instant.now());
		fetchErrors.removeIf(e -> e.plus(30, ChronoUnit.SECONDS).isBefore(Instant.now()));
		if(fetchErrors.size() >= 5) {
			WildfireGender.LOGGER.error("Too many recent sync errors, disabling future lookups for 5 minutes");
			disableFetchingUntil = Instant.now().plus(5, ChronoUnit.MINUTES);
		}
	}

	private static boolean isFetchingDisabled(boolean ignoreConfig) {
		if(!isAvailable()) return true;
		if(!ignoreConfig && !isEnabled()) return true;
		return disableFetchingUntil != null && disableFetchingUntil.isAfter(Instant.now());
	}

	private static HttpRequest.Builder createRequest(URI uri) {
		WildfireGender.LOGGER.debug("Connecting to {}", uri);
		return HttpRequest.newBuilder(uri)
				.header("User-Agent", USER_AGENT)
				.header("Accept", "application/json")
				.timeout(Duration.ofSeconds(5));
	}

	@ApiStatus.Internal
	public static CompletableFuture<Map<UUID, Contributor>> getContributors() {
		return CompletableFuture.supplyAsync(() -> {
			var request = createRequest(URI.create(getCloudServer() + "/contributors")).GET().build();

			HttpResponse<String> response;
			try {
				response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
				if(response.statusCode() != 200) {
					WildfireGender.LOGGER.warn("Couldn't fetch contributor list: server responded {}", response.statusCode());
					return Map.of();
				}
			} catch(Exception e) {
				WildfireGender.LOGGER.warn("Couldn't fetch contributor list", e);
				return Map.of();
			}

			try {
				var json = GSON.fromJson(response.body(), JsonObject.class);
				var interim = json.asMap()
						.entrySet()
						.stream()
						.collect(Collectors.toMap(
								entry -> UUID.fromString(entry.getKey()),
								entry -> GSON.fromJson(entry.getValue(), Contributor.class)
						));
				return Collections.unmodifiableMap(interim.size() <= 8 ? new Object2ObjectArrayMap<>(interim) : new Object2ObjectOpenHashMap<>(interim));
			} catch(Exception e) {
				WildfireGender.LOGGER.error("Failed to parse contributor list", e);
				return Map.of();
			}
		}, EXECUTOR);
	}

	private static String generateServerId() {
		// https://github.com/hibiii/Kappa/blob/main/src/main/java/hibiii/kappa/Provider.java#L40-L42
		BigInteger intA = new BigInteger(128, new Random());
		BigInteger intB = new BigInteger(128, new Random(System.identityHashCode(new Object())));
		return intA.xor(intB).toString(16);
	}

	@Blocking
	private static String getAuthToken() {
		synchronized(AUTH_LOCK) {
			var client = Minecraft.getInstance();
			if(client.player == null) {
				SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATION_FAILED);
				throw new IllegalStateException("Cannot get a new auth token while the client player is unset");
			}
			if(auth == null || auth.isExpired() || auth.isInvalidForClientPlayer()) {
				WildfireGender.LOGGER.info("Authenticating with Mojang session servers");
				SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATING_MOJANG);

				var serverId = generateServerId();
				var session = client.getUser();

				try {
					CloudUtils.getSessionService().joinServer(Objects.requireNonNull(session.getProfileId()), session.getAccessToken(), serverId);
				} catch(AuthenticationException e) {
					SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATION_FAILED);
					throw new RuntimeException(e);
				}

				WildfireGender.LOGGER.info("Obtaining new authentication token from the cloud sync server");
				SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATING_CLOUD_SYNC);
				var query = HttpAuthenticationService.buildQuery(Map.of("serverId", serverId, "username", session.getName()));
				var uri = URI.create(getCloudServer() + "/auth?" + query);
				var request = createRequest(uri).GET().build();
				var response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
				if(response.statusCode() >= 400) {
					SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATION_FAILED);
					throw new RuntimeException("Failed to authenticate with sync server: " + response.body());
				}

				auth = GSON.fromJson(response.body(), CloudAuth.class);
				if(auth.isInvalidForClientPlayer()) {
					WildfireGender.LOGGER.warn("Authenticated account {} does not match the current player ({}); you likely have a misbehaving account switcher mod installed!", auth.account(), client.player.getUUID());
				}
				WildfireGender.LOGGER.info("Obtained authentication token for {}, expiry {}", auth.account(), auth.expires());
			}
		}
		return auth.token();
	}

	/**
	 * Send the client player config to the cloud sync server for syncing to other players
	 *
	 * @param config The config of the client player
	 *
	 * @return A {@link CompletableFuture} indicating when the process has finished, or with an exception if
	 *         syncing failed.
	 */
	public static CompletableFuture<Void> sync(PlayerConfig config) {
		if(!isEnabled()) {
			return CompletableFuture.completedFuture(null);
		}

		synchronized(SYNC_LOCK) {
			// Force a 10s cooldown on syncing
			if(syncOnCooldown()) {
				var future = new CompletableFuture<Void>();
				future.completeExceptionally(new SyncingTooFrequentlyException());
				return future;
			}
			lastSync = Instant.now();
		}

		return syncInternal(config, false);
	}

	private static CompletableFuture<Void> syncInternal(PlayerConfig config, boolean resyncing) {
		return CompletableFuture.runAsync(() -> {
			var token = getAuthToken();
			var url = URI.create(getCloudServer() + "/" + config.uuid);
			var json = config.toJson().toString();

			SyncLog.add(WildfireLocalization.SYNC_LOG_ATTEMPTING_SYNC);

			var request = createRequest(url)
					.PUT(HttpRequest.BodyPublishers.ofString(json))
					.header("Content-Type", "application/json; charset=UTF-8")
					.header("Auth-Token", token)
					.build();
			var response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
			if(response.statusCode() == 401 && !resyncing) {
				SyncLog.add(WildfireLocalization.SYNC_LOG_REAUTHENTICATING);
				WildfireGender.LOGGER.warn("Auth token is invalid, attempting to reauth...");
				auth = null;
				syncInternal(config, true).join();
				return;
			} else if(response.statusCode() >= 400) {
				throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
			}
			WildfireGender.LOGGER.debug("Server responded to update: {}", response.body());
			SyncLog.add(WildfireLocalization.SYNC_LOG_SYNC_SUCCESS);
		}, EXECUTOR);
	}

	/**
	 * Request that the cloud sync server delete the data stored for the provided client player.
	 *
	 * @param config The config of the client player
	 *
	 * @return A {@link CompletableFuture} indicating when the process has finished, or with an exception if
	 *         the request failed.
	 */
	public static CompletableFuture<Void> deleteProfile(PlayerConfig config) {
		return CompletableFuture.runAsync(() -> {
			var token = getAuthToken();
			var url = URI.create(getCloudServer() + "/" + config.uuid);
			var request = createRequest(url).DELETE().header("Auth-Token", token).build();
			var response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
			if(response.statusCode() == 404) {
				SyncLog.add(WildfireLocalization.SYNC_LOG_NO_PROFILE_TO_DELETE);
				return;
			} else if(response.statusCode() >= 400) {
				SyncLog.add(WildfireLocalization.SYNC_LOG_DELETION_FAILED);
				throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
			}
			WildfireGender.LOGGER.debug("Deleted cloud sync profile");
			SyncLog.add(WildfireLocalization.SYNC_LOG_DELETED);
		}, EXECUTOR);
	}

	/**
	 * @see #getProfile(UUID, boolean)
	 */
	public static CompletableFuture<@Nullable JsonObject> getProfile(UUID uuid) {
		return getProfile(uuid, false);
	}

	/**
	 * Fetch player data from the sync server
	 *
	 * @param uuid The UUID of the player to get data for
	 *
	 * @return A {@link CompletableFuture} containing a {@link JsonObject} of the player's data if they have any data
	 *         stored in the sync server, or {@code null} otherwise.
	 *
	 * @apiNote The provided UUID <b>must</b> be {@link UUID#version() version 4}, otherwise the request will fail.
	 *
	 * @see #queueFetch(UUID)
	 */
	public static CompletableFuture<@Nullable JsonObject> getProfile(UUID uuid, boolean ignoreConfig) {
		if(isFetchingDisabled(ignoreConfig)) {
			return CompletableFuture.completedFuture(null);
		}
		if(uuid.version() != 4) {
			// some servers (namely hypixel) use non-v4 uuids for their npcs; the sync server will immediately reject
			// such uuids with a 422 response, so don't bother trying to fetch them.
			return CompletableFuture.completedFuture(null);
		}
		var cached = FETCH_CACHE.getIfPresent(uuid);
		//noinspection OptionalAssignedToNull
		if(cached != null) {
			return CompletableFuture.completedFuture(cached.orElse(null));
		}

		return CompletableFuture.supplyAsync(() -> {
			var url = URI.create(getCloudServer() + "/" + uuid);
			var request = createRequest(url).GET().build();
			var response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
			if(response.statusCode() == 404 || response.statusCode() == 204) {
				WildfireGender.LOGGER.debug("Server replied no data for {}", uuid);
				FETCH_CACHE.put(uuid, Optional.empty());
				return null;
			} else if(response.statusCode() >= 400) {
				markFetchError();
				throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
			}

			SyncLog.add(WildfireLocalization.SYNC_LOG_GET_SINGLE_PROFILE, SyncVerbosity.SHOW_FETCHES);

			var data = GSON.fromJson(response.body(), JsonObject.class);
			FETCH_CACHE.put(uuid, Optional.of(data));
			return data;
		}, EXECUTOR);
	}

	/**
	 * Fetch data for multiple players from the sync server.
	 *
	 * @param uuids A collection of between 2 and 20 UUIDs to fetch player data for
	 *
	 * @return A {@link CompletableFuture} containing a map of player UUIDs to their synced data; any provided
	 *         player UUIDs without any sync data will not be included in the returned map.
	 *
	 * @apiNote All UUIDs <b>must</b> be {@link UUID#version() version 4}, otherwise the request will fail.
	 *
	 * @see #queueFetch(UUID)
	 */
	public static CompletableFuture<Map<UUID, JsonObject>> getMultiple(Collection<UUID> uuids) {
		return CompletableFuture.supplyAsync(() -> {
			if(isFetchingDisabled(false)) {
				return Collections.emptyMap();
			}

			var url = URI.create(getCloudServer() + "/");
			var json = new JsonArray();
			uuids.forEach(uuid -> json.add(uuid.toString()));
			var request = createRequest(url)
					.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
					.header("Content-Type", "application/json; charset=UTF-8")
					.build();
			var response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
			if(response.statusCode() >= 400) {
				markFetchError();
				throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
			}

			SyncLog.add(WildfireLocalization.SYNC_LOG_GET_MULTIPLE_PROFILES, SyncVerbosity.SHOW_FETCHES);
			var data = GSON.fromJson(response.body(), BulkFetch.class).users();
			uuids.forEach(uuid -> FETCH_CACHE.put(uuid, Optional.ofNullable(data.get(uuid))));
			return data;
		}, EXECUTOR);
	}

	/**
	 * <p>Add a UUID to the fetch queue; this may be requested individually or in bulk, depending on how many other
	 * users are queued to be fetched.</p>
	 *
	 * <p>Queued queries are currently sent once every 2 seconds (or every 40th tick) in batches of up to 20 at once.</p>
	 *
	 * @param uuid The UUID of the user to fetch
	 *
	 * @return A {@link CompletableFuture} containing a {@link JsonObject} of the relevant player data,
	 *         which will be completed once the next queued batch is finished.
	 */
	public static CompletableFuture<@Nullable JsonObject> queueFetch(UUID uuid) {
		if(!isEnabled()) {
			return CompletableFuture.completedFuture(null);
		}

		var cached = FETCH_CACHE.getIfPresent(uuid);
		//noinspection OptionalAssignedToNull
		if(cached != null) {
			return CompletableFuture.completedFuture(cached.orElse(null));
		}
		if(uuid.version() != 4) {
			// some servers (namely hypixel) use non-v4 uuids for their npcs; the sync server will immediately reject
			// such uuids with a 422 response, so don't bother trying to fetch them.
			return CompletableFuture.completedFuture(null);
		}

		var future = new CompletableFuture<@Nullable JsonObject>();
		QUEUED.add(new QueuedFetch(uuid, future));
		return future;
	}

	/**
	 * Sends up to 20 {@link #queueFetch(UUID) queued sync queries}
	 *
	 * @apiNote This method is not intended to be used by other mods.
	 */
	@ApiStatus.Internal
	public static void sendNextQueueBatch() {
		if(QUEUED.isEmpty()) {
			return;
		}

		final var toFetch = new ArrayList<QueuedFetch>();
		for(int i = 0; i < 20; i++) {
			var next = QUEUED.poll();
			if(next == null) break;
			toFetch.add(next);
		}

		// If there's 3 or fewer players in the queue, just send them all individually so that the requests
		// can be cached easier
		if(toFetch.size() < 4) {
			WildfireGender.LOGGER.debug("Sending {} queued sync queries", toFetch.size());
			toFetch.forEach(queued -> CompletableFuture.runAsync(() -> {
				try {
					queued.future().complete(getProfile(queued.uuid()).join());
				} catch(Exception e) {
					var actualException = e instanceof CompletionException ce ? ce.getCause() : e;
					queued.future().completeExceptionally(actualException);
				}
			}));
			return;
		}

		WildfireGender.LOGGER.debug("Fetching sync data for {} players in bulk", toFetch.size());
		CompletableFuture.runAsync(() -> {
			Map<UUID, JsonObject> result;
			try {
				result = getMultiple(toFetch.stream().map(QueuedFetch::uuid).toList()).join();
			} catch(Exception e) {
				var actualException = e instanceof CompletionException ce ? ce.getCause() : e;
				toFetch.forEach(queued -> queued.future().completeExceptionally(actualException));
				return;
			}

			toFetch.forEach(queued -> queued.future().complete(result.get(queued.uuid())));
		});
	}
}

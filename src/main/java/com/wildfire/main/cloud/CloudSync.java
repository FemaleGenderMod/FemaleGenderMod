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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.util.InstantTypeAdapter;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.entitydata.PlayerConfig;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Utility class for managing syncing player data to/from the cloud, even if the current connected server doesn't
 * have the mod installed.</p>
 */
public final class CloudSync {

    private CloudSync() {
        throw new UnsupportedOperationException();
    }

    private static Instant lastSync = Instant.EPOCH;
    private static final List<Instant> fetchErrors = new ArrayList<>();
    @Nullable
    private static Instant disableFetchingUntil;
    private static CloudAuth auth;

    private static final Object AUTH_LOCK = new Object();
    private static final Object SYNC_LOCK = new Object();
    //TODO - 1.21.4: Give this a name
    private static final Executor EXECUTOR = Util.ioPool();//.forName("wildfire_gender$cloudSync");
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantTypeAdapter()).create();
    //Note: HttpClient will seemingly _completely_ break PUT requests if allowed to use its default of HTTP/2 with
    // a sync server not running over https; this should realistically only ever be an issue you'd encounter when
    // running the sync server locally to develop on it, which is why this enforces that you're in a dev env to
    // allow using HTTP/1.1.
    private static final boolean USE_HTTP_1_1 = !FMLEnvironment.production && Boolean.parseBoolean(System.getProperties().getProperty("wildfire_gender.force_http"));
    private static final HttpClient CLIENT = HttpClient.newBuilder()
          .version(USE_HTTP_1_1 ? HttpClient.Version.HTTP_1_1 : HttpClient.Version.HTTP_2)
          .connectTimeout(Duration.ofSeconds(5))
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

    private static final String USER_AGENT = "WildfireGender/" + getVersion(WildfireGender.INSTANCE.container) +
                                             " NeoForge/" + NeoForgeVersion.getVersion() +
                                             " Minecraft/" + FMLLoader.versionInfo().mcVersion();

    private static String getVersion(ModContainer modContainer) {
        ArtifactVersion version = modContainer.getModInfo().getVersion();
        return version.getMajorVersion() + "." + version.getMinorVersion() + "." + version.getIncrementalVersion();
    }

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
        if (Minecraft.getInstance().getUser().getType() != User.Type.MSA) {
            return SyncUnavailable.INVALID_ACCOUNT;
        }
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener netHandler = client.getConnection();
        if (!client.isSingleplayer() && netHandler != null && !netHandler.getConnection().isEncrypted()) {
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
        return isAvailable() && GeneralClientConfig.INSTANCE.cloudSync.get();
    }

    /**
     * @return The URL of the sync server currently being used
     */
    public static String getCloudServer() {
        String url = GeneralClientConfig.INSTANCE.cloudServer.get();
        return url.isBlank() ? DEFAULT_CLOUD_URL : url;
    }

    private static void markFetchError() {
        fetchErrors.add(Instant.now());
        fetchErrors.removeIf(e -> e.plus(30, ChronoUnit.SECONDS).isBefore(Instant.now()));
        if (fetchErrors.size() >= 5) {
            WildfireGender.LOGGER.error("Too many recent sync errors, disabling future lookups for 5 minutes");
            disableFetchingUntil = Instant.now().plus(5, ChronoUnit.MINUTES);
        }
    }

    private static boolean isFetchingDisabled() {
        return !isEnabled() || disableFetchingUntil != null && disableFetchingUntil.isAfter(Instant.now());
    }

    private static HttpRequest.Builder createRequest(URI uri) {
        WildfireGender.LOGGER.debug("Connecting to {}", uri);
        return HttpRequest.newBuilder(uri)
              .header("User-Agent", USER_AGENT)
              .header("Accept", "application/json")
              .timeout(Duration.ofSeconds(5));
    }

    public static CompletableFuture<Map<UUID, ContributorNametag>> getContributors() {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequest request = createRequest(URI.create(getCloudServer() + "/contributors")).GET().build();
            HttpResponse<String> response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                WildfireGender.LOGGER.warn("Couldn't fetch contributor nametags: server responded {}", response.statusCode());
                return Collections.emptyMap();
            }

            try {
                JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                Map<UUID, ContributorNametag> map = new HashMap<>();
                for (Entry<String, JsonElement> entry : json.asMap().entrySet()) {
                    map.put(UUID.fromString(entry.getKey()), GSON.fromJson(entry.getValue(), ContributorNametag.class));
                }
                return Collections.unmodifiableMap(map);
            } catch (Exception e) {
                WildfireGender.LOGGER.error("Failed to parse contributor list", e);
                return Collections.emptyMap();
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
        synchronized (AUTH_LOCK) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATION_FAILED);
                throw new IllegalStateException("Cannot get a new auth token while the client player is unset");
            }
            if (auth == null || auth.isExpired() || auth.isInvalidForClientPlayer()) {
                WildfireGender.LOGGER.info("Obtaining new authentication token from the cloud sync server");
                SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATING);

                String serverId = generateServerId();
                User session = client.getUser();

                try {
                    client.getMinecraftSessionService().joinServer(Objects.requireNonNull(session.getProfileId()), session.getAccessToken(), serverId);
                } catch (AuthenticationException e) {
                    throw new RuntimeException(e);
                }

                String query = HttpAuthenticationService.buildQuery(Map.of("serverId", serverId, "username", session.getName()));
                URI uri = URI.create(getCloudServer() + "/auth?" + query);
                HttpRequest request = createRequest(uri).GET().build();
                HttpResponse<String> response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
                if (response.statusCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATION_FAILED);
                    throw new RuntimeException("Failed to authenticate with sync server: " + response.body());
                }

                auth = GSON.fromJson(response.body(), CloudAuth.class);
                if (auth.isInvalidForClientPlayer()) {
                    WildfireGender.LOGGER.warn("Authenticated account {} does not match the current player ({}); you likely have a misbehaving account switcher mod installed!", auth.account(), client.player.getUUID());
                }
                WildfireGender.LOGGER.info("Obtained authentication token for {}, expiry {}", auth.account(), auth.expires());
                if (!auth.isInvalidForClientPlayer()) { //TODO: This might not need to be here.
                    SyncLog.add(WildfireLocalization.SYNC_LOG_AUTHENTICATION_SUCCESS);
                }
            }
        }
        return auth.token();
    }

    /**
     * Send the client player config to the cloud sync server for syncing to other players
     *
     * @param config The config of the client player
     *
     * @return A {@link CompletableFuture} indicating when the process has finished, or with an exception if syncing failed.
     */
    public static CompletableFuture<Void> sync(@NotNull PlayerConfig config) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        synchronized (SYNC_LOCK) {
            // Force a 10s cooldown on syncing
            if (syncOnCooldown()) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(new SyncingTooFrequentlyException());
                return future;
            }
            lastSync = Instant.now();
        }

        return syncInternal(config, false);
    }

    private static CompletableFuture<Void> syncInternal(PlayerConfig config, boolean resyncing) {
        return CompletableFuture.runAsync(() -> {
            String token = getAuthToken();
            URI url = URI.create(getCloudServer() + "/" + config.uuid);
            String json = config.toJson().toString();

            SyncLog.add(WildfireLocalization.SYNC_LOG_ATTEMPTING_SYNC);

            HttpRequest request = createRequest(url)
                  .PUT(HttpRequest.BodyPublishers.ofString(json))
                  .header("Content-Type", "application/json; charset=UTF-8")
                  .header("Auth-Token", token)
                  .build();
            HttpResponse<String> response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
            if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED && !resyncing) {
                SyncLog.add(WildfireLocalization.SYNC_LOG_REAUTHENTICATING);
                WildfireGender.LOGGER.warn("Auth token is invalid, attempting to reauth...");
                auth = null;
                syncInternal(config, true).join();
                return;
            } else if (response.statusCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
            }
            WildfireGender.LOGGER.debug("Server responded to update: {}", response.body());
            SyncLog.add(WildfireLocalization.SYNC_LOG_SYNC_SUCCESS);
        }, EXECUTOR);
    }

    /**
     * Fetch player data from the sync server
     *
     * @param uuid The UUID of the player to get data for
     *
     * @return A {@link CompletableFuture} containing a {@link JsonObject} of the player's data if they have any data stored in the sync server, or {@code null}
     * otherwise.
     *
     * @apiNote The provided UUID <b>must</b> be {@link UUID#version() version 4}, otherwise the request will fail.
     * @see #queueFetch(UUID)
     */
    public static CompletableFuture<@Nullable JsonObject> getProfile(UUID uuid) {
        if (isFetchingDisabled()) {
            return CompletableFuture.completedFuture(null);
        }
        if (uuid.version() != 4) {
            // some servers (namely hypixel) use non-v4 uuids for their npcs; the sync server will immediately reject
            // such uuids with a 422 response, so don't bother trying to fetch them.
            return CompletableFuture.completedFuture(null);
        }
        Optional<JsonObject> cached = FETCH_CACHE.getIfPresent(uuid);
        //noinspection OptionalAssignedToNull
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.orElse(null));
        }

        return CompletableFuture.supplyAsync(() -> {
            URI url = URI.create(getCloudServer() + "/" + uuid);
            HttpRequest request = createRequest(url).GET().build();
            HttpResponse<String> response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
            if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND || response.statusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                WildfireGender.LOGGER.debug("Server replied no data for {}", uuid);
                FETCH_CACHE.put(uuid, Optional.empty());
                return null;
            } else if (response.statusCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                markFetchError();
                throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
            }

            SyncLog.add(WildfireLocalization.SYNC_LOG_GET_SINGLE_PROFILE, SyncVerbosity.SHOW_FETCHES);

            JsonObject data = GSON.fromJson(response.body(), JsonObject.class);
            FETCH_CACHE.put(uuid, Optional.of(data));
            return data;
        }, EXECUTOR);
    }

    /**
     * Fetch data for multiple players from the sync server.
     *
     * @param uuids A collection of between 2 and 20 UUIDs to fetch player data for
     *
     * @return A {@link CompletableFuture} containing a map of player UUIDs to their synced data; any provided player UUIDs without any sync data will not be included in
     * the returned map.
     *
     * @apiNote All UUIDs <b>must</b> be {@link UUID#version() version 4}, otherwise the request will fail.
     * @see #queueFetch(UUID)
     */
    public static CompletableFuture<Map<UUID, JsonObject>> getMultiple(Collection<UUID> uuids) {
        return CompletableFuture.supplyAsync(() -> {
            if (isFetchingDisabled()) {
                return Collections.emptyMap();
            }

            URI url = URI.create(getCloudServer() + "/");
            JsonArray json = new JsonArray();
            uuids.forEach(uuid -> json.add(uuid.toString()));
            HttpRequest request = createRequest(url)
                  .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                  .header("Content-Type", "application/json; charset=UTF-8")
                  .build();
            HttpResponse<String> response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
            if (response.statusCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                markFetchError();
                throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
            }

            SyncLog.add(WildfireLocalization.SYNC_LOG_GET_MULTIPLE_PROFILES, SyncVerbosity.SHOW_FETCHES);
            Map<UUID, JsonObject> data = GSON.fromJson(response.body(), BulkFetch.class).users();
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
     * @return A {@link CompletableFuture} containing a {@link JsonObject} of the relevant player data, which will be completed once the next queued batch is finished.
     */
    public static CompletableFuture<@Nullable JsonObject> queueFetch(UUID uuid) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        Optional<JsonObject> cached = FETCH_CACHE.getIfPresent(uuid);
        //noinspection OptionalAssignedToNull
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.orElse(null));
        }
        if (uuid.version() != 4) {
            // some servers (namely hypixel) use non-v4 uuids for their npcs; the sync server will immediately reject
            // such uuids with a 422 response, so don't bother trying to fetch them.
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<@Nullable JsonObject> future = new CompletableFuture<>();
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
        if (QUEUED.isEmpty()) {
            return;
        }

        final List<QueuedFetch> toFetch = new ArrayList<QueuedFetch>();
        for (int i = 0; i < 20; i++) {
            QueuedFetch next = QUEUED.poll();
            if (next == null) {
                break;
            }
            toFetch.add(next);
        }

        // If there's 3 or fewer players in the queue, just send them all individually so that the requests
        // can be cached easier
        if (toFetch.size() < 4) {
            WildfireGender.LOGGER.debug("Sending {} queued sync queries", toFetch.size());
            toFetch.forEach(queued -> CompletableFuture.runAsync(() -> {
                try {
                    queued.future().complete(getProfile(queued.uuid()).join());
                } catch (Exception e) {
                    Throwable actualException = e instanceof CompletionException ce ? ce.getCause() : e;
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
            } catch (Exception e) {
                Throwable actualException = e instanceof CompletionException ce ? ce.getCause() : e;
                toFetch.forEach(queued -> queued.future().completeExceptionally(actualException));
                return;
            }

            toFetch.forEach(queued -> queued.future().complete(result.get(queued.uuid())));
        });
    }
}

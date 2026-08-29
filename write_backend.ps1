$noBom = New-Object System.Text.UTF8Encoding($false)

$trackMapper = @"
package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AudiusTrackMapper {

    public TrackDto mapTrack(JsonNode node, String streamUrl) {
        if (node == null || node.isNull()) {
            return null;
        }

        String id = node.path("id").asText("");
        if (id.isBlank()) {
            id = String.valueOf(node.path("track_id").asLong(0));
        }

        String title = node.path("title").asText("Untitled Track").trim();
        String genre = node.path("genre").asText("Music");
        long durationSec = node.path("duration").asLong(180);
        long durationMs = durationSec * 1000L;
        long playCount = node.path("play_count").asLong(0);
        String playsCountFormatted = playCount > 0 ? formatPlays(playCount) : "10K+";

        JsonNode userNode = node.path("user");
        String artist = "Audius Artist";
        if (userNode != null && !userNode.isNull()) {
            String name = userNode.path("name").asText("").trim();
            if (!name.isBlank()) {
                artist = name;
            } else {
                artist = userNode.path("handle").asText("Audius Artist");
            }
        }

        String coverUrl = "";
        JsonNode artwork = node.path("artwork");
        if (artwork != null && !artwork.isNull()) {
            if (artwork.hasNonNull("480x480")) {
                coverUrl = artwork.path("480x480").asText("");
            } else if (artwork.hasNonNull("1000x1000")) {
                coverUrl = artwork.path("1000x1000").asText("");
            } else if (artwork.hasNonNull("150x150")) {
                coverUrl = artwork.path("150x150").asText("");
            }
        }

        if (coverUrl.isBlank()) {
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500";
        }

        String effectiveAudioUrl = (streamUrl != null && !streamUrl.isBlank())
                ? streamUrl
                : "https://discoveryprovider.audius.co/v1/tracks/" + id + "/stream?app_name=TuneFlow";

        boolean isStreamable = node.path("is_streamable").asBoolean(true);

        return new TrackDto(
                id,
                title,
                artist,
                genre,
                durationMs,
                effectiveAudioUrl,
                coverUrl,
                playsCountFormatted,
                false,
                "audius",
                id,
                null,
                effectiveAudioUrl,
                isStreamable,
                "AUDIO",
                artist,
                true
        );
    }

    public List<TrackDto> mapTracks(JsonNode dataNode, AudiusClient audiusClient) {
        List<TrackDto> list = new ArrayList<>();
        if (dataNode == null || !dataNode.isArray()) {
            return list;
        }

        for (JsonNode item : dataNode) {
            String id = item.path("id").asText("");
            if (id.isBlank()) {
                id = String.valueOf(item.path("track_id").asLong(0));
            }
            String streamUrl = audiusClient.resolveStreamUrl(id);
            TrackDto dto = mapTrack(item, streamUrl);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    private String formatPlays(long plays) {
        if (plays >= 1_000_000) {
            return String.format("%.1fM", plays / 1_000_000.0);
        } else if (plays >= 1_000) {
            return String.format("%.1fK", plays / 1_000.0);
        }
        return String.valueOf(plays);
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\service\audius\AudiusTrackMapper.java", $trackMapper, $noBom)

$artistMapper = @"
package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.ArtistDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AudiusArtistMapper {

    public ArtistDto mapArtist(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        String id = node.path("id").asText("");
        if (id.isBlank()) {
            id = String.valueOf(node.path("user_id").asLong(0));
        }

        String name = node.path("name").asText("").trim();
        if (name.isBlank()) {
            name = node.path("handle").asText("Audius Artist");
        }

        String bio = node.path("bio").asText("Official TuneFlow Creator");
        boolean verified = node.path("is_verified").asBoolean(false);
        int followersCount = node.path("follower_count").asInt(0);

        String imageUrl = "";
        JsonNode profilePic = node.path("profile_picture");
        if (profilePic != null && !profilePic.isNull()) {
            if (profilePic.hasNonNull("480x480")) {
                imageUrl = profilePic.path("480x480").asText("");
            } else if (profilePic.hasNonNull("1000x1000")) {
                imageUrl = profilePic.path("1000x1000").asText("");
            } else if (profilePic.hasNonNull("150x150")) {
                imageUrl = profilePic.path("150x150").asText("");
            }
        }

        if (imageUrl.isBlank()) {
            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500";
        }

        return new ArtistDto(
                id,
                name,
                "Audius Verified",
                bio,
                imageUrl,
                "#7C3AED",
                "#EC4899",
                followersCount,
                verified
        );
    }

    public List<ArtistDto> mapArtists(JsonNode dataNode) {
        List<ArtistDto> list = new ArrayList<>();
        if (dataNode == null || !dataNode.isArray()) {
            return list;
        }

        for (JsonNode item : dataNode) {
            ArtistDto dto = mapArtist(item);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\service\audius\AudiusArtistMapper.java", $artistMapper, $noBom)

$playlistMapper = @"
package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.PlaylistDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AudiusPlaylistMapper {

    public PlaylistDto mapPlaylist(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        String id = node.path("id").asText("");
        if (id.isBlank()) {
            id = String.valueOf(node.path("playlist_id").asLong(0));
        }

        String title = node.path("playlist_name").asText("Trending Playlist").trim();
        String description = node.path("description").asText("Curated with TuneFlow").trim();

        String coverUrl = "";
        JsonNode artwork = node.path("artwork");
        if (artwork != null && !artwork.isNull()) {
            if (artwork.hasNonNull("480x480")) {
                coverUrl = artwork.path("480x480").asText("");
            } else if (artwork.hasNonNull("1000x1000")) {
                coverUrl = artwork.path("1000x1000").asText("");
            } else if (artwork.hasNonNull("150x150")) {
                coverUrl = artwork.path("150x150").asText("");
            }
        }

        if (coverUrl.isBlank()) {
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500";
        }

        return new PlaylistDto(
                id,
                title,
                description,
                "gradient",
                coverUrl
        );
    }

    public List<PlaylistDto> mapPlaylists(JsonNode dataNode) {
        List<PlaylistDto> list = new ArrayList<>();
        if (dataNode == null || !dataNode.isArray()) {
            return list;
        }

        for (JsonNode item : dataNode) {
            PlaylistDto dto = mapPlaylist(item);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\service\audius\AudiusPlaylistMapper.java", $playlistMapper, $noBom)

$audiusService = @"
package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.sonexa.backend.model.dto.CatalogDtos.ArtistDto;
import com.sonexa.backend.model.dto.CatalogDtos.PlaylistDto;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class AudiusService {

    private static final Logger log = LoggerFactory.getLogger(AudiusService.class);

    private final AudiusClient audiusClient;
    private final AudiusTrackMapper trackMapper;
    private final AudiusArtistMapper artistMapper;
    private final AudiusPlaylistMapper playlistMapper;

    public AudiusService(
            AudiusClient audiusClient,
            AudiusTrackMapper trackMapper,
            AudiusArtistMapper artistMapper,
            AudiusPlaylistMapper playlistMapper
    ) {
        this.audiusClient = audiusClient;
        this.trackMapper = trackMapper;
        this.artistMapper = artistMapper;
        this.playlistMapper = playlistMapper;
    }

    public List<TrackDto> getTrendingTracks(int limit) {
        try {
            JsonNode root = audiusClient.get("/tracks/trending", "limit=" + Math.max(limit, 10));
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to fetch trending tracks from Audius: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<TrackDto> searchTracks(String query, int limit) {
        if (query == null || query.isBlank()) {
            return getTrendingTracks(limit);
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            JsonNode root = audiusClient.get("/tracks/search", "query=" + encoded + "&limit=" + Math.max(limit, 10));
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to search tracks from Audius for [{}]: {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<ArtistDto> searchArtists(String query, int limit) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            JsonNode root = audiusClient.get("/users/search", "query=" + encoded + "&limit=" + Math.max(limit, 6));
            if (root != null && root.hasNonNull("data")) {
                return artistMapper.mapArtists(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to search artists from Audius for [{}]: {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<PlaylistDto> searchPlaylists(String query, int limit) {
        if (query == null || query.isBlank()) {
            return getTrendingPlaylists(limit);
        }
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            JsonNode root = audiusClient.get("/playlists/search", "query=" + encoded + "&limit=" + Math.max(limit, 6));
            if (root != null && root.hasNonNull("data")) {
                return playlistMapper.mapPlaylists(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to search playlists from Audius for [{}]: {}", query, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<PlaylistDto> getTrendingPlaylists(int limit) {
        try {
            JsonNode root = audiusClient.get("/playlists/trending", "limit=" + Math.max(limit, 8));
            if (root != null && root.hasNonNull("data")) {
                return playlistMapper.mapPlaylists(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to fetch trending playlists from Audius: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public TrackDto getTrackById(String id) {
        try {
            JsonNode root = audiusClient.get("/tracks/" + id, "");
            if (root != null && root.hasNonNull("data")) {
                JsonNode trackData = root.get("data");
                String streamUrl = audiusClient.resolveStreamUrl(id);
                return trackMapper.mapTrack(trackData, streamUrl);
            }
        } catch (Exception e) {
            log.error("Failed to fetch track [{}] from Audius: {}", id, e.getMessage());
        }
        return null;
    }

    public ArtistDto getArtistById(String id) {
        try {
            JsonNode root = audiusClient.get("/users/" + id, "");
            if (root != null && root.hasNonNull("data")) {
                return artistMapper.mapArtist(root.get("data"));
            }
        } catch (Exception e) {
            log.error("Failed to fetch artist [{}] from Audius: {}", id, e.getMessage());
        }
        return null;
    }

    public List<TrackDto> getArtistTracks(String artistId, int limit) {
        try {
            JsonNode root = audiusClient.get("/users/" + artistId + "/tracks", "limit=" + Math.max(limit, 20));
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to fetch artist tracks [{}] from Audius: {}", artistId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<TrackDto> getPlaylistTracks(String playlistId) {
        try {
            JsonNode root = audiusClient.get("/playlists/" + playlistId + "/tracks", "");
            if (root != null && root.hasNonNull("data")) {
                return trackMapper.mapTracks(root.get("data"), audiusClient);
            }
        } catch (Exception e) {
            log.error("Failed to fetch playlist tracks [{}] from Audius: {}", playlistId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public String resolveStreamUrl(String trackId) {
        return audiusClient.resolveStreamUrl(trackId);
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\service\audius\AudiusService.java", $audiusService, $noBom)

$openApiConfig = @"
package com.sonexa.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tuneFlowOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(new Info()
                        .title("TuneFlow Music Streaming REST API")
                        .description("Production-ready API for TuneFlow streaming platform powered by Audius legal music catalog.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TuneFlow Engineering Team")
                                .email("engineering@tuneflow.app")
                                .url("https://tuneflow.app"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\config\OpenApiConfig.java", $openApiConfig, $noBom)

$searchController = @"
package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.ArtistDto;
import com.sonexa.backend.model.dto.CatalogDtos.PlaylistDto;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@CrossOrigin(origins = "*")
public class SearchController {

    private final AudiusService audiusService;

    public SearchController(AudiusService audiusService) {
        this.audiusService = audiusService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> search(
            @RequestParam(value = "q", defaultValue = "") String query,
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        String trimmed = query.trim();
        List<TrackDto> tracks = Collections.emptyList();
        List<ArtistDto> artists = Collections.emptyList();
        List<PlaylistDto> playlists = Collections.emptyList();

        if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("tracks") || type.equalsIgnoreCase("songs")) {
            tracks = audiusService.searchTracks(trimmed, limit);
        }
        if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("artists")) {
            artists = audiusService.searchArtists(trimmed, 8);
        }
        if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("playlists") || type.equalsIgnoreCase("albums")) {
            playlists = audiusService.searchPlaylists(trimmed, 8);
        }

        Map<String, Object> result = Map.of(
                "query", trimmed,
                "tracks", tracks,
                "artists", artists,
                "playlists", playlists,
                "albums", Collections.emptyList()
        );

        return ApiResponse.success(result);
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\controller\SearchController.java", $searchController, $noBom)

$favController = @"
package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.model.entity.UserLibraryItem;
import com.sonexa.backend.repository.UserLibraryItemRepository;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final UserLibraryItemRepository libraryItemRepository;
    private final CatalogService catalogService;
    private final AudiusService audiusService;

    public FavoriteController(
            UserLibraryItemRepository libraryItemRepository,
            CatalogService catalogService,
            AudiusService audiusService
    ) {
        this.libraryItemRepository = libraryItemRepository;
        this.catalogService = catalogService;
        this.audiusService = audiusService;
    }

    @GetMapping({"/api/v1/favorites", "/api/v1/me/favorites"})
    public ApiResponse<List<TrackDto>> getFavorites() {
        String userKey = catalogService.currentUserKey();
        List<UserLibraryItem> items = libraryItemRepository.findByUserKeyAndLikedTrue(userKey);

        List<TrackDto> favorites = new ArrayList<>();
        for (UserLibraryItem item : items) {
            String tid = item.getTrackPublicId();
            if (tid != null && !tid.isBlank()) {
                TrackDto track = audiusService.getTrackById(tid);
                if (track != null) {
                    favorites.add(new TrackDto(
                            track.id(), track.title(), track.artist(), track.album(),
                            track.durationMs(), track.audioUrl(), track.coverUrl(),
                            track.playsCount(), true, track.provider(), track.providerTrackId(),
                            track.videoId(), track.providerUrl(), track.isPlayable(),
                            track.providerType(), track.channelTitle(), track.isOfficial()
                    ));
                }
            }
        }
        return ApiResponse.success(favorites);
    }

    @PostMapping({"/api/v1/favorites", "/api/v1/me/favorites"})
    public ApiResponse<Map<String, Object>> addFavorite(@RequestBody Map<String, String> body) {
        String trackId = body.get("trackId");
        if (trackId == null || trackId.isBlank()) {
            return ApiResponse.error("trackId is required", "INVALID_REQUEST");
        }

        String userKey = catalogService.currentUserKey();
        Optional<UserLibraryItem> existing = libraryItemRepository.findByUserKeyAndTrackPublicId(userKey, trackId);
        UserLibraryItem item = existing.orElseGet(UserLibraryItem::new);
        item.setUserKey(userKey);
        item.setTrackPublicId(trackId);
        item.setLiked(true);
        item.setLikedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        libraryItemRepository.save(item);

        return ApiResponse.success("Favorite added successfully", Map.of("trackId", trackId, "isLiked", true));
    }

    @DeleteMapping({"/api/v1/favorites/{trackId}", "/api/v1/me/favorites/{trackId}"})
    public ApiResponse<Map<String, Object>> removeFavorite(@PathVariable String trackId) {
        String userKey = catalogService.currentUserKey();
        Optional<UserLibraryItem> existing = libraryItemRepository.findByUserKeyAndTrackPublicId(userKey, trackId);
        if (existing.isPresent()) {
            UserLibraryItem item = existing.get();
            item.setLiked(false);
            item.setUpdatedAt(LocalDateTime.now());
            libraryItemRepository.save(item);
        }
        return ApiResponse.success("Favorite removed successfully", Map.of("trackId", trackId, "isLiked", false));
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\controller\FavoriteController.java", $favController, $noBom)

$histController = @"
package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.model.entity.UserLibraryItem;
import com.sonexa.backend.repository.UserLibraryItemRepository;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    private final UserLibraryItemRepository libraryItemRepository;
    private final CatalogService catalogService;
    private final AudiusService audiusService;

    public HistoryController(
            UserLibraryItemRepository libraryItemRepository,
            CatalogService catalogService,
            AudiusService audiusService
    ) {
        this.libraryItemRepository = libraryItemRepository;
        this.catalogService = catalogService;
        this.audiusService = audiusService;
    }

    @GetMapping
    public ApiResponse<List<TrackDto>> getListeningHistory(@RequestParam(value = "limit", defaultValue = "20") int limit) {
        String userKey = catalogService.currentUserKey();
        List<UserLibraryItem> historyItems = libraryItemRepository.findByUserKeyOrderByLastPlayedAtDesc(userKey);

        List<TrackDto> historyTracks = new ArrayList<>();
        for (UserLibraryItem item : historyItems) {
            if (historyTracks.size() >= limit) break;
            String tid = item.getTrackPublicId();
            if (tid != null && !tid.isBlank()) {
                TrackDto track = audiusService.getTrackById(tid);
                if (track != null) {
                    historyTracks.add(new TrackDto(
                            track.id(), track.title(), track.artist(), track.album(),
                            track.durationMs(), track.audioUrl(), track.coverUrl(),
                            track.playsCount(), item.isLiked(), track.provider(),
                            track.providerTrackId(), track.videoId(), track.providerUrl(),
                            track.isPlayable(), track.providerType(), track.channelTitle(),
                            track.isOfficial()
                    ));
                }
            }
        }
        return ApiResponse.success(historyTracks);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> recordHistory(@RequestBody Map<String, Object> payload) {
        String trackId = (String) payload.get("trackId");
        if (trackId == null || trackId.isBlank()) {
            return ApiResponse.error("trackId is required", "INVALID_REQUEST");
        }

        String userKey = catalogService.currentUserKey();
        Optional<UserLibraryItem> existing = libraryItemRepository.findByUserKeyAndTrackPublicId(userKey, trackId);
        UserLibraryItem item = existing.orElseGet(UserLibraryItem::new);
        item.setUserKey(userKey);
        item.setTrackPublicId(trackId);
        item.setLastPlayedAt(LocalDateTime.now());
        item.setPlayCount((item.getPlayCount() != null ? item.getPlayCount() : 0) + 1);
        item.setUpdatedAt(LocalDateTime.now());
        libraryItemRepository.save(item);

        return ApiResponse.success("History recorded", Map.of("trackId", trackId, "recorded", true));
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\controller\HistoryController.java", $histController, $noBom)

$recController = @"
package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final AudiusService audiusService;

    public RecommendationController(AudiusService audiusService) {
        this.audiusService = audiusService;
    }

    @GetMapping
    public ApiResponse<List<TrackDto>> getRecommendations(
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        List<TrackDto> trending = audiusService.getTrendingTracks(limit);
        return ApiResponse.success(trending);
    }
}
"@
[System.IO.File]::WriteAllText("D:\RAJU\BACKEND\sonexa-backend\src\main\java\com\sonexa\backend\controller\RecommendationController.java", $recController, $noBom)
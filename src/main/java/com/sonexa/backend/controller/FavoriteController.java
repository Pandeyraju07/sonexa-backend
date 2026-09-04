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
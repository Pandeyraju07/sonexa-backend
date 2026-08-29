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
package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/search")
@CrossOrigin(origins = "*")
public class SearchController {

    private final AudiusService audiusService;
    private final CatalogService catalogService;

    public SearchController(AudiusService audiusService, CatalogService catalogService) {
        this.audiusService = audiusService;
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public SearchCategoriesResponse categories() {
        return catalogService.searchCategories();
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> search(
            @RequestParam(value = "q", defaultValue = "") String query,
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        String trimmed = query.trim();
        SearchResponse nativeResults = catalogService.search(trimmed);

        List<TrackDto> tracks = new ArrayList<>(nativeResults.tracks() != null ? nativeResults.tracks() : List.of());
        List<AlbumDto> albums = new ArrayList<>(nativeResults.albums() != null ? nativeResults.albums() : List.of());
        List<ArtistDto> artists = new ArrayList<>();
        List<PlaylistDto> playlists = new ArrayList<>();

        if (tracks.isEmpty() || type.equalsIgnoreCase("tracks")) {
            try {
                List<TrackDto> audiusTracks = audiusService.searchTracks(trimmed, limit);
                tracks.addAll(audiusTracks);
            } catch (Exception ignored) {}
        }

        if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("artists")) {
            try {
                artists.addAll(audiusService.searchArtists(trimmed, 8));
            } catch (Exception ignored) {}
        }

        if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("playlists")) {
            try {
                playlists.addAll(audiusService.searchPlaylists(trimmed, 8));
            } catch (Exception ignored) {}
        }

        Map<String, Object> result = Map.of(
                "query", trimmed,
                "tracks", tracks,
                "artists", artists,
                "playlists", playlists,
                "albums", albums
        );

        return ApiResponse.success(result);
    }
}

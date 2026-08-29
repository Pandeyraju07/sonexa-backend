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
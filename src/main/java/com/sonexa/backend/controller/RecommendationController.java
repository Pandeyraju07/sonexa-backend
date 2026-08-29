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
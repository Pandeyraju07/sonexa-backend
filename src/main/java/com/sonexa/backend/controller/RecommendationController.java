package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.AiDtos.PredictionItem;
import com.sonexa.backend.model.dto.AiDtos.WhyThisSongResponse;
import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.ai.RecommendationEngine;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationEngine recommendationEngine;
    private final CatalogService catalogService;

    public RecommendationController(RecommendationEngine recommendationEngine, CatalogService catalogService) {
        this.recommendationEngine = recommendationEngine;
        this.catalogService = catalogService;
    }

    @GetMapping
    public ApiResponse<List<TrackDto>> getRecommendations(
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        String userKey = catalogService.requireAuthenticatedUserKey();
        List<TrackDto> tracks = recommendationEngine.recommendTracks(userKey, Math.min(Math.max(limit, 1), 50));
        return ApiResponse.success(tracks);
    }

    @GetMapping("/daily-mix")
    public ApiResponse<List<TrackDto>> getDailyMix() {
        List<TrackDto> tracks = recommendationEngine.generateDailyMix(catalogService.requireAuthenticatedUserKey());
        return ApiResponse.success(tracks);
    }

    @GetMapping("/surprise")
    public ApiResponse<List<TrackDto>> getSurprise() {
        List<TrackDto> tracks = recommendationEngine.generateSurprise(catalogService.requireAuthenticatedUserKey());
        return ApiResponse.success(tracks);
    }

    @GetMapping("/predictions")
    public ApiResponse<List<PredictionItem>> getPredictions() {
        List<PredictionItem> predictions = recommendationEngine.predictWhatILike(catalogService.requireAuthenticatedUserKey());
        return ApiResponse.success(predictions);
    }

    @GetMapping("/why/{trackId}")
    public ApiResponse<WhyThisSongResponse> getWhyThisSong(@PathVariable("trackId") String trackId) {
        WhyThisSongResponse response = recommendationEngine.getWhyThisSong(
                catalogService.requireAuthenticatedUserKey(), trackId);
        return ApiResponse.success(response);
    }

    @GetMapping("/mood")
    public ApiResponse<List<TrackDto>> getMoodSession(
            @RequestParam(value = "mood", defaultValue = "Chill") String mood,
            @RequestParam(value = "energy", required = false) Double energy
    ) {
        List<TrackDto> tracks = recommendationEngine.generateMoodSession(
                catalogService.requireAuthenticatedUserKey(), mood, energy);
        return ApiResponse.success(tracks);
    }

    @GetMapping("/energy")
    public ApiResponse<List<TrackDto>> getEnergySession(
            @RequestParam(value = "level", defaultValue = "0.5") Double level
    ) {
        List<TrackDto> tracks = recommendationEngine.generateEnergySession(
                catalogService.requireAuthenticatedUserKey(), level);
        return ApiResponse.success(tracks);
    }
}

package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.AiDtos.*;
import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.ai.AIDJService;
import com.sonexa.backend.service.ai.MusicIntentParser;
import com.sonexa.backend.service.ai.QueueRepairService;
import com.sonexa.backend.service.ai.RecommendationEngine;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final MusicIntentParser intentParser;
    private final RecommendationEngine recommendationEngine;
    private final QueueRepairService queueRepairService;
    private final AIDJService aidjService;
    private final AudiusService audiusService;
    private final CatalogService catalogService;

    public AiController(
            MusicIntentParser intentParser,
            RecommendationEngine recommendationEngine,
            QueueRepairService queueRepairService,
            AIDJService aidjService,
            AudiusService audiusService,
            CatalogService catalogService
    ) {
        this.intentParser = intentParser;
        this.recommendationEngine = recommendationEngine;
        this.queueRepairService = queueRepairService;
        this.aidjService = aidjService;
        this.audiusService = audiusService;
        this.catalogService = catalogService;
    }

    @PostMapping("/intent")
    public ApiResponse<MusicIntent> parseIntent(@RequestBody IntentParseRequest request) {
        MusicIntent intent = intentParser.parse(request.getText());
        return ApiResponse.success(intent);
    }

    @PostMapping("/change-vibe")
    public ApiResponse<ChangeVibeResponse> changeVibe(@RequestBody ChangeVibeRequest request) {
        String vibe = request.getVibe() != null ? request.getVibe().toUpperCase(Locale.ROOT) : "MORE_ENERGETIC";
        double targetEnergy = 0.5;
        String explanation = "Adjusted playback flow";

        if (vibe.contains("ENERGETIC") || vibe.contains("PARTY")) {
            targetEnergy = 0.85;
            explanation = "Elevated queue rhythm and upbeat energy profile";
        } else if (vibe.contains("RELAX") || vibe.contains("CALM") || vibe.contains("ACOUSTIC")) {
            targetEnergy = 0.30;
            explanation = "Calmed queue flow with soothing acoustic resonance";
        } else if (vibe.contains("ROMANTIC")) {
            targetEnergy = 0.55;
            explanation = "Tuned queue towards heartfelt romantic melodies";
        }

        List<TrackDto> reordered = new ArrayList<>(request.getCurrentQueue());
        if (reordered.isEmpty()) {
            reordered = recommendationEngine.generateEnergySession(catalogService.requireAuthenticatedUserKey(), targetEnergy);
        } else {
            Collections.shuffle(reordered);
        }

        return ApiResponse.success(new ChangeVibeResponse(vibe, targetEnergy, reordered, explanation));
    }

    @PostMapping("/fix-queue")
    public ApiResponse<FixQueueResponse> fixQueue(@RequestBody FixQueueRequest request) {
        FixQueueResponse response = queueRepairService.fixQueue(request.getQueue());
        return ApiResponse.success(response);
    }

    @PostMapping("/music-journey")
    public ApiResponse<MusicJourneyResponse> createJourney(
            @RequestParam(value = "theme", defaultValue = "CALM_TO_ENERGETIC") String theme,
            @RequestParam(value = "duration", defaultValue = "60") int duration
    ) {
        String userKey = catalogService.requireAuthenticatedUserKey();
        MusicJourneyResponse response = recommendationEngine.generateJourney(userKey, theme, duration);
        return ApiResponse.success(response);
    }

    @PostMapping("/dj/next")
    public ApiResponse<AIDJService.NextTrackDecision> djNext(
            @RequestBody(required = false) TrackDto currentTrack
    ) {
        String userKey = catalogService.requireAuthenticatedUserKey();
        AIDJService.NextTrackDecision decision = aidjService.selectNextTrack(userKey, currentTrack, Collections.emptyList());
        return ApiResponse.success(decision);
    }

    @PostMapping("/playlist")
    public ApiResponse<List<TrackDto>> generateAiPlaylist(@RequestBody IntentParseRequest request) {
        MusicIntent intent = intentParser.parse(request.getText());
        String query = intent.getQuery();
        if (!intent.getMoods().isEmpty()) {
            query = intent.getMoods().get(0) + " " + query;
        }
        if (!intent.getLanguages().isEmpty()) {
            query = intent.getLanguages().get(0) + " " + query;
        }
        List<TrackDto> tracks = audiusService.searchTracks(query, 20);
        if (tracks.isEmpty()) {
            tracks = audiusService.getTrendingTracks(15);
        }
        return ApiResponse.success(tracks);
    }
}
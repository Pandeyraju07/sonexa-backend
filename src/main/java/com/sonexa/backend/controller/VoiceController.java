package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.AiDtos.MusicIntent;
import com.sonexa.backend.model.dto.AiDtos.VoiceSearchRequest;
import com.sonexa.backend.model.dto.AiDtos.VoiceSearchResponse;
import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import com.sonexa.backend.model.entity.VoiceSearchHistory;
import com.sonexa.backend.repository.VoiceSearchHistoryRepository;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.ai.MusicIntentParser;
import com.sonexa.backend.service.audius.AudiusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/voice")
public class VoiceController {

    private final MusicIntentParser intentParser;
    private final AudiusService audiusService;
    private final VoiceSearchHistoryRepository historyRepository;
    private final CatalogService catalogService;

    public VoiceController(
            MusicIntentParser intentParser,
            AudiusService audiusService,
            VoiceSearchHistoryRepository historyRepository,
            CatalogService catalogService
    ) {
        this.intentParser = intentParser;
        this.audiusService = audiusService;
        this.historyRepository = historyRepository;
        this.catalogService = catalogService;
    }

    @PostMapping("/search")
    public ApiResponse<VoiceSearchResponse> voiceSearch(@RequestBody VoiceSearchRequest request) {
        String transcript = request.getTranscript() != null ? request.getTranscript().trim() : "";
        if (transcript.length() > 500) {
            transcript = transcript.substring(0, 500);
        }
        String userKey = catalogService.requireAuthenticatedUserKey();

        MusicIntent intent = intentParser.parse(transcript);
        List<TrackDto> tracks = audiusService.searchTracks(transcript, 15);
        if (tracks.isEmpty()) {
            tracks = audiusService.getTrendingTracks(10);
        }

        String feedback = String.format("Playing '%s'", transcript);
        if ("PAUSE".equals(intent.getIntentType())) feedback = "Music paused";
        if ("NEXT".equals(intent.getIntentType())) feedback = "Skipping to next track";
        if ("LIKE".equals(intent.getIntentType())) feedback = "Added to favorites";

        try {
            historyRepository.save(new VoiceSearchHistory(userKey, transcript, intent.getIntentType(), "", request.getLanguage()));
        } catch (Exception ignored) {
            // history is optional; search must still succeed
        }

        return ApiResponse.success(new VoiceSearchResponse(transcript, intent, feedback, tracks));
    }

    @GetMapping("/history")
    public ApiResponse<List<VoiceSearchHistory>> getHistory() {
        String userKey = catalogService.requireAuthenticatedUserKey();
        return ApiResponse.success(historyRepository.findByUserKeyOrderByTimestampDesc(userKey));
    }

    @DeleteMapping("/history")
    public ApiResponse<String> clearHistory() {
        String userKey = catalogService.requireAuthenticatedUserKey();
        historyRepository.deleteByUserKey(userKey);
        return ApiResponse.success("Voice search history cleared successfully.");
    }
}

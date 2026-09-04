package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.AiDtos.*;
import com.sonexa.backend.model.dto.ApiResponse;
import com.sonexa.backend.model.entity.EmotionCheckIn;
import com.sonexa.backend.repository.EmotionCheckInRepository;
import com.sonexa.backend.service.CatalogService;
import com.sonexa.backend.service.ai.MusicDnaService;
import com.sonexa.backend.service.ai.UserTasteProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserInsightsController {

    private final MusicDnaService musicDnaService;
    private final UserTasteProfileService tasteProfileService;
    private final EmotionCheckInRepository emotionCheckInRepository;
    private final CatalogService catalogService;

    public UserInsightsController(
            MusicDnaService musicDnaService,
            UserTasteProfileService tasteProfileService,
            EmotionCheckInRepository emotionCheckInRepository,
            CatalogService catalogService
    ) {
        this.musicDnaService = musicDnaService;
        this.tasteProfileService = tasteProfileService;
        this.emotionCheckInRepository = emotionCheckInRepository;
        this.catalogService = catalogService;
    }

    @GetMapping("/api/v1/me/music-dna")
    public ApiResponse<MusicDnaResponse> getMusicDna() {
        return ApiResponse.success(musicDnaService.getMusicDna(catalogService.requireAuthenticatedUserKey()));
    }

    @GetMapping("/api/v1/me/listening-insights")
    public ApiResponse<ListeningInsightsResponse> getListeningInsights() {
        return ApiResponse.success(musicDnaService.getListeningInsights(catalogService.requireAuthenticatedUserKey()));
    }

    @GetMapping("/api/v1/me/soundtrack")
    public ApiResponse<MusicDnaResponse> getSoundtrack() {
        MusicDnaResponse dna = musicDnaService.getMusicDna(catalogService.requireAuthenticatedUserKey());
        dna.setSummaryText("The definitive soundtrack of your musical journey in 2026.");
        return ApiResponse.success(dna);
    }

    @PostMapping("/api/v1/events")
    public ApiResponse<String> trackEvent(@RequestBody UserEventDto eventDto) {
        if (eventDto == null) {
            eventDto = new UserEventDto();
        }
        eventDto.setUserKey(catalogService.requireAuthenticatedUserKey());
        tasteProfileService.recordEvent(eventDto);
        return ApiResponse.success("Event recorded");
    }

    @PostMapping("/api/v1/me/emotion-checkin")
    public ApiResponse<String> emotionCheckIn(
            @RequestParam(value = "emotion", defaultValue = "Calm") String emotion,
            @RequestParam(value = "intensity", defaultValue = "3") int intensity
    ) {
        String userKey = catalogService.requireAuthenticatedUserKey();
        int safeIntensity = Math.min(5, Math.max(1, intensity));
        emotionCheckInRepository.save(new EmotionCheckIn(userKey, emotion, safeIntensity));
        return ApiResponse.success("Emotion check-in recorded");
    }
}

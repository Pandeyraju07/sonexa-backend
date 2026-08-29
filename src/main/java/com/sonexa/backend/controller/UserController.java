package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final CatalogService catalogService;

    public UserController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/profile")
    public UserProfileResponse profile() {
        return catalogService.profile();
    }

    @PutMapping("/profile")
    public SimpleSuccessResponse updateProfile(@RequestBody UpdateProfileRequest body) {
        return catalogService.updateProfile(body != null ? body : new UpdateProfileRequest(null, null, null));
    }

    @GetMapping("/library")
    public UserLibraryResponse library() {
        return catalogService.library();
    }

    @PostMapping("/like")
    public ToggleLikeResponse like(@RequestBody ToggleLikeRequest body) {
        String trackId = body != null && body.trackId() != null ? body.trackId() : "tr_1";
        return catalogService.toggleLike(trackId);
    }

    @GetMapping("/notifications")
    public NotificationListResponse notifications() {
        return catalogService.notifications();
    }

    @GetMapping("/settings")
    public SettingsResponse settings() {
        return catalogService.settings();
    }

    @PutMapping("/settings")
    public SimpleSuccessResponse updateSettings(@RequestBody UpdateSettingsRequest body) {
        return catalogService.updateSettings(body != null ? body : new UpdateSettingsRequest(Map.of()));
    }

    @GetMapping("/premium")
    public PremiumResponse premium() {
        return catalogService.premium();
    }

    @PostMapping("/premium/subscribe")
    public SimpleSuccessResponse subscribe(@RequestBody(required = false) SubscribeRequest body) {
        return catalogService.subscribe(body != null ? body.planId() : "individual");
    }
}

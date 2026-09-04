package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.service.AuthService;
import com.sonexa.backend.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final CatalogService catalogService;
    private final AuthService authService;

    public UserController(CatalogService catalogService, AuthService authService) {
        this.catalogService = catalogService;
        this.authService = authService;
    }

    @GetMapping("/profile")
    public UserProfileResponse profile() {
        return catalogService.profile();
    }

    @PutMapping("/profile")
    public SimpleSuccessResponse updateProfile(@RequestBody UpdateProfileRequest body) {
        return catalogService.updateProfile(body != null ? body : new UpdateProfileRequest(null, null, null, null));
    }

    @GetMapping("/library")
    public UserLibraryResponse library() {
        return catalogService.library();
    }

    @PostMapping("/like")
    public ToggleLikeResponse like(@RequestBody ToggleLikeRequest body) {
        String trackId = body != null && body.trackId() != null && !body.trackId().isBlank() ? body.trackId() : null;
        if (trackId == null) {
            return new ToggleLikeResponse(false, "", false, "trackId is required");
        }
        return catalogService.toggleLike(trackId);
    }

    @GetMapping("/playlists")
    public UserPlaylistsResponse playlists() {
        return catalogService.getUserPlaylists();
    }

    @PostMapping("/playlists")
    public PlaylistDto createPlaylist(@RequestBody CreatePlaylistRequest body) {
        return catalogService.createPlaylist(body);
    }

    @PutMapping("/playlists/{id}")
    public PlaylistDto updatePlaylist(@PathVariable String id, @RequestBody UpdatePlaylistRequest body) {
        return catalogService.updatePlaylist(id, body);
    }

    @DeleteMapping("/playlists/{id}")
    public SimpleSuccessResponse deletePlaylist(@PathVariable String id) {
        return catalogService.deletePlaylist(id);
    }

    @PostMapping("/playlists/{id}/tracks")
    public SimpleSuccessResponse addTrackToPlaylist(@PathVariable String id, @RequestBody AddTrackToPlaylistRequest body) {
        return catalogService.addTrackToPlaylist(id, body);
    }

    @DeleteMapping("/playlists/{id}/tracks/{trackId}")
    public SimpleSuccessResponse removeTrackFromPlaylist(@PathVariable String id, @PathVariable String trackId) {
        return catalogService.removeTrackFromPlaylist(id, trackId);
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

    @PostMapping("/premium/redeem")
    public RedeemCouponResponse redeem(@RequestBody(required = false) RedeemCouponRequest body) {
        return catalogService.redeemCoupon(body != null ? body.code() : "");
    }

    @PostMapping("/premium/cancel")
    public SimpleSuccessResponse cancelPremium() {
        return catalogService.cancelSubscription();
    }

    @DeleteMapping("/account")
    public SimpleSuccessResponse deleteAccount() {
        authService.deleteAccount(null);
        return new SimpleSuccessResponse(true, "Account deleted successfully");
    }
}

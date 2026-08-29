package com.sonexa.backend.controller;

import com.sonexa.backend.model.dto.AdminOpsDtos.*;
import com.sonexa.backend.service.AdminOpsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/ops")
@CrossOrigin(origins = "*")
public class AdminOpsController {

    private final AdminOpsService adminOpsService;

    public AdminOpsController(AdminOpsService adminOpsService) {
        this.adminOpsService = adminOpsService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return adminOpsService.dashboard();
    }

    @GetMapping("/users")
    public AdminUserListResponse users(@RequestParam(value = "q", required = false) String q) {
        return adminOpsService.listUsers(q);
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest body) {
        return adminOpsService.updateUser(id, body);
    }

    @DeleteMapping("/users/{id}")
    public AdminOpsService.SimpleMsg deleteUser(@PathVariable Long id) {
        return adminOpsService.deleteUser(id);
    }

    @GetMapping("/playlists")
    public AdminPlaylistListResponse playlists() {
        return adminOpsService.listPlaylists();
    }

    @PostMapping("/playlists")
    public AdminPlaylistResponse createPlaylist(@RequestBody CreatePlaylistRequest body) {
        return adminOpsService.createPlaylist(body);
    }

    @GetMapping("/podcasts")
    public AdminPodcastListResponse podcasts() {
        return adminOpsService.listPodcasts();
    }

    @PostMapping("/notifications/broadcast")
    public AdminOpsService.SimpleMsg broadcast(@RequestBody BroadcastNotificationRequest body) {
        return adminOpsService.broadcast(body);
    }

    @GetMapping("/analytics")
    public AnalyticsOverviewResponse analytics() {
        return adminOpsService.analytics();
    }

    @GetMapping("/ai")
    public AiOverviewResponse ai() {
        return adminOpsService.aiOverview();
    }

    @GetMapping("/system")
    public SystemSettingsResponse system() {
        return adminOpsService.systemSettings();
    }

    @PutMapping("/system")
    public SystemSettingsResponse updateSystem(@RequestBody UpdateSystemSettingsRequest body) {
        return adminOpsService.updateSystemSettings(body);
    }

    @GetMapping("/storage")
    public StorageOverviewResponse storage() {
        return adminOpsService.storage();
    }

    @GetMapping("/search")
    public SearchManagementResponse search() {
        return adminOpsService.searchManagement();
    }

    @GetMapping("/recommendations")
    public RecommendationConfigResponse recommendations() {
        return adminOpsService.recommendationConfig();
    }

    @PutMapping("/recommendations")
    public RecommendationConfigResponse updateRecommendations(@RequestBody RecommendationConfigResponse body) {
        return adminOpsService.updateRecommendation(body);
    }

    @GetMapping("/moderation")
    public ModerationQueueResponse moderation() {
        return adminOpsService.moderation();
    }

    @GetMapping("/subscriptions")
    public SubscriptionOverviewResponse subscriptions() {
        return adminOpsService.subscriptions();
    }
}

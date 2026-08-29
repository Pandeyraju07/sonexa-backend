package com.sonexa.backend.model.dto;

import java.util.List;
import java.util.Map;

/** Operations-center DTOs for Spotify-level admin. */
public class AdminOpsDtos {

    public record ChartPoint(String label, double value) {}

    public record NamedMetric(String name, String value, String delta) {}

    public record ActivityItem(String id, String title, String subtitle, String timeAgo, String type) {}

    public record AdminDashboardResponse(
            boolean success,
            String welcomeTitle,
            KpiBlock kpis,
            TodayBlock today,
            List<ChartPoint> dailyStreams,
            List<ChartPoint> newUsers,
            List<ChartPoint> revenue,
            List<ChartPoint> aiUsage,
            List<NamedMetric> deviceDistribution,
            List<NamedMetric> topGenres,
            List<NamedMetric> topArtists,
            List<NamedMetric> topSongs,
            List<ActivityItem> recentActivities,
            List<ActivityItem> pendingReports,
            List<ActivityItem> recentUploads,
            ServerStatus serverStatus
    ) {}

    public record KpiBlock(
            long totalUsers,
            long activeUsersToday,
            long premiumUsers,
            long songs,
            long albums,
            long artists,
            long playlists,
            long podcasts,
            long aiRequests,
            String revenue,
            String storageUsed,
            long streamingSessions
    ) {}

    public record TodayBlock(
            String revenue,
            long streams,
            long aiRequests,
            long newUsers
    ) {}

    public record ServerStatus(
            String status,
            String apiLatencyMs,
            String uptime,
            boolean maintenanceMode,
            String version
    ) {}

    public record AdminUserDto(
            String id,
            Long dbId,
            String name,
            String email,
            String role,
            boolean premium,
            boolean enabled,
            boolean emailVerified,
            String provider,
            String createdAt
    ) {}

    public record AdminUserListResponse(boolean success, List<AdminUserDto> users, long total) {}

    public record UpdateUserRequest(
            Boolean premium,
            Boolean enabled,
            String role
    ) {}

    public record AdminUserResponse(boolean success, String message, AdminUserDto user) {}

    public record AdminPlaylistDto(
            String id,
            Long dbId,
            String title,
            String subtitle,
            String artworkType,
            String coverUrl,
            boolean madeForYou,
            String type
    ) {}

    public record AdminPlaylistListResponse(boolean success, List<AdminPlaylistDto> playlists) {}

    public record AdminPlaylistResponse(boolean success, String message, AdminPlaylistDto playlist) {}

    public record CreatePlaylistRequest(
            String title,
            String subtitle,
            String artworkType,
            String coverUrl,
            Boolean madeForYou,
            String type
    ) {}

    public record AdminPodcastDto(
            String id,
            Long dbId,
            String title,
            String host,
            String category,
            String coverUrl
    ) {}

    public record AdminPodcastListResponse(boolean success, List<AdminPodcastDto> podcasts) {}

    public record BroadcastNotificationRequest(
            String audience,
            String title,
            String message,
            String targetEmail
    ) {}

    public record AnalyticsOverviewResponse(
            boolean success,
            long totalStreams,
            String completionRate,
            String skipRate,
            String repeatRate,
            String avgSessionLength,
            List<NamedMetric> mostSearched,
            List<NamedMetric> mostPlayedArtists,
            List<NamedMetric> countryAnalytics,
            List<NamedMetric> deviceAnalytics,
            List<ChartPoint> streamsTrend
    ) {}

    public record AiOverviewResponse(
            boolean success,
            long conversations,
            long djRequests,
            long playlistGenerations,
            long moodDetections,
            long voiceCommands,
            long failedRequests,
            String apiCost,
            long tokensUsed,
            List<ActivityItem> recentRequests
    ) {}

    public record SystemSettingsResponse(
            boolean success,
            String appVersion,
            boolean maintenanceMode,
            Map<String, Boolean> featureFlags,
            Map<String, String> aiProviders,
            Map<String, String> storageSettings
    ) {}

    public record UpdateSystemSettingsRequest(
            Boolean maintenanceMode,
            Map<String, Boolean> featureFlags
    ) {}

    public record StorageOverviewResponse(
            boolean success,
            String uploadedAudio,
            String images,
            String cache,
            String cdnUsage,
            List<NamedMetric> breakdown
    ) {}

    public record SearchManagementResponse(
            boolean success,
            List<String> trendingSearches,
            List<String> blockedKeywords,
            List<NamedMetric> searchAnalytics
    ) {}

    public record RecommendationConfigResponse(
            boolean success,
            Map<String, Double> weights,
            String trendingLogic,
            boolean aiRankingEnabled,
            boolean moodRankingEnabled
    ) {}

    public record ModerationQueueResponse(
            boolean success,
            List<ActivityItem> reportedSongs,
            List<ActivityItem> reportedPlaylists,
            List<ActivityItem> reportedComments,
            List<ActivityItem> copyrightClaims
    ) {}

    public record SubscriptionOverviewResponse(
            boolean success,
            List<NamedMetric> plans,
            String mrr,
            String refunds,
            long renewals,
            List<NamedMetric> coupons
    ) {}
}

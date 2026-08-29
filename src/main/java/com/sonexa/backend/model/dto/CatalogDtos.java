package com.sonexa.backend.model.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Flat response DTOs matching Android Retrofit models (success + payload at top level).
 */
public final class CatalogDtos {

    private CatalogDtos() {}

    public record TrackDto(
            String id, String title, String artist, String album,
            long durationMs, String audioUrl, String coverUrl,
            String playsCount, boolean isLiked,
            String provider, String providerTrackId, String videoId,
            String providerUrl, Boolean isPlayable, String providerType,
            String channelTitle, Boolean isOfficial
    ) {
        public TrackDto(String id, String title, String artist, String album,
                        long durationMs, String audioUrl, String coverUrl,
                        String playsCount, boolean isLiked) {
            this(id, title, artist, album, durationMs, audioUrl, coverUrl, playsCount, isLiked,
                 "SONEXA", id, null, audioUrl, true, "NATIVE", null, false);
        }
    }

    public record AlbumDto(
            String id, String title, String artist, String year,
            String coverUrl, int trackCount
    ) {}

    public record PlaylistDto(
            String id, String title, String subtitle, String artworkType, String coverUrl
    ) {}

    public record ArtistDto(
            String id, String name, String genre, String bio, String imageUrl,
            String color1, String color2, int followersCount, boolean verified
    ) {}

    public record GenreDto(String id, String name, String color1, String color2, String imageUrl) {}

    public record MoodDto(String id, String name, String iconKey, String colorHex) {}

    public record PodcastChapterDto(
            String title,
            long startTimeSeconds,
            long endTimeSeconds
    ) {}

    public record PodcastLanguageDto(
            String code,
            String name,
            String nativeName,
            String coverUrl,
            int showCount
    ) {}

    public record PodcastCategoryDto(
            String id,
            String name,
            String icon,
            String colorHex,
            String gradientFrom,
            String gradientTo
    ) {}

    public record PodcastDto(
            String id,
            String title,
            String host,
            String description,
            String coverUrl,
            String category,
            String language,
            String followerCount,
            int episodeCount,
            boolean isFollowed
    ) {
        public PodcastDto(String id, String title, String host, String description, String coverUrl, String category) {
            this(id, title, host, description, coverUrl, category, "Hindi", "125K", 25, false);
        }
    }

    public record PodcastEpisodeDto(
            String id,
            String podcastId,
            String title,
            String description,
            String durationLabel,
            long durationMs,
            String audioUrl,
            String coverUrl,
            int episodeNumber,
            String publishedAt,
            int progressPercent,
            List<PodcastChapterDto> chapters
    ) {
        public PodcastEpisodeDto(String id, String title, String description, String durationLabel, String audioUrl, int episodeNumber) {
            this(id, "", title, description, durationLabel, 1800000L, audioUrl, "", episodeNumber, "Recently added", 0, Collections.emptyList());
        }
    }

    public record PodcastHomeResponse(
            boolean success,
            List<PodcastEpisodeDto> continueListening,
            List<PodcastLanguageDto> languages,
            List<PodcastDto> trendingPodcasts,
            List<PodcastDto> madeForYou,
            List<PodcastDto> popularShows,
            List<PodcastCategoryDto> categories
    ) {}

    public record NotificationDto(
            String id, String title, String message, String iconKey, String colorHex,
            String timeAgo, boolean read
    ) {}

    public record HomeFeedResponse(
            boolean success,
            List<TrackDto> continueListening,
            List<TrackDto> trendingNow,
            List<AlbumDto> popularAlbums,
            List<PlaylistDto> madeForYou
    ) {}

    public record TrendingResponse(boolean success, List<TrackDto> tracks) {}

    public record SearchResponse(
            boolean success,
            List<TrackDto> tracks,
            List<AlbumDto> albums,
            List<String> artists
    ) {}

    public record TrackDetailResponse(boolean success, TrackDto track) {}

    public record AlbumDetailResponse(boolean success, AlbumDto album, List<TrackDto> tracks) {}

    public record PlaylistDetailResponse(boolean success, PlaylistDto playlist, List<TrackDto> tracks) {}

    public record ArtistDetailResponse(boolean success, ArtistDto artist, List<TrackDto> tracks, List<AlbumDto> albums) {}

    public record QueueResponse(boolean success, TrackDto nowPlaying, List<TrackDto> queue, String sourceTitle) {}

    public record LyricsLineDto(long tMs, String text) {}

    public record LyricsResponse(
            boolean success, String trackId, boolean synced, List<LyricsLineDto> lines, String plainText, String source
    ) {}

    public record GenreListResponse(boolean success, List<GenreDto> genres) {}

    public record ArtistListResponse(boolean success, List<ArtistDto> artists) {}

    public record MoodListResponse(boolean success, List<MoodDto> moods) {}

    public record PodcastListResponse(boolean success, List<PodcastDto> podcasts) {}

    public record PodcastDetailResponse(boolean success, PodcastDto podcast, List<PodcastEpisodeDto> episodes) {}

    public record NotificationListResponse(boolean success, List<NotificationDto> notifications) {}

    public record UserProfileResponse(boolean success, Map<String, Object> user) {}

    public record UserLibraryResponse(boolean success, List<TrackDto> likedSongs, List<AlbumDto> savedAlbums) {}

    public record ToggleLikeResponse(boolean success, String trackId, boolean isLiked, String message) {}

    public record SimpleSuccessResponse(boolean success, String message) {}

    public record SaveListResponse(boolean success, String message, List<String> items, int count) {}

    public record PremiumResponse(
            boolean success,
            boolean isPremium,
            List<Map<String, Object>> plans,
            List<String> benefits
    ) {}

    public record SettingsResponse(boolean success, Map<String, Object> settings) {}

    public record AiSignatureResponse(
            boolean success,
            String signatureId,
            String vibeTitle,
            String aiGeneratedAudioUrl,
            int bpm,
            String key,
            List<TrackDto> recommendedTracks
    ) {}

    public record AiChatResponse(boolean success, String reply) {}

    public record SplashConfigResponse(
            boolean success, String appName, String version, String minSupportedVersion,
            boolean forceUpdate, boolean maintenanceMode, String message
    ) {}

    public record OnboardingSlideDto(String title, String subtitle) {}

    public record OnboardingResponse(boolean success, List<OnboardingSlideDto> slides) {}

    public record LanguageDto(String code, String name, String nativeName) {}

    public record LanguagesCatalogResponse(
            boolean success, String title, String subtitle, int minSelection,
            List<String> defaultSelected, List<LanguageDto> languages
    ) {}

    public record AppUpdateResponse(
            boolean success, boolean updateAvailable, boolean forceUpdate,
            String latestVersion, String message, String storeUrl
    ) {}

    public record PermissionsConfigResponse(
            boolean success,
            Map<String, Object> notifications,
            Map<String, Object> downloads
    ) {}

    public record PreferenceSaveRequest(List<String> items) {}

    public record LanguagesSaveRequest(List<String> languages) {}

    public record ProfileCreateRequest(String displayName, String handle) {}

    public record ToggleLikeRequest(String trackId) {}

    public record UpdateProfileRequest(String name, String profilePicUrl, String bio) {}

    public record UpdateSettingsRequest(Map<String, Object> settings) {}

    public record SubscribeRequest(String planId) {}

    public record AiSignatureRequest(String mood, String prompt, String detectedEmotion) {}

    public record AiChatRequest(String message) {}

    // LIVE EVENTS DTOS
    public record EventSetlistTrackDto(
            String id,
            String title,
            String artist,
            String audioUrl,
            String coverUrl,
            String durationLabel,
            long durationMs
    ) {}

    public record EventTicketTierDto(
            String id,
            String name,
            String price,
            String description,
            List<String> perks,
            boolean isAvailable
    ) {}

    public record LiveEventDto(
            String id,
            String title,
            String artistName,
            String artistImageUrl,
            String bannerUrl,
            String venue,
            String city,
            String date,
            String time,
            String priceStarting,
            String status, // "SELLING_FAST", "LIVE_NOW", "UPCOMING", "SOLD_OUT"
            String category, // "Stadium Tour", "Festival", "Acoustic", "Club"
            String bookingUrl,
            boolean isReminderSet,
            List<String> lineup,
            List<EventSetlistTrackDto> setlist
    ) {}

    public record LiveEventsFeedResponse(
            boolean success,
            String title,
            List<String> cities,
            List<String> categories,
            List<LiveEventDto> featuredTours,
            List<LiveEventDto> events
    ) {}

    public record LiveEventDetailResponse(
            boolean success,
            LiveEventDto event,
            List<EventTicketTierDto> ticketTiers,
            List<LiveEventDto> nearbyEvents
    ) {}

    // HOME OF I-POP DTOS
    public record IPopArtistDto(
            String id,
            String name,
            String imageUrl,
            String followers,
            String topSongTitle,
            boolean isVerified
    ) {}

    public record IPopPlaylistDto(
            String id,
            String title,
            String description,
            String coverUrl,
            String badge,
            int trackCount,
            List<TrackDto> tracks
    ) {}

    public record IPopHomeResponse(
            boolean success,
            String title,
            String subtitle,
            String spotlightBannerUrl,
            String spotlightTitle,
            String spotlightSubtitle,
            List<String> subgenres,
            List<TrackDto> trendingTracks,
            List<IPopPlaylistDto> featuredPlaylists,
            List<IPopArtistDto> spotlightArtists,
            List<TrackDto> newReleases
    ) {}
}
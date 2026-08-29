package com.sonexa.backend.service;

import com.sonexa.backend.constant.ErrorCode;
import com.sonexa.backend.exception.BusinessException;
import com.sonexa.backend.model.dto.AdminOpsDtos.*;
import com.sonexa.backend.model.entity.*;
import com.sonexa.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminOpsService {

    private static final AtomicBoolean MAINTENANCE = new AtomicBoolean(false);
    private static final Map<String, Boolean> FEATURE_FLAGS = new LinkedHashMap<>();
    private static final Map<String, Double> REC_WEIGHTS = new LinkedHashMap<>();

    static {
        FEATURE_FLAGS.put("aiDj", true);
        FEATURE_FLAGS.put("moodDetection", true);
        FEATURE_FLAGS.put("voiceCommands", true);
        FEATURE_FLAGS.put("offlineDownloads", true);
        FEATURE_FLAGS.put("losslessAudio", false);
        FEATURE_FLAGS.put("socialSharing", true);
        REC_WEIGHTS.put("trending", 0.35);
        REC_WEIGHTS.put("affinity", 0.30);
        REC_WEIGHTS.put("mood", 0.20);
        REC_WEIGHTS.put("freshness", 0.15);
    }

    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final PodcastRepository podcastRepository;
    private final AppNotificationRepository notificationRepository;
    private final MediaStorageService mediaStorageService;

    @Value("${sonexa.media.public-base-url:}")
    private String publicBaseUrl;

    @Value("${spring.application.name:sonexa-backend}")
    private String appName;

    public AdminOpsService(
            UserRepository userRepository,
            TrackRepository trackRepository,
            ArtistRepository artistRepository,
            AlbumRepository albumRepository,
            GenreRepository genreRepository,
            PlaylistRepository playlistRepository,
            PodcastRepository podcastRepository,
            AppNotificationRepository notificationRepository,
            MediaStorageService mediaStorageService
    ) {
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
        this.playlistRepository = playlistRepository;
        this.podcastRepository = podcastRepository;
        this.notificationRepository = notificationRepository;
        this.mediaStorageService = mediaStorageService;
    }

    public AdminDashboardResponse dashboard() {
        List<User> users = userRepository.findAll();
        List<Track> tracks = trackRepository.findAll();
        List<Artist> artists = artistRepository.findAll();
        List<Genre> genres = genreRepository.findAll();

        long totalUsers = users.size();
        long premium = users.stream().filter(User::isPremium).count();
        long songs = tracks.size();
        long albums = albumRepository.count();
        long artistCount = artists.size();
        long playlists = playlistRepository.count();
        long podcasts = podcastRepository.count();
        long newUsersToday = users.stream().filter(u -> isToday(u.getCreatedAt())).count();
        long activeToday = users.stream().filter(u -> isToday(u.getUpdatedAt()) || isToday(u.getCreatedAt())).count();
        String storage = formatBytes(folderSize(mediaStorageService.getRoot()));

        KpiBlock kpis = new KpiBlock(
                totalUsers,
                activeToday,
                premium,
                songs,
                albums,
                artistCount,
                playlists,
                podcasts,
                0L,
                "0",
                storage,
                0L
        );
        TodayBlock today = new TodayBlock(
                "0",
                0L,
                0L,
                newUsersToday
        );

        return new AdminDashboardResponse(
                true,
                "Welcome Admin",
                kpis,
                today,
                List.of(),
                usersCreatedLast7Days(users),
                List.of(),
                List.of(),
                authProviderDistribution(users),
                genreMetrics(genres, artists),
                topArtists(artists),
                topSongs(tracks),
                recentActivities(tracks, users),
                List.of(),
                recentUploads(tracks),
                new ServerStatus(
                        MAINTENANCE.get() ? "MAINTENANCE" : "ONLINE",
                        "—",
                        "—",
                        MAINTENANCE.get(),
                        "1.0.0"
                )
        );
    }

    public AdminUserListResponse listUsers(String q) {
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<AdminUserDto> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .filter(u -> query.isBlank()
                        || (u.getEmail() != null && u.getEmail().toLowerCase(Locale.ROOT).contains(query))
                        || (u.getName() != null && u.getName().toLowerCase(Locale.ROOT).contains(query)))
                .map(this::toUserDto)
                .toList();
        return new AdminUserListResponse(true, users, users.size());
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Body required");
        }
        if (request.premium() != null) user.setPremium(request.premium());
        if (request.enabled() != null) user.setEnabled(request.enabled());
        if (request.role() != null && !request.role().isBlank()) {
            String role = request.role().trim().toUpperCase(Locale.ROOT);
            if (!role.equals("USER") && !role.equals("ADMIN")) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Role must be USER or ADMIN");
            }
            user.setRole(role);
        }
        user = userRepository.save(user);
        return new AdminUserResponse(true, "User updated", toUserDto(user));
    }

    @Transactional
    public SimpleMsg deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Cannot delete admin accounts");
        }
        userRepository.delete(user);
        return new SimpleMsg(true, "User deleted");
    }

    public AdminPlaylistListResponse listPlaylists() {
        List<AdminPlaylistDto> list = playlistRepository.findAll().stream()
                .map(this::toPlaylistDto)
                .toList();
        return new AdminPlaylistListResponse(true, list);
    }

    @Transactional
    public AdminPlaylistResponse createPlaylist(CreatePlaylistRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Playlist title is required");
        }
        Playlist p = new Playlist();
        p.setTitle(request.title().trim());
        p.setSubtitle(blank(request.subtitle(), ""));
        String type = blank(request.type(), blank(request.artworkType(), "featured"));
        p.setArtworkType(type);
        p.setCoverUrl(blank(request.coverUrl(), ""));
        p.setMadeForYou(Boolean.TRUE.equals(request.madeForYou()) || "ai".equalsIgnoreCase(type));
        p = playlistRepository.save(p);
        return new AdminPlaylistResponse(true, "Playlist created", toPlaylistDto(p));
    }

    public AdminPodcastListResponse listPodcasts() {
        List<AdminPodcastDto> list = podcastRepository.findAll().stream()
                .map(p -> new AdminPodcastDto(
                        p.publicId(),
                        p.getId(),
                        p.getTitle(),
                        p.getHost() != null ? p.getHost() : "",
                        p.getCategory() != null ? p.getCategory() : "",
                        p.getCoverUrl() != null ? p.getCoverUrl() : ""
                ))
                .toList();
        return new AdminPodcastListResponse(true, list);
    }

    @Transactional
    public SimpleMsg broadcast(BroadcastNotificationRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Notification title required");
        }
        String audience = blank(request.audience(), "everyone").toLowerCase(Locale.ROOT);
        String message = blank(request.message(), "");
        List<String> targets = new ArrayList<>();
        if ("specific".equals(audience) || "user".equals(audience)) {
            if (request.targetEmail() == null || request.targetEmail().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "targetEmail required for specific audience");
            }
            targets.add(request.targetEmail().trim());
        } else if ("premium".equals(audience)) {
            userRepository.findAll().stream().filter(User::isPremium).map(User::getEmail).forEach(targets::add);
            if (targets.isEmpty()) {
                return new SimpleMsg(false, "No premium users to notify");
            }
        } else {
            targets.add("global");
        }
        for (String key : targets) {
            notificationRepository.save(new AppNotification(
                    key, request.title().trim(), message, "admin", "#6B3CE9", "Just now"
            ));
        }
        return new SimpleMsg(true, "Notification saved for " + targets.size() + " target(s)");
    }

    public AnalyticsOverviewResponse analytics() {
        List<Track> tracks = trackRepository.findAll();
        List<Artist> artists = artistRepository.findAll();
        List<User> users = userRepository.findAll();
        long totalPlays = tracks.stream().mapToLong(t -> parsePlays(t.getPlaysCount())).sum();
        return new AnalyticsOverviewResponse(
                true,
                totalPlays,
                "—",
                "—",
                "—",
                "—",
                topSongs(tracks).stream().limit(10).toList(),
                topArtists(artists),
                List.of(),
                authProviderDistribution(users),
                usersCreatedLast7Days(users)
        );
    }

    public AiOverviewResponse aiOverview() {
        // No AI event table yet — return zeros only (no invented activity).
        return new AiOverviewResponse(
                true, 0, 0, 0, 0, 0, 0, "0", 0, List.of()
        );
    }

    public SystemSettingsResponse systemSettings() {
        Map<String, String> storage = new LinkedHashMap<>();
        storage.put("uploadDir", mediaStorageService.getRoot().toString());
        storage.put("publicBaseUrl", publicBaseUrl == null || publicBaseUrl.isBlank() ? "not-set" : publicBaseUrl);
        storage.put("bytesUsed", String.valueOf(folderSize(mediaStorageService.getRoot())));
        return new SystemSettingsResponse(
                true,
                "1.0.0",
                MAINTENANCE.get(),
                new LinkedHashMap<>(FEATURE_FLAGS),
                Map.of(
                        "chat", "local",
                        "dj", "local",
                        "mood", "local"
                ),
                storage
        );
    }

    public SystemSettingsResponse updateSystemSettings(UpdateSystemSettingsRequest request) {
        if (request != null) {
            if (request.maintenanceMode() != null) {
                MAINTENANCE.set(request.maintenanceMode());
            }
            if (request.featureFlags() != null) {
                FEATURE_FLAGS.putAll(request.featureFlags());
            }
        }
        return systemSettings();
    }

    public StorageOverviewResponse storage() {
        Path root = mediaStorageService.getRoot();
        long audio = folderSize(root.resolve("audio"));
        long images = folderSize(root.resolve("covers"));
        long total = folderSize(root);
        long other = Math.max(0, total - audio - images);
        List<NamedMetric> breakdown = new ArrayList<>();
        if (total > 0) {
            breakdown.add(new NamedMetric("Audio", formatBytes(audio), pctOf(audio, total)));
            breakdown.add(new NamedMetric("Images", formatBytes(images), pctOf(images, total)));
            breakdown.add(new NamedMetric("Other", formatBytes(other), pctOf(other, total)));
        }
        return new StorageOverviewResponse(
                true,
                formatBytes(audio),
                formatBytes(images),
                formatBytes(0),
                formatBytes(total),
                breakdown
        );
    }

    public SearchManagementResponse searchManagement() {
        // No search-log table — return empty real response.
        return new SearchManagementResponse(true, List.of(), List.of(), List.of());
    }

    public RecommendationConfigResponse recommendationConfig() {
        return new RecommendationConfigResponse(
                true,
                new LinkedHashMap<>(REC_WEIGHTS),
                "configured-weights",
                true,
                true
        );
    }

    public RecommendationConfigResponse updateRecommendation(RecommendationConfigResponse body) {
        if (body != null && body.weights() != null && !body.weights().isEmpty()) {
            REC_WEIGHTS.clear();
            REC_WEIGHTS.putAll(body.weights());
        }
        return recommendationConfig();
    }

    public ModerationQueueResponse moderation() {
        // No reports table — empty queues only.
        return new ModerationQueueResponse(true, List.of(), List.of(), List.of(), List.of());
    }

    public SubscriptionOverviewResponse subscriptions() {
        long premium = userRepository.findAll().stream().filter(User::isPremium).count();
        long total = userRepository.count();
        return new SubscriptionOverviewResponse(
                true,
                List.of(
                        new NamedMetric("Premium users", String.valueOf(premium), "isPremium=true"),
                        new NamedMetric("Free users", String.valueOf(Math.max(0, total - premium)), "isPremium=false")
                ),
                "0",
                "0",
                premium,
                List.of()
        );
    }

    public record SimpleMsg(boolean success, String message) {}

    private AdminUserDto toUserDto(User u) {
        return new AdminUserDto(
                "u_" + u.getId(),
                u.getId(),
                u.getName() != null ? u.getName() : "",
                u.getEmail(),
                u.getRole() != null ? u.getRole() : "USER",
                u.isPremium(),
                u.isEnabled(),
                u.isEmailVerified(),
                u.getProvider() != null ? u.getProvider() : "LOCAL",
                u.getCreatedAt() != null ? u.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE) : ""
        );
    }

    private AdminPlaylistDto toPlaylistDto(Playlist p) {
        String type = p.getArtworkType() != null && !p.getArtworkType().isBlank() ? p.getArtworkType() : "featured";
        if (p.isMadeForYou()) type = "ai";
        return new AdminPlaylistDto(
                p.publicId(),
                p.getId(),
                p.getTitle(),
                p.getSubtitle() != null ? p.getSubtitle() : "",
                p.getArtworkType() != null ? p.getArtworkType() : "",
                p.getCoverUrl() != null ? p.getCoverUrl() : "",
                p.isMadeForYou(),
                type
        );
    }

    private List<ChartPoint> usersCreatedLast7Days(List<User> users) {
        LocalDate today = LocalDate.now();
        List<ChartPoint> points = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = users.stream()
                    .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().toLocalDate().equals(day))
                    .count();
            points.add(new ChartPoint(day.getDayOfWeek().name().substring(0, 3), count));
        }
        return points;
    }

    private List<NamedMetric> authProviderDistribution(List<User> users) {
        if (users.isEmpty()) return List.of();
        Map<String, Long> counts = users.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getProvider() == null || u.getProvider().isBlank() ? "LOCAL" : u.getProvider().toUpperCase(Locale.ROOT),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        long total = users.size();
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new NamedMetric(e.getKey(), e.getValue() + " users", pctOf(e.getValue(), total)))
                .toList();
    }

    private List<NamedMetric> genreMetrics(List<Genre> genres, List<Artist> artists) {
        if (!genres.isEmpty()) {
            Map<String, Long> artistByGenre = artists.stream()
                    .collect(Collectors.groupingBy(
                            a -> a.getGenre() == null || a.getGenre().isBlank() ? "Unknown" : a.getGenre(),
                            Collectors.counting()
                    ));
            return genres.stream()
                    .sorted(Comparator.comparingInt(Genre::getSortOrder))
                    .map(g -> {
                        long n = artistByGenre.getOrDefault(g.getName(), 0L);
                        return new NamedMetric(g.getName(), n + " artists", "sort " + g.getSortOrder());
                    })
                    .toList();
        }
        return artists.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getGenre() == null || a.getGenre().isBlank() ? "Unknown" : a.getGenre(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new NamedMetric(e.getKey(), e.getValue() + " artists", ""))
                .toList();
    }

    private List<NamedMetric> topArtists(List<Artist> artists) {
        return artists.stream()
                .sorted(Comparator.comparingInt(Artist::getFollowersCount).reversed()
                        .thenComparing(Artist::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(10)
                .map(a -> new NamedMetric(
                        a.getName(),
                        a.getFollowersCount() + " followers",
                        a.isVerified() ? "verified" : ""
                ))
                .toList();
    }

    private List<NamedMetric> topSongs(List<Track> tracks) {
        return tracks.stream()
                .sorted(Comparator
                        .comparingLong((Track t) -> parsePlays(t.getPlaysCount())).reversed()
                        .thenComparing(Track::getId, Comparator.reverseOrder()))
                .limit(10)
                .map(t -> new NamedMetric(
                        t.getTitle(),
                        t.getPlaysCount() != null && !t.getPlaysCount().isBlank() ? t.getPlaysCount() : "0",
                        t.getArtistName() != null ? t.getArtistName() : ""
                ))
                .toList();
    }

    private List<ActivityItem> recentActivities(List<Track> tracks, List<User> users) {
        List<ActivityItem> items = new ArrayList<>();
        tracks.stream().sorted(Comparator.comparing(Track::getId).reversed()).limit(5).forEach(t ->
                items.add(new ActivityItem(
                        "track_" + t.getId(),
                        "Track",
                        t.getTitle() + " · " + (t.getArtistName() != null ? t.getArtistName() : ""),
                        "#" + t.getId(),
                        "upload"
                )));
        users.stream()
                .filter(u -> u.getCreatedAt() != null)
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .limit(5)
                .forEach(u -> items.add(new ActivityItem(
                        "user_" + u.getId(),
                        "User registered",
                        u.getEmail(),
                        u.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        "users"
                )));
        notificationRepository.findAll().stream()
                .sorted(Comparator.comparing(AppNotification::getId).reversed())
                .limit(5)
                .forEach(n -> items.add(new ActivityItem(
                        "notif_" + n.getId(),
                        "Notification",
                        n.getTitle(),
                        n.getUserKey() != null ? n.getUserKey() : "global",
                        "notification"
                )));
        return items;
    }

    private List<ActivityItem> recentUploads(List<Track> tracks) {
        return tracks.stream()
                .sorted(Comparator.comparing(Track::getId).reversed())
                .limit(10)
                .map(t -> new ActivityItem(
                        "ru_" + t.getId(),
                        t.getTitle(),
                        t.getArtistName() != null ? t.getArtistName() : "Unknown",
                        t.isTrending() ? "trending" : "track",
                        "upload"
                ))
                .toList();
    }

    private static boolean isToday(LocalDateTime dt) {
        return dt != null && dt.toLocalDate().equals(LocalDate.now());
    }

    private static long parsePlays(String raw) {
        if (raw == null || raw.isBlank()) return 0L;
        String n = raw.trim().toUpperCase(Locale.ROOT).replace(",", "").replace(" ", "");
        try {
            if (n.endsWith("M")) return Math.round(Double.parseDouble(n.substring(0, n.length() - 1)) * 1_000_000d);
            if (n.endsWith("K")) return Math.round(Double.parseDouble(n.substring(0, n.length() - 1)) * 1_000d);
            return Long.parseLong(n.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0L;
        }
    }

    private static long folderSize(Path path) {
        if (path == null || !Files.exists(path)) return 0L;
        AtomicLong total = new AtomicLong();
        try (Stream<Path> walk = Files.walk(path)) {
            walk.filter(Files::isRegularFile).forEach(p -> {
                try {
                    total.addAndGet(Files.size(p));
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
        return total.get();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format(Locale.US, "%.2f MB", mb);
        return String.format(Locale.US, "%.2f GB", mb / 1024.0);
    }

    private static String pctOf(long part, long total) {
        if (total <= 0) return "0%";
        return Math.round(part * 100.0 / total) + "%";
    }

    private static String blank(String v, String fallback) {
        return v == null || v.isBlank() ? fallback : v;
    }
}

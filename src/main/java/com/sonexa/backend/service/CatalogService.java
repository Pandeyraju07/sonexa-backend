package com.sonexa.backend.service;

import com.sonexa.backend.model.dto.CatalogDtos.*;
import com.sonexa.backend.model.entity.*;
import com.sonexa.backend.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final MoodRepository moodRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final PodcastRepository podcastRepository;
    private final PodcastEpisodeRepository podcastEpisodeRepository;
    private final AppNotificationRepository notificationRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final UserLibraryItemRepository libraryItemRepository;
    private final UserRepository userRepository;
    private final LyricsService lyricsService;
    private final YouTubeDiscoveryService youTubeDiscoveryService;

    public CatalogService(
            TrackRepository trackRepository,
            AlbumRepository albumRepository,
            ArtistRepository artistRepository,
            GenreRepository genreRepository,
            MoodRepository moodRepository,
            PlaylistRepository playlistRepository,
            PlaylistTrackRepository playlistTrackRepository,
            PodcastRepository podcastRepository,
            PodcastEpisodeRepository podcastEpisodeRepository,
            AppNotificationRepository notificationRepository,
            UserPreferenceRepository preferenceRepository,
            UserLibraryItemRepository libraryItemRepository,
            UserRepository userRepository,
            LyricsService lyricsService,
            YouTubeDiscoveryService youTubeDiscoveryService
    ) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.genreRepository = genreRepository;
        this.moodRepository = moodRepository;
        this.playlistRepository = playlistRepository;
        this.playlistTrackRepository = playlistTrackRepository;
        this.podcastRepository = podcastRepository;
        this.podcastEpisodeRepository = podcastEpisodeRepository;
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.userRepository = userRepository;
        this.lyricsService = lyricsService;
        this.youTubeDiscoveryService = youTubeDiscoveryService;
    }

    public String currentUserKey() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "guest";
    }

    private Set<String> likedIds(String userKey) {
        return libraryItemRepository.findByUserKeyAndLikedTrue(userKey).stream()
                .map(UserLibraryItem::getTrackPublicId)
                .collect(Collectors.toSet());
    }

    public TrackDto toTrackDto(Track t, Set<String> liked) {
        return new TrackDto(
                t.publicId(), t.getTitle(), t.getArtistName(),
                t.getAlbumTitle() != null ? t.getAlbumTitle() : "",
                t.getDurationMs() != null ? t.getDurationMs() : 0L,
                t.getAudioUrl() != null ? t.getAudioUrl() : "",
                t.getCoverUrl() != null ? t.getCoverUrl() : "",
                t.getPlaysCount() != null ? t.getPlaysCount() : "",
                liked.contains(t.publicId())
        );
    }

    public AlbumDto toAlbumDto(Album a) {
        return new AlbumDto(a.publicId(), a.getTitle(), a.getArtistName(),
                a.getYear() != null ? a.getYear() : "",
                a.getCoverUrl() != null ? a.getCoverUrl() : "",
                a.getTrackCount());
    }

    public PlaylistDto toPlaylistDto(Playlist p) {
        return new PlaylistDto(p.publicId(), p.getTitle(),
                p.getSubtitle() != null ? p.getSubtitle() : "",
                p.getArtworkType() != null ? p.getArtworkType() : "",
                p.getCoverUrl() != null ? p.getCoverUrl() : "");
    }

    public ArtistDto toArtistDto(Artist a) {
        return new ArtistDto(a.publicId(), a.getName(),
                a.getGenre() != null ? a.getGenre() : "",
                a.getBio() != null ? a.getBio() : "",
                a.getImageUrl() != null ? a.getImageUrl() : "",
                a.getColor1() != null ? a.getColor1() : "#6B3CE9",
                a.getColor2() != null ? a.getColor2() : "#9825DD",
                a.getFollowersCount(), a.isVerified());
    }

    private Long parseId(String publicId, String prefix) {
        if (publicId == null) return null;
        String raw = publicId.startsWith(prefix) ? publicId.substring(prefix.length()) : publicId;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public HomeFeedResponse homeFeed() {
        String userKey = currentUserKey();
        Set<String> liked = likedIds(userKey);
        // Newest first so admin uploads appear immediately
        List<Track> newest = trackRepository.findAllByOrderByIdDesc();
        List<TrackDto> continueListening = newest.stream().limit(8).map(t -> toTrackDto(t, liked)).toList();
        List<TrackDto> trending = trackRepository.findByTrendingTrue().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .map(t -> toTrackDto(t, liked))
                .toList();
        if (trending.isEmpty()) {
            trending = newest.stream().limit(20).map(t -> toTrackDto(t, liked)).toList();
        }
        // Always prepend newest uploads into trending if not already present
        LinkedHashMap<String, TrackDto> merged = new LinkedHashMap<>();
        newest.stream().limit(10).map(t -> toTrackDto(t, liked)).forEach(t -> merged.put(t.id(), t));
        trending.forEach(t -> merged.putIfAbsent(t.id(), t));
        trending = new ArrayList<>(merged.values());

        List<AlbumDto> albums = albumRepository.findAll().stream().map(this::toAlbumDto).toList();
        List<PlaylistDto> playlists = playlistRepository.findByMadeForYouTrue().stream()
                .map(this::toPlaylistDto).toList();
        return new HomeFeedResponse(true, continueListening, trending, albums, playlists);
    }

    public TrendingResponse trending() {
        Set<String> liked = likedIds(currentUserKey());
        List<TrackDto> tracks = trackRepository.findByTrendingTrue().stream()
                .map(t -> toTrackDto(t, liked)).toList();
        if (tracks.isEmpty()) {
            tracks = trackRepository.findAll().stream().map(t -> toTrackDto(t, liked)).toList();
        }
        return new TrendingResponse(true, tracks);
    }

    public SearchResponse search(String q) {
        Set<String> liked = likedIds(currentUserKey());
        if (q == null || q.isBlank()) {
            return new SearchResponse(true,
                    trackRepository.findAll().stream().limit(5).map(t -> toTrackDto(t, liked)).toList(),
                    albumRepository.findAll().stream().map(this::toAlbumDto).toList(),
                    artistRepository.findAll().stream().map(Artist::getName).toList());
        }
        List<TrackDto> nativeTracks = trackRepository
                .findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(q, q)
                .stream().map(t -> toTrackDto(t, liked)).toList();
        List<AlbumDto> albums = albumRepository
                .findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(q, q)
                .stream().map(this::toAlbumDto).toList();
        List<String> artists = artistRepository.findByNameContainingIgnoreCase(q).stream()
                .map(Artist::getName).toList();

        List<TrackDto> ytTracks = youTubeDiscoveryService != null 
                ? youTubeDiscoveryService.searchYouTubeMusic(q, 10) 
                : Collections.emptyList();

        List<TrackDto> combinedTracks = new ArrayList<>(nativeTracks);
        combinedTracks.addAll(ytTracks);

        return new SearchResponse(true, combinedTracks, albums, artists);
    }

    public TrackDetailResponse trackDetail(String id) {
        if (id != null && id.startsWith("yt_")) {
            String videoId = id.substring("yt_".length());
            TrackDto ytTrack = new TrackDto(
                    id, "YouTube Music Track", "YouTube Artist", "YouTube Music", 0L,
                    "https://www.youtube.com/watch?v=" + videoId,
                    "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg",
                    "YouTube", false, "YOUTUBE", videoId, videoId,
                    "https://www.youtube.com/watch?v=" + videoId, true,
                    "OFFICIAL_IFRAME_PLAYER", null, true
            );
            return new TrackDetailResponse(true, ytTrack);
        }

        Long dbId = parseId(id, "tr_");
        Track track = dbId != null ? trackRepository.findById(dbId).orElse(null) : null;
        if (track == null && !trackRepository.findAll().isEmpty()) {
            track = trackRepository.findAll().get(0);
        }
        if (track == null) {
            return new TrackDetailResponse(false, null);
        }
        return new TrackDetailResponse(true, toTrackDto(track, likedIds(currentUserKey())));
    }

    public AlbumDetailResponse albumDetail(String id) {
        Long dbId = parseId(id, "alb_");
        Album album = dbId != null ? albumRepository.findById(dbId).orElse(null) : null;
        if (album == null && !albumRepository.findAll().isEmpty()) {
            album = albumRepository.findAll().get(0);
        }
        if (album == null) {
            return new AlbumDetailResponse(false, null, List.of());
        }
        Set<String> liked = likedIds(currentUserKey());
        List<TrackDto> tracks = trackRepository.findByAlbumId(album.getId()).stream()
                .map(t -> toTrackDto(t, liked)).toList();
        if (tracks.isEmpty()) {
            tracks = trackRepository.findAll().stream().limit(5).map(t -> toTrackDto(t, liked)).toList();
        }
        return new AlbumDetailResponse(true, toAlbumDto(album), tracks);
    }

    public PlaylistDetailResponse playlistDetail(String id) {
        Long dbId = parseId(id, "pl_");
        Playlist playlist = dbId != null ? playlistRepository.findById(dbId).orElse(null) : null;
        if (playlist == null && !playlistRepository.findAll().isEmpty()) {
            playlist = playlistRepository.findAll().get(0);
        }
        if (playlist == null) {
            return new PlaylistDetailResponse(false, null, List.of());
        }
        Set<String> liked = likedIds(currentUserKey());
        List<TrackDto> tracks = playlistTrackRepository.findByPlaylistIdOrderBySortOrderAsc(playlist.getId())
                .stream()
                .map(pt -> trackRepository.findById(pt.getTrackId()).orElse(null))
                .filter(Objects::nonNull)
                .map(t -> toTrackDto(t, liked))
                .toList();
        return new PlaylistDetailResponse(true, toPlaylistDto(playlist), tracks);
    }

    public ArtistDetailResponse artistDetail(String id) {
        Long dbId = parseId(id, "art_");
        Artist artist = dbId != null ? artistRepository.findById(dbId).orElse(null) : null;
        if (artist == null && !artistRepository.findAll().isEmpty()) {
            artist = artistRepository.findAll().get(0);
        }
        if (artist == null) {
            return new ArtistDetailResponse(false, null, List.of(), List.of());
        }
        final Artist resolvedArtist = artist;
        Set<String> liked = likedIds(currentUserKey());
        List<TrackDto> tracks = trackRepository.findByArtistId(resolvedArtist.getId()).stream()
                .map(t -> toTrackDto(t, liked)).toList();
        List<AlbumDto> albums = albumRepository.findAll().stream()
                .filter(a -> resolvedArtist.getName().equalsIgnoreCase(a.getArtistName()))
                .map(this::toAlbumDto).toList();
        return new ArtistDetailResponse(true, toArtistDto(resolvedArtist), tracks, albums);
    }

    public QueueResponse queue() {
        Set<String> liked = likedIds(currentUserKey());
        List<TrackDto> all = trackRepository.findAll().stream().map(t -> toTrackDto(t, liked)).toList();
        TrackDto now = all.isEmpty() ? null : all.get(0);
        String source = now != null && now.album() != null && !now.album().isBlank() ? now.album() : "Queue";
        return new QueueResponse(true, now, all, source);
    }

    @Transactional
    public LyricsResponse trackLyrics(String id) {
        Long dbId = parseId(id, "tr_");
        Track track = dbId != null ? trackRepository.findById(dbId).orElse(null) : null;
        if (track == null) {
            return new LyricsResponse(false, id, false, List.of(), "Lyrics unavailable for this track.", "none");
        }

        String trackId = "tr_" + track.getId();
        LyricsService.FetchedLyrics fetched = null;

        if (track.getLyrics() != null && !track.getLyrics().isBlank()) {
            fetched = lyricsService.parseCachedPayload(track.getLyrics());
        }

        if (fetched == null || fetched.plainText() == null || fetched.plainText().isBlank()) {
            fetched = lyricsService.fetch(
                    track.getTitle(),
                    track.getArtistName(),
                    track.getAlbumTitle(),
                    track.getDurationMs()
            );
            String cache = lyricsService.toCachePayload(fetched);
            if (cache != null) {
                track.setLyrics(cache);
                trackRepository.save(track);
            }
        }

        if (fetched == null || ((fetched.plainText() == null || fetched.plainText().isBlank())
                && (fetched.lines() == null || fetched.lines().isEmpty()))) {
            return new LyricsResponse(false, trackId, false, List.of(),
                    "No public lyrics found for \"" + track.getTitle() + "\" yet. "
                            + "Synced lyrics appear automatically for catalog matches (LRCLIB / Musixmatch).",
                    "none");
        }

        return new LyricsResponse(
                true,
                trackId,
                fetched.synced(),
                fetched.lines() != null ? fetched.lines() : List.of(),
                fetched.plainText() != null ? fetched.plainText() : "",
                fetched.source() != null ? fetched.source() : "unknown"
        );
    }

    public GenreListResponse genres() {
        List<GenreDto> list = genreRepository.findAllByOrderBySortOrderAsc().stream()
                .map(g -> new GenreDto("g_" + g.getId(), g.getName(), g.getColor1(), g.getColor2(),
                        g.getImageUrl() != null ? g.getImageUrl() : ""))
                .toList();
        return new GenreListResponse(true, list);
    }

    public ArtistListResponse artists() {
        return new ArtistListResponse(true, artistRepository.findAll().stream().map(this::toArtistDto).toList());
    }

    public MoodListResponse moods() {
        List<MoodDto> list = moodRepository.findAllByOrderBySortOrderAsc().stream()
                .map(m -> new MoodDto("m_" + m.getId(), m.getName(), m.getIconKey(), m.getColorHex()))
                .toList();
        return new MoodListResponse(true, list);
    }

    public PodcastListResponse podcasts() {
        List<PodcastDto> list = podcastRepository.findAll().stream()
                .map(p -> new PodcastDto(p.publicId(), p.getTitle(), p.getHost(), p.getDescription(),
                        p.getCoverUrl(), p.getCategory()))
                .toList();
        return new PodcastListResponse(true, list);
    }

    public PodcastDetailResponse podcastDetail(String id) {
        Long dbId = parseId(id, "pod_");
        Podcast podcast = dbId != null ? podcastRepository.findById(dbId).orElse(null) : null;
        if (podcast == null && !podcastRepository.findAll().isEmpty()) {
            podcast = podcastRepository.findAll().get(0);
        }
        if (podcast == null) {
            return new PodcastDetailResponse(false, null, List.of());
        }
        List<PodcastEpisodeDto> episodes = podcastEpisodeRepository
                .findByPodcastIdOrderByEpisodeNumberAsc(podcast.getId()).stream()
                .map(e -> new PodcastEpisodeDto(e.publicId(), e.getTitle(), e.getDescription(),
                        e.getDurationLabel(), e.getAudioUrl(), e.getEpisodeNumber()))
                .toList();
        PodcastDto dto = new PodcastDto(podcast.publicId(), podcast.getTitle(), podcast.getHost(),
                podcast.getDescription(), podcast.getCoverUrl(), podcast.getCategory());
        return new PodcastDetailResponse(true, dto, episodes);
    }

    public NotificationListResponse notifications() {
        List<AppNotification> items = notificationRepository.findByUserKeyOrderByCreatedAtDesc("global");
        List<NotificationDto> dtos = items.stream()
                .map(n -> new NotificationDto(n.publicId(), n.getTitle(), n.getMessage(),
                        n.getIconKey(), n.getColorHex(), n.getTimeAgo(), n.isReadFlag()))
                .toList();
        return new NotificationListResponse(true, dtos);
    }

    @Transactional
    public ToggleLikeResponse toggleLike(String trackId) {
        String userKey = currentUserKey();
        UserLibraryItem item = libraryItemRepository.findByUserKeyAndTrackPublicId(userKey, trackId)
                .orElseGet(() -> new UserLibraryItem(userKey, trackId, false));
        item.setLiked(!item.isLiked());
        item.setUpdatedAt(LocalDateTime.now());
        libraryItemRepository.save(item);
        return new ToggleLikeResponse(true, trackId, item.isLiked(),
                item.isLiked() ? "Added to Liked Songs" : "Removed from Liked Songs");
    }

    public UserLibraryResponse library() {
        String userKey = currentUserKey();
        Set<String> liked = likedIds(userKey);
        List<TrackDto> likedSongs = trackRepository.findAll().stream()
                .filter(t -> liked.contains(t.publicId()))
                .map(t -> toTrackDto(t, liked))
                .toList();
        if (likedSongs.isEmpty()) {
            likedSongs = trackRepository.findAll().stream().limit(2).map(t -> toTrackDto(t, liked)).toList();
        }
        return new UserLibraryResponse(true, likedSongs, albumRepository.findAll().stream().limit(3).map(this::toAlbumDto).toList());
    }

    public UserProfileResponse profile() {
        String userKey = currentUserKey();
        Map<String, Object> user = new LinkedHashMap<>();
        Optional<User> dbUser = userRepository.findByEmail(userKey);
        if (dbUser.isPresent()) {
            User u = dbUser.get();
            user.put("id", "usr_" + u.getId());
            user.put("name", u.getName() != null ? u.getName() : "Sonexa Listener");
            user.put("email", u.getEmail());
            user.put("profilePicUrl", u.getProfilePicUrl() != null ? u.getProfilePicUrl()
                    : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300");
            user.put("isPremium", u.isPremium());
            user.put("isEmailVerified", u.isEmailVerified());
            user.put("followersCount", u.getFollowersCount());
            user.put("followingCount", u.getFollowingCount());
        } else {
            user.put("id", "usr_guest");
            user.put("name", "Sonexa Listener");
            user.put("email", userKey.equals("guest") ? "guest@sonexa.ai" : userKey);
            user.put("profilePicUrl", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300");
            user.put("isPremium", false);
            user.put("isEmailVerified", false);
            user.put("followersCount", 0);
            user.put("followingCount", 0);
        }
        preferenceRepository.findByUserKeyAndPrefType(userKey, "HANDLE").stream().findFirst()
                .ifPresent(p -> user.put("handle", p.getPrefValue()));
        preferenceRepository.findByUserKeyAndPrefType(userKey, "BIO").stream().findFirst()
                .ifPresent(p -> user.put("bio", p.getPrefValue()));
        return new UserProfileResponse(true, user);
    }

    @Transactional
    public SimpleSuccessResponse updateProfile(UpdateProfileRequest request) {
        String userKey = currentUserKey();
        userRepository.findByEmail(userKey).ifPresent(u -> {
            if (request.name() != null && !request.name().isBlank()) u.setName(request.name());
            if (request.profilePicUrl() != null) u.setProfilePicUrl(request.profilePicUrl());
            userRepository.save(u);
        });
        if (request.bio() != null) {
            replacePrefs(userKey, "BIO", List.of(request.bio()));
        }
        return new SimpleSuccessResponse(true, "Profile updated successfully");
    }

    @Transactional
    public SaveListResponse savePrefs(String type, List<String> items) {
        String userKey = currentUserKey();
        if (items == null || items.isEmpty()) {
            return new SaveListResponse(false, "Please select at least one item", List.of(), 0);
        }
        replacePrefs(userKey, type, items);
        return new SaveListResponse(true, type + " saved successfully", items, items.size());
    }

    @Transactional
    public void replacePrefs(String userKey, String type, List<String> items) {
        preferenceRepository.deleteByUserKeyAndPrefType(userKey, type);
        for (String item : items) {
            if (item != null && !item.isBlank()) {
                preferenceRepository.save(new UserPreference(userKey, type, item.trim()));
            }
        }
    }

    public List<String> getPrefs(String type) {
        return preferenceRepository.findByUserKeyAndPrefType(currentUserKey(), type).stream()
                .map(UserPreference::getPrefValue).toList();
    }

    public Map<String, Object> permissionPrefs() {
        List<String> flags = getPrefs("PERMISSION");
        boolean notifications = flags.contains("NOTIFICATIONS_ENABLED");
        boolean downloads = flags.contains("DOWNLOADS_ENABLED");
        return Map.of(
                "success", true,
                "notificationsEnabled", notifications,
                "downloadsEnabled", downloads
        );
    }

    @Transactional
    public Map<String, Object> savePermissionPrefs(boolean notificationsEnabled, boolean downloadsEnabled) {
        String userKey = currentUserKey();
        List<String> values = new ArrayList<>();
        if (notificationsEnabled) values.add("NOTIFICATIONS_ENABLED");
        if (downloadsEnabled) values.add("DOWNLOADS_ENABLED");
        if (values.isEmpty()) {
            preferenceRepository.deleteByUserKeyAndPrefType(userKey, "PERMISSION");
        } else {
            replacePrefs(userKey, "PERMISSION", values);
        }
        return Map.of(
                "success", true,
                "message", "Permission preferences saved",
                "notificationsEnabled", notificationsEnabled,
                "downloadsEnabled", downloadsEnabled
        );
    }

    @Transactional
    public SimpleSuccessResponse createProfile(ProfileCreateRequest request) {
        String userKey = currentUserKey();
        if (request.displayName() != null && !request.displayName().isBlank()) {
            replacePrefs(userKey, "DISPLAY_NAME", List.of(request.displayName()));
            userRepository.findByEmail(userKey).ifPresent(u -> {
                u.setName(request.displayName());
                userRepository.save(u);
            });
        }
        if (request.handle() != null && !request.handle().isBlank()) {
            replacePrefs(userKey, "HANDLE", List.of(request.handle()));
        }
        return new SimpleSuccessResponse(true, "Profile created successfully");
    }

    public PremiumResponse premium() {
        boolean isPremium = userRepository.findByEmail(currentUserKey()).map(User::isPremium).orElse(false);
        List<Map<String, Object>> plans = List.of(
                Map.of("id", "individual", "name", "Individual", "price", "₹119/mo", "description", "1 account"),
                Map.of("id", "duo", "name", "Duo", "price", "₹149/mo", "description", "2 accounts"),
                Map.of("id", "family", "name", "Family", "price", "₹179/mo", "description", "Up to 6 accounts")
        );
        List<String> benefits = List.of(
                "Ad-free listening", "Offline downloads", "Hi-Fi lossless audio",
                "AI Signature mixes", "Unlimited skips"
        );
        return new PremiumResponse(true, isPremium, plans, benefits);
    }

    @Transactional
    public SimpleSuccessResponse subscribe(String planId) {
        userRepository.findByEmail(currentUserKey()).ifPresent(u -> {
            u.setPremium(true);
            userRepository.save(u);
        });
        replacePrefs(currentUserKey(), "PREMIUM_PLAN", List.of(planId != null ? planId : "individual"));
        return new SimpleSuccessResponse(true, "Subscribed to Sonexa Premium");
    }

    public SettingsResponse settings() {
        Map<String, Object> settings = defaultSettings();
        // Merge every persisted SETTING_* preference for this user
        preferenceRepository.findByUserKeyAndPrefTypeStartingWith(currentUserKey(), "SETTING_")
                .forEach(pref -> {
                    String key = pref.getPrefType().substring("SETTING_".length());
                    settings.put(key, coerceSettingValue(key, pref.getPrefValue()));
                });
        // Backward-compat: older audioQuality rows
        getPrefs("SETTINGS_AUDIO").stream().findFirst().ifPresent(v -> settings.put("audioQuality", v));
        List<String> languages = getPrefs("LANGUAGE");
        if (!languages.isEmpty()) {
            settings.put("language", String.join(" • ", languages));
            settings.put("languages", languages);
        }
        List<String> devices = getPrefs("CONNECTED_DEVICE");
        settings.put("connectedDevices", devices.isEmpty()
                ? List.of("This Android phone", "Bluetooth earbuds")
                : devices);
        return new SettingsResponse(true, settings);
    }

    @Transactional
    public SimpleSuccessResponse updateSettings(UpdateSettingsRequest request) {
        if (request == null || request.settings() == null || request.settings().isEmpty()) {
            return new SimpleSuccessResponse(false, "No settings provided");
        }
        String userKey = currentUserKey();
        request.settings().forEach((key, value) -> {
            if (key == null || key.isBlank() || value == null) return;
            if ("connectedDevices".equals(key) && value instanceof List<?> list) {
                replacePrefs(userKey, "CONNECTED_DEVICE",
                        list.stream().map(String::valueOf).filter(s -> !s.isBlank()).toList());
                return;
            }
            if ("languages".equals(key) && value instanceof List<?> list) {
                List<String> langs = list.stream().map(String::valueOf).filter(s -> !s.isBlank()).toList();
                replacePrefs(userKey, "LANGUAGE", langs);
                replacePrefs(userKey, "SETTING_language",
                        List.of(String.join(" • ", langs)));
                return;
            }
            String type = "SETTING_" + key;
            replacePrefs(userKey, type, List.of(String.valueOf(value)));
            if ("audioQuality".equals(key)) {
                replacePrefs(userKey, "SETTINGS_AUDIO", List.of(String.valueOf(value)));
            }
        });
        return new SimpleSuccessResponse(true, "Settings updated");
    }

    private Map<String, Object> defaultSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("audioQuality", "High");
        settings.put("downloadQuality", "High");
        settings.put("downloadOverWifiOnly", true);
        settings.put("crossfade", false);
        settings.put("crossfadeSeconds", 0);
        settings.put("normalizeVolume", true);
        settings.put("explicitContent", false);
        settings.put("gaplessPlayback", true);
        settings.put("language", "English (India)");
        settings.put("languages", List.of("English (India)", "Hindi", "Punjabi"));
        settings.put("theme", "Dark");
        settings.put("accentStyle", "Glassmorphism");
        settings.put("pushNotifications", true);
        settings.put("friendActivity", true);
        settings.put("newReleaseAlerts", true);
        settings.put("aiSensitivity", "High");
        settings.put("aiVoiceModel", "Sonexa Voice v2.4");
        settings.put("smartLyrics", true);
        settings.put("dataSharing", false);
        settings.put("twoFactorEnabled", false);
        settings.put("showActiveSessions", true);
        settings.put("connectedDevices", List.of("This Android phone", "Bluetooth earbuds"));
        settings.put("appVersion", "2.4.0");
        return settings;
    }

    private Object coerceSettingValue(String key, String raw) {
        if (raw == null) return null;
        Set<String> boolKeys = Set.of(
                "downloadOverWifiOnly", "crossfade", "normalizeVolume", "explicitContent",
                "gaplessPlayback", "pushNotifications", "friendActivity", "newReleaseAlerts",
                "smartLyrics", "dataSharing", "twoFactorEnabled", "showActiveSessions"
        );
        if (boolKeys.contains(key)) {
            return Boolean.parseBoolean(raw);
        }
        if ("crossfadeSeconds".equals(key)) {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return raw;
    }

    public AiSignatureResponse aiSignature(AiSignatureRequest request) {
        Set<String> liked = likedIds(currentUserKey());
        String mood = request != null && request.mood() != null && !request.mood().isBlank()
                ? request.mood() : "Neon";
        List<TrackDto> recs = trackRepository.findAll().stream().limit(4).map(t -> toTrackDto(t, liked)).toList();
        return new AiSignatureResponse(true, "ai_sig_" + System.currentTimeMillis(),
                "AI Signature: " + mood.toUpperCase() + " VIBE",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                128, "F# Minor", recs);
    }

    public AiChatResponse aiChat(AiChatRequest request) {
        String msg = request != null && request.message() != null ? request.message() : "";
        return new AiChatResponse(true,
                "Sonexa AI: Based on \"" + msg + "\", try a chill lo-fi mix or Energy Boost playlist.");
    }
}

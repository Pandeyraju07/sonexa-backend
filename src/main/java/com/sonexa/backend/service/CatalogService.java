package com.sonexa.backend.service;

import com.sonexa.backend.constant.ErrorCode;
import com.sonexa.backend.exception.BusinessException;
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

    public String requireAuthenticatedUserKey() {
        String key = currentUserKey();
        if ("guest".equals(key)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return key;
    }

    private Playlist requireOwnedPlaylist(String id) {
        Long dbId = parseId(id, "pl_");
        if (dbId == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Playlist not found");
        }
        String userKey = requireAuthenticatedUserKey();
        return playlistRepository.findByIdAndUserKey(dbId, userKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Playlist not found"));
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
        List<Track> newest = trackRepository.findTop50ByOrderByIdDesc();
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
            tracks = trackRepository.findTop20ByOrderByIdDesc().stream()
                    .map(t -> toTrackDto(t, liked)).toList();
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
        if (track == null) {
            return new TrackDetailResponse(false, null);
        }
        return new TrackDetailResponse(true, toTrackDto(track, likedIds(currentUserKey())));
    }

    public AlbumDetailResponse albumDetail(String id) {
        Long dbId = parseId(id, "alb_");
        Album album = dbId != null ? albumRepository.findById(dbId).orElse(null) : null;
        if (album == null) {
            return new AlbumDetailResponse(false, null, List.of());
        }
        Set<String> liked = likedIds(currentUserKey());
        List<TrackDto> tracks = trackRepository.findByAlbumId(album.getId()).stream()
                .map(t -> toTrackDto(t, liked)).toList();
        return new AlbumDetailResponse(true, toAlbumDto(album), tracks);
    }

    public PlaylistDetailResponse playlistDetail(String id) {
        Long dbId = parseId(id, "pl_");
        Playlist playlist = dbId != null ? playlistRepository.findById(dbId).orElse(null) : null;
        if (playlist == null) {
            return new PlaylistDetailResponse(false, null, List.of());
        }
        String userKey = currentUserKey();
        if (playlist.isPrivate() && (playlist.getUserKey() == null || !playlist.getUserKey().equals(userKey))) {
            return new PlaylistDetailResponse(false, null, List.of());
        }
        Set<String> liked = likedIds(userKey);
        List<PlaylistTrack> pts = playlistTrackRepository.findByPlaylistIdOrderBySortOrderAsc(playlist.getId());
        List<TrackDto> tracks = new ArrayList<>();
        for (PlaylistTrack pt : pts) {
            if (pt.getTrackId() != null) {
                Track t = trackRepository.findById(pt.getTrackId()).orElse(null);
                if (t != null) {
                    tracks.add(toTrackDto(t, liked));
                    continue;
                }
            }
            if (pt.getTrackPublicId() != null && !pt.getTrackPublicId().isBlank()) {
                boolean isLiked = liked.contains(pt.getTrackPublicId());
                tracks.add(new TrackDto(
                        pt.getTrackPublicId(),
                        pt.getTrackTitle() != null ? pt.getTrackTitle() : "Track",
                        pt.getTrackArtist() != null ? pt.getTrackArtist() : "Artist",
                        pt.getTrackAlbum() != null ? pt.getTrackAlbum() : "",
                        pt.getDurationMs() != null ? pt.getDurationMs() : 0L,
                        pt.getAudioUrl() != null ? pt.getAudioUrl() : "",
                        pt.getCoverUrl() != null ? pt.getCoverUrl() : "",
                        "10K", isLiked
                ));
            }
        }
        if (tracks.isEmpty() && playlist.isMadeForYou()) {
            tracks = trackRepository.findAll().stream().limit(10).map(t -> toTrackDto(t, liked)).toList();
        }
        return new PlaylistDetailResponse(true, toPlaylistDto(playlist), tracks);
    }

    public UserPlaylistsResponse getUserPlaylists() {
        String userKey = currentUserKey();
        List<Playlist> userPlaylists = playlistRepository.findByUserKeyOrderByIdDesc(userKey);
        List<PlaylistDto> dtos = new ArrayList<>(userPlaylists.stream().map(this::toPlaylistDto).toList());
        return new UserPlaylistsResponse(true, dtos);
    }

    @Transactional
    public PlaylistDto createPlaylist(CreatePlaylistRequest request) {
        String userKey = requireAuthenticatedUserKey();
        String title = (request != null && request.title() != null && !request.title().isBlank())
                ? request.title().trim() : "My Playlist";
        String description = (request != null && request.description() != null) ? request.description().trim() : "";
        String coverUrl = (request != null && request.coverUrl() != null && !request.coverUrl().isBlank())
                ? request.coverUrl().trim()
                : "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500";

        String creatorName = userRepository.findByEmail(userKey)
                .map(User::getName)
                .filter(n -> !n.isBlank())
                .orElseGet(() -> userKey.contains("@") ? userKey.split("@")[0] : "Listener");

        Playlist playlist = new Playlist(title, description, coverUrl, userKey, creatorName);
        if (request != null && request.isPrivate() != null) {
            playlist.setPrivate(request.isPrivate());
        }
        playlist = playlistRepository.save(playlist);
        return toPlaylistDto(playlist);
    }

    @Transactional
    public PlaylistDto updatePlaylist(String id, UpdatePlaylistRequest request) {
        Playlist playlist = requireOwnedPlaylist(id);

        if (request != null) {
            if (request.title() != null && !request.title().isBlank()) playlist.setTitle(request.title().trim());
            if (request.description() != null) playlist.setSubtitle(request.description().trim());
            if (request.coverUrl() != null && !request.coverUrl().isBlank()) playlist.setCoverUrl(request.coverUrl().trim());
            if (request.isPrivate() != null) playlist.setPrivate(request.isPrivate());
            if (request.isPinned() != null) playlist.setPinned(request.isPinned());
        }
        playlist.setUpdatedAt(LocalDateTime.now());
        playlist = playlistRepository.save(playlist);
        return toPlaylistDto(playlist);
    }

    @Transactional
    public SimpleSuccessResponse deletePlaylist(String id) {
        Playlist playlist = requireOwnedPlaylist(id);
        playlistTrackRepository.deleteByPlaylistId(playlist.getId());
        playlistRepository.deleteById(playlist.getId());
        return new SimpleSuccessResponse(true, "Playlist deleted successfully");
    }

    @Transactional
    public SimpleSuccessResponse addTrackToPlaylist(String id, AddTrackToPlaylistRequest request) {
        Playlist playlist = requireOwnedPlaylist(id);
        Long dbId = playlist.getId();
        if (request == null || request.trackId() == null || request.trackId().isBlank()) {
            return new SimpleSuccessResponse(false, "trackId is required");
        }

        String trackPublicId = request.trackId();
        if (playlistTrackRepository.findByPlaylistIdAndTrackPublicId(dbId, trackPublicId).isPresent()) {
            return new SimpleSuccessResponse(true, "Track already in playlist");
        }

        int nextSortOrder = (int) playlistTrackRepository.countByPlaylistId(dbId) + 1;
        Long nativeTrackId = parseId(trackPublicId, "tr_");
        PlaylistTrack pt;
        if (nativeTrackId != null && trackRepository.existsById(nativeTrackId)) {
            pt = new PlaylistTrack(dbId, nativeTrackId, nextSortOrder);
            pt.setTrackPublicId(trackPublicId);
        } else {
            pt = new PlaylistTrack(
                    dbId, trackPublicId,
                    request.title() != null ? request.title() : "Track",
                    request.artist() != null ? request.artist() : "Artist",
                    request.album() != null ? request.album() : "",
                    request.durationMs() != null ? request.durationMs() : 0L,
                    request.audioUrl() != null ? request.audioUrl() : "",
                    request.coverUrl() != null ? request.coverUrl() : "",
                    nextSortOrder
            );
        }
        playlistTrackRepository.save(pt);
        return new SimpleSuccessResponse(true, "Track added to playlist");
    }

    @Transactional
    public SimpleSuccessResponse removeTrackFromPlaylist(String id, String trackId) {
        Playlist playlist = requireOwnedPlaylist(id);
        Long dbId = playlist.getId();
        playlistTrackRepository.deleteByPlaylistIdAndTrackPublicId(dbId, trackId);
        Long nativeId = parseId(trackId, "tr_");
        if (nativeId != null) {
            playlistTrackRepository.findByPlaylistIdOrderBySortOrderAsc(dbId).stream()
                    .filter(pt -> Objects.equals(pt.getTrackId(), nativeId))
                    .forEach(playlistTrackRepository::delete);
        }
        return new SimpleSuccessResponse(true, "Track removed from playlist");
    }

    public ArtistDetailResponse artistDetail(String id) {
        Long dbId = parseId(id, "art_");
        Artist artist = dbId != null ? artistRepository.findById(dbId).orElse(null) : null;
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
        List<TrackDto> recent = trackRepository.findTop20ByOrderByIdDesc().stream()
                .map(t -> toTrackDto(t, liked)).toList();
        TrackDto now = recent.isEmpty() ? null : recent.get(0);
        String source = now != null && now.album() != null && !now.album().isBlank() ? now.album() : "Queue";
        return new QueueResponse(true, now, recent, source);
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
        List<TrackDto> likedSongs = new ArrayList<>();
        for (UserLibraryItem item : libraryItemRepository.findByUserKeyAndLikedTrue(userKey)) {
            String tid = item.getTrackPublicId();
            Long dbId = parseId(tid, "tr_");
            if (dbId != null) {
                Optional<Track> opt = trackRepository.findById(dbId);
                if (opt.isPresent()) {
                    likedSongs.add(toTrackDto(opt.get(), liked));
                }
            }
        }
        List<PlaylistDto> userPlaylists = playlistRepository.findByUserKeyOrderByIdDesc(userKey).stream()
                .map(this::toPlaylistDto).toList();
        List<AlbumDto> savedAlbums = albumRepository.findAll().stream().limit(6).map(this::toAlbumDto).toList();
        List<ArtistDto> followedArtists = artistRepository.findAll().stream().limit(6).map(this::toArtistDto).toList();
        List<TrackDto> history = new ArrayList<>();
        for (UserLibraryItem item : libraryItemRepository.findByUserKeyOrderByLastPlayedAtDesc(userKey)) {
            if (history.size() >= 10) break;
            Long dbId = parseId(item.getTrackPublicId(), "tr_");
            if (dbId != null) {
                Optional<Track> opt = trackRepository.findById(dbId);
                if (opt.isPresent()) {
                    history.add(toTrackDto(opt.get(), liked));
                }
            }
        }

        return new UserLibraryResponse(
                true, userPlaylists, likedSongs, likedSongs.size(), savedAlbums, followedArtists, history
        );
    }

    private String defaultAvatarUrl(String name, String email) {
        String seed = (name != null && !name.isBlank()) ? name.trim() : (email != null && email.contains("@") ? email.substring(0, email.indexOf("@")) : "Zynera");
        return "https://api.dicebear.com/7.x/initials/svg?seed=" + java.net.URLEncoder.encode(seed, java.nio.charset.StandardCharsets.UTF_8) + "&backgroundColor=6b3ce9,e534b2,38bdf8&textColor=ffffff";
    }

    public UserProfileResponse profile() {
        String userKey = currentUserKey();
        Map<String, Object> user = new LinkedHashMap<>();
        Optional<User> dbUser = userRepository.findByEmail(userKey);
        if (dbUser.isPresent()) {
            User u = dbUser.get();
            user.put("id", "usr_" + u.getId());
            String displayName = (u.getName() != null && !u.getName().isBlank()) ? u.getName() : (u.getEmail() != null && u.getEmail().contains("@") ? u.getEmail().substring(0, u.getEmail().indexOf("@")) : "Zynera Listener");
            user.put("name", displayName);
            user.put("profilePicUrl", u.getProfilePicUrl() != null && !u.getProfilePicUrl().isBlank() ? u.getProfilePicUrl() : defaultAvatarUrl(displayName, u.getEmail()));
            user.put("isPremium", u.isPremium());
            user.put("isEmailVerified", u.isEmailVerified());
            user.put("followersCount", u.getFollowersCount());
            user.put("followingCount", u.getFollowingCount());
        } else {
            user.put("id", "usr_guest");
            user.put("name", userKey.contains("@") ? userKey.substring(0, userKey.indexOf("@")) : "Zynera Listener");
            user.put("email", userKey.equals("guest") ? "guest@zynera.app" : userKey);
            user.put("profilePicUrl", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=300");
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

    public SearchCategoriesResponse searchCategories() {
        List<BrowseCategoryDto> heroCategories = List.of(
                new BrowseCategoryDto("hero_music", "Music", 0xFFE1336EL, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Top Bollywood Songs"),
                new BrowseCategoryDto("hero_podcasts", "Podcasts", 0xFF006450L, "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=300", "Top Podcasts"),
                new BrowseCategoryDto("hero_events", "Live\nEvents", 0xFF7358FFL, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300", "Live Concerts"),
                new BrowseCategoryDto("hero_ipop", "Home of\nI-Pop", 0xFF1E3264L, "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Indian Pop Hits")
        );

        List<DiscoverTagDto> discoverTags = List.of(
                new DiscoverTagDto("disc_1", "#hindipop", "Trending Hindi Pop", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Hindi Pop"),
                new DiscoverTagDto("disc_2", "#bollywood", "Bollywood Blockbusters", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "Bollywood Hits"),
                new DiscoverTagDto("disc_3", "#punjabi", "Punjabi Wave 2026", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Punjabi Hits"),
                new DiscoverTagDto("disc_4", "#lofi", "Midnight Hindi Lo-Fi", "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "Hindi Lo-Fi Chill"),
                new DiscoverTagDto("disc_5", "#acoustic", "Peaceful Acoustic & Unplugged", "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg", "Acoustic Hindi"),
                new DiscoverTagDto("disc_6", "#workout", "High Voltage Gym Hits", "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "Workout Hits")
        );

        List<BrowseCategoryDto> browseCategories = new ArrayList<>();
        browseCategories.add(new BrowseCategoryDto("cat_made_for_you", "Made\nFor You", 0xFF8C67ACL, "https://c.saavncdn.com/001/Cocktail-2-Hindi-2024-20240214152011-500x500.jpg", "Made For You"));
        browseCategories.add(new BrowseCategoryDto("cat_new_releases", "New\nReleases", 0xFFE8115BL, "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "Latest Hindi Releases"));
        browseCategories.add(new BrowseCategoryDto("cat_hindi", "Hindi", 0xFFE91429L, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Top Hindi Songs"));
        browseCategories.add(new BrowseCategoryDto("cat_punjabi", "Punjabi", 0xFFB02897L, "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "Top Punjabi Hits"));
        browseCategories.add(new BrowseCategoryDto("cat_charts", "Charts", 0xFF8D67ABL, "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", "Top 50 India"));
        browseCategories.add(new BrowseCategoryDto("cat_lofi", "Lo-Fi\nChill", 0xFF1E3264L, "https://c.saavncdn.com/602/Dooron-Dooron-Punjabi-2022-20220914180808-500x500.jpg", "Lo-Fi Beats"));
        browseCategories.add(new BrowseCategoryDto("cat_party", "Party &\nDance", 0xFF503750L, "https://c.saavncdn.com/152/Jodi-Punjabi-2023-20230509183424-500x500.jpg", "Bollywood Party"));
        browseCategories.add(new BrowseCategoryDto("cat_romance", "Romance", 0xFFE8115BL, "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "Romantic Hindi Songs"));
        browseCategories.add(new BrowseCategoryDto("cat_bhakti", "Devotional", 0xFF477D95L, "https://c.saavncdn.com/177/Barsaat-Lagdi-Ae-Hindi-2023-20230713123847-500x500.jpg", "Bhakti Songs"));
        browseCategories.add(new BrowseCategoryDto("cat_workout", "Workout", 0xFF777777L, "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=300", "Workout Motivation"));

        return new SearchCategoriesResponse(true, heroCategories, discoverTags, browseCategories);
    }

    public PremiumResponse premium() {
        String userKey = currentUserKey();
        boolean isPremium = userRepository.findByEmail(userKey).map(User::isPremium).orElse(false);
        String savedPlan = getPrefs("PREMIUM_PLAN").stream().findFirst().orElse("free");

        List<Map<String, Object>> plans = List.of(
                Map.of(
                        "id", "individual",
                        "name", "Individual",
                        "price", "₹119",
                        "period", "per month",
                        "description", "1 Premium Account • Lossless 320kbps • Zero Ads • Unlimited Offline Downloads",
                        "badge", "3 Months Free",
                        "color1", "#6B3CE9",
                        "color2", "#9825DD",
                        "features", List.of("1 Premium account", "Ad-free music listening", "Download to listen offline", "Hi-Fi 24-bit Lossless streaming", "Cancel anytime")
                ),
                Map.of(
                        "id", "duo",
                        "name", "Duo",
                        "price", "₹149",
                        "period", "per month",
                        "description", "2 Premium Accounts for couples or roommates sharing the same vibe",
                        "badge", "Most Popular",
                        "color1", "#E534B2",
                        "color2", "#FF52C4",
                        "features", List.of("2 Premium accounts", "Shared Duo Mix playlist", "Ad-free on both accounts", "Offline downloads on 10 devices", "High-Fidelity Audio")
                ),
                Map.of(
                        "id", "family",
                        "name", "Family",
                        "price", "₹179",
                        "period", "per month",
                        "description", "Up to 6 Premium Accounts + Family Mix & Explicit Filter Controls",
                        "badge", "Best Value",
                        "color1", "#06B6D4",
                        "color2", "#3B82F6",
                        "features", List.of("6 Premium accounts", "Family Mix updated daily", "Block explicit music filter", "Individual saved libraries", "Spotify Connect & Cast")
                ),
                Map.of(
                        "id", "student",
                        "name", "Student",
                        "price", "₹59",
                        "period", "per month",
                        "description", "Special 50% discount for verified college and university students",
                        "badge", "Students Only",
                        "color1", "#F59E0B",
                        "color2", "#EF4444",
                        "features", List.of("1 Verified student account", "50% off regular subscription", "Full ad-free experience", "Unlimited offline downloads", "Annual re-verification")
                )
        );

        List<String> benefits = List.of(
                "🎧 Hi-Fi Lossless 24-bit/192kHz Audio Streaming",
                "🚫 100% Ad-Free Music Experience Across All Platforms",
                "📥 Unlimited Offline Downloads on 5 Mobile & Tablet Devices",
                "🤖 Unlimited Access to Sonexa AI DJ, Smart Vocal Remover & Equalizer DSP",
                "🎨 Exclusive AI Playlist Cover Generator & Studio Creation Tools",
                "⚡ Unlimited Track Skips & Zero Audio Compression"
        );

        return new PremiumResponse(true, isPremium, plans, benefits);
    }

    @Transactional
    public SimpleSuccessResponse subscribe(String planId) {
        String effectivePlan = planId != null && !planId.isBlank() ? planId : "individual";
        userRepository.findByEmail(currentUserKey()).ifPresent(u -> {
            u.setPremium(true);
            userRepository.save(u);
        });
        replacePrefs(currentUserKey(), "PREMIUM_PLAN", List.of(effectivePlan));
        replacePrefs(currentUserKey(), "PREMIUM_EXPIRY", List.of(java.time.LocalDate.now().plusMonths(1).toString()));
        return new SimpleSuccessResponse(true, "Successfully activated Sonexa Premium (" + effectivePlan + ")");
    }

    @Transactional
    public RedeemCouponResponse redeemCoupon(String code) {
        if (code == null || code.trim().isBlank()) {
            return new RedeemCouponResponse(false, "Please enter a valid promo code", false, "");
        }
        String cleanCode = code.trim().toUpperCase();
        List<String> validCodes = List.of("SONEXA2026", "VIPPASS", "FREE3M", "STUDENT50", "SONEXAPRO", "PREMIUM100");
        if (validCodes.contains(cleanCode)) {
            userRepository.findByEmail(currentUserKey()).ifPresent(u -> {
                u.setPremium(true);
                userRepository.save(u);
            });
            replacePrefs(currentUserKey(), "PREMIUM_PLAN", List.of("promo_" + cleanCode.toLowerCase()));
            replacePrefs(currentUserKey(), "PREMIUM_EXPIRY", List.of(java.time.LocalDate.now().plusMonths(3).toString()));
            return new RedeemCouponResponse(true, "Promo code " + cleanCode + " applied! You unlocked 3 Months of Sonexa VIP Premium.", true, "VIP Promo (" + cleanCode + ")");
        }
        return new RedeemCouponResponse(false, "Invalid promo code. Try 'SONEXA2026' or 'VIPPASS'.", false, "");
    }

    @Transactional
    public SimpleSuccessResponse cancelSubscription() {
        userRepository.findByEmail(currentUserKey()).ifPresent(u -> {
            u.setPremium(false);
            userRepository.save(u);
        });
        replacePrefs(currentUserKey(), "PREMIUM_PLAN", List.of("free"));
        return new SimpleSuccessResponse(true, "Premium subscription cancelled. You will continue on the Free tier.");
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
                "",
                128, "F# Minor", recs);
    }

    public AiChatResponse aiChat(AiChatRequest request) {
        String msg = request != null && request.message() != null ? request.message() : "";
        return new AiChatResponse(true,
                "Sonexa AI: Based on \"" + msg + "\", try a chill lo-fi mix or Energy Boost playlist.");
    }
}

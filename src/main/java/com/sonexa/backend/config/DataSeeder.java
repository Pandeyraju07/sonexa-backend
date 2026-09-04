package com.sonexa.backend.config;

import com.sonexa.backend.model.entity.*;
import com.sonexa.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String COVER = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500";
    private static final String COVER2 = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500";
    private static final String COVER3 = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500";
    private static final String COVER4 = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500";

    private final GenreRepository genreRepository;
    private final ArtistRepository artistRepository;
    private final MoodRepository moodRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final PodcastRepository podcastRepository;
    private final PodcastEpisodeRepository podcastEpisodeRepository;
    private final AppNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DataSeeder(
            GenreRepository genreRepository,
            ArtistRepository artistRepository,
            MoodRepository moodRepository,
            AlbumRepository albumRepository,
            TrackRepository trackRepository,
            PlaylistRepository playlistRepository,
            PlaylistTrackRepository playlistTrackRepository,
            PodcastRepository podcastRepository,
            PodcastEpisodeRepository podcastEpisodeRepository,
            AppNotificationRepository notificationRepository,
            UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        this.genreRepository = genreRepository;
        this.artistRepository = artistRepository;
        this.moodRepository = moodRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
        this.playlistTrackRepository = playlistTrackRepository;
        this.podcastRepository = podcastRepository;
        this.podcastEpisodeRepository = podcastEpisodeRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdminUser();
        if (trackRepository.count() > 0) {
            log.info("event=SEED_SKIPPED reason=already_populated");
            return;
        }
        seedGenres();
        seedMoods();
        seedArtistsAlbumsTracks();
        seedPlaylists();
        seedPodcasts();
        seedNotifications();
        log.info("event=SEED_COMPLETE tracks={} artists={} albums={}",
                trackRepository.count(), artistRepository.count(), albumRepository.count());
    }

    private void seedAdminUser() {
        String adminEmail = "admin@sonexa.ai";
        if (userRepository.existsByEmail(adminEmail)) {
            userRepository.findByEmail(adminEmail).ifPresent(u -> {
                if (!"ADMIN".equalsIgnoreCase(u.getRole())) {
                    u.setRole("ADMIN");
                    userRepository.save(u);
                }
            });
            return;
        }
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setName("Zynera Admin");
        admin.setProvider("LOCAL");
        admin.setEmailVerified(true);
        admin.setEnabled(true);
        admin.setRole("ADMIN");
        admin.setProfilePicUrl("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300");
        userRepository.save(admin);
        log.info("event=ADMIN_SEEDED email={} password=Admin@123", adminEmail);
    }

    private void seedGenres() {
        genreRepository.save(new Genre("Pop", "#E534B2", "#FF52C4", 1));
        genreRepository.save(new Genre("Hip-Hop", "#6B3CE9", "#9825DD", 2));
        genreRepository.save(new Genre("EDM / Dance", "#06B6D4", "#3B82F6", 3));
        genreRepository.save(new Genre("Bollywood", "#F59E0B", "#EF4444", 4));
        genreRepository.save(new Genre("Lo-Fi Chill", "#8B5CF6", "#EC4899", 5));
        genreRepository.save(new Genre("Classical", "#059669", "#10B981", 6));
        genreRepository.save(new Genre("R&B", "#A855F7", "#EC4899", 7));
        genreRepository.save(new Genre("Rock", "#EF4444", "#F97316", 8));
    }

    private void seedMoods() {
        moodRepository.save(new Mood("Energetic", "bolt", "#F59E0B", 1));
        moodRepository.save(new Mood("Relaxed", "spa", "#06B6D4", 2));
        moodRepository.save(new Mood("Focused", "center_focus", "#8B5CF6", 3));
        moodRepository.save(new Mood("Party", "celebration", "#E534B2", 4));
        moodRepository.save(new Mood("Chill / Peaceful", "self_improvement", "#10B981", 5));
        moodRepository.save(new Mood("Romantic", "favorite", "#EF4444", 6));
        moodRepository.save(new Mood("Workout / Power", "fitness", "#F97316", 7));
    }

    private void seedArtistsAlbumsTracks() {
        Artist weeknd = artistRepository.save(new Artist("The Weeknd", "R&B / Pop", "#E534B2", "#FF52C4", COVER));
        weeknd.setBio("Canadian singer, songwriter and record producer.");
        weeknd.setFollowersCount(95_000_000);
        artistRepository.save(weeknd);

        Artist arijit = artistRepository.save(new Artist("Arijit Singh", "Bollywood", "#5935E5", "#9825DD", COVER2));
        arijit.setBio("Indian playback singer and music composer.");
        arijit.setFollowersCount(48_000_000);
        artistRepository.save(arijit);

        Artist dua = artistRepository.save(new Artist("Dua Lipa", "Dance Pop", "#8B5CF6", "#EC4899", COVER3));
        dua.setBio("English and Albanian singer and songwriter.");
        dua.setFollowersCount(72_000_000);
        artistRepository.save(dua);

        Artist drake = artistRepository.save(new Artist("Drake", "Hip-Hop", "#F59E0B", "#EF4444", COVER4));
        Artist taylor = artistRepository.save(new Artist("Taylor Swift", "Pop / Folk", "#06B6D4", "#3B82F6", COVER));
        Artist badshah = artistRepository.save(new Artist("Badshah", "Hip-Hop", "#F59E0B", "#EF4444", COVER2));
        Artist rahman = artistRepository.save(new Artist("A.R. Rahman", "Composer", "#059669", "#10B981", COVER3));
        artistRepository.save(new Artist("Ed Sheeran", "Pop", "#3B82F6", "#06B6D4", COVER4));

        Album starboy = albumRepository.save(new Album("Starboy", "The Weeknd", "2016", COVER, 18));
        Album afterHours = albumRepository.save(new Album("After Hours", "The Weeknd", "2020", COVER3, 14));
        Album brahmastra = albumRepository.save(new Album("Brahmastra", "Arijit Singh", "2022", COVER2, 9));
        Album futureNostalgia = albumRepository.save(new Album("Future Nostalgia", "Dua Lipa", "2020", COVER4, 12));
        Album clb = albumRepository.save(new Album("Certified Lover Boy", "Drake", "2021", COVER3, 21));
        Album cokeStudio = albumRepository.save(new Album("Coke Studio S14", "Ali Sethi, Shae Gill", "2022", COVER4, 8));

        saveTrack("Jai Ho", "A.R. Rahman", "Slumdog Millionaire", 320000L, COVER3, "50M", false, null, rahman.getId());
    }

    private Track saveTrack(String title, String artist, String album, long ms, String cover,
                            String plays, boolean trending, Long albumId, Long artistId) {
        Track t = new Track(title, artist, album, ms, cover, plays, trending);
        t.setAlbumId(albumId);
        t.setArtistId(artistId);
        return trackRepository.save(t);
    }

    private void seedPlaylists() {
        Playlist p1 = playlistRepository.save(new Playlist("Daily Mix 1", "Arijit Singh, Atif Aslam, Pritam", "waves", true));
        Playlist p2 = playlistRepository.save(new Playlist("Energy Boost", "High tempo workout hits", "runner", true));
        Playlist p3 = playlistRepository.save(new Playlist("Relax & Unwind", "Calm music to soothe your mind", "sunset", true));
        Playlist p4 = playlistRepository.save(new Playlist("Top Hits Global", "What's hot worldwide", "globe", false));

        var tracks = trackRepository.findAll();
        int i = 0;
        for (Track t : tracks) {
            playlistTrackRepository.save(new PlaylistTrack(p1.getId(), t.getId(), i));
            if (i < 4) playlistTrackRepository.save(new PlaylistTrack(p2.getId(), t.getId(), i));
            if (i % 2 == 0) playlistTrackRepository.save(new PlaylistTrack(p3.getId(), t.getId(), i / 2));
            playlistTrackRepository.save(new PlaylistTrack(p4.getId(), t.getId(), i));
            i++;
        }
    }

    private void seedPodcasts() {
        Podcast tech = podcastRepository.save(new Podcast(
                "Zynera Tech Talks", "Maya Chen",
                "Deep dives into music tech, AI curation and lossless audio.",
                COVER, "Technology"));
        Podcast culture = podcastRepository.save(new Podcast(
                "Beat Culture", "Arjun Mehta",
                "Stories behind the songs that shaped generations.",
                COVER2, "Culture"));

        podcastEpisodeRepository.save(new PodcastEpisode(tech.getId(), "AI DJs of Tomorrow", "How generative models remix on the fly", "42 min", 1));
        podcastEpisodeRepository.save(new PodcastEpisode(tech.getId(), "Spatial Audio Explained", "From stereo to immersive soundscapes", "35 min", 2));
        podcastEpisodeRepository.save(new PodcastEpisode(culture.getId(), "The Rise of Indie Pop", "Bedroom producers to festival stages", "48 min", 1));
        podcastEpisodeRepository.save(new PodcastEpisode(culture.getId(), "Bollywood Beats 2026", "What's trending in Indian film music", "39 min", 2));
    }

    private void seedNotifications() {
        notificationRepository.save(new AppNotification(
                "global", "New Single Alert", "Arijit Singh just released 'Satranga'. Stream it now!",
                "music_note", "#E534B2", "10m ago"));
        notificationRepository.save(new AppNotification(
                "global", "AI Mix Ready", "Your Weekly AI Signature mix is ready to play.",
                "auto_awesome", "#8B5CF6", "1h ago"));
        notificationRepository.save(new AppNotification(
                "global", "Premium Offer", "Get 2 months of Zynera Premium for free.",
                "workspace_premium", "#F59E0B", "Yesterday"));
        notificationRepository.save(new AppNotification(
                "global", "Friend Activity", "Yash started following The Weeknd.",
                "person_add", "#06B6D4", "2d ago"));
    }
}

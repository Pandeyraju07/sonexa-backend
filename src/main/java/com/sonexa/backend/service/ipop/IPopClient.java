package com.sonexa.backend.service.ipop;

import com.sonexa.backend.model.dto.CatalogDtos.IPopArtistDto;
import com.sonexa.backend.model.dto.CatalogDtos.IPopHomeResponse;
import com.sonexa.backend.model.dto.CatalogDtos.IPopPlaylistDto;
import com.sonexa.backend.model.dto.CatalogDtos.TrackDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IPopClient {

    private final List<TrackDto> allIPopTracks = new ArrayList<>();
    private final List<IPopPlaylistDto> allPlaylists = new ArrayList<>();
    private final List<IPopArtistDto> allArtists = new ArrayList<>();

    public IPopClient() {
        initIPopData();
    }

    private void initIPopData() {
        allIPopTracks.clear();
        allPlaylists.clear();
        allArtists.clear();

        // 1. Tracks (using 9-arg constructor: id, title, artist, album, durationMs, audioUrl, coverUrl, playsCount, isLiked)
        allIPopTracks.add(new TrackDto("ipop_1", "Maan Meri Jaan", "King", "Champagne Talk", 194000L, "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "485M", true));
        allIPopTracks.add(new TrackDto("ipop_2", "cold/mess", "Prateek Kuhad", "cold/mess EP", 272000L, "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80", "192M", true));
        allIPopTracks.add(new TrackDto("ipop_3", "Husn", "Anuv Jain", "Husn - Single", 218000L, "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "340M", true));
        allIPopTracks.add(new TrackDto("ipop_4", "With You", "AP Dhillon", "With You", 154000L, "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", "260M", true));
        allIPopTracks.add(new TrackDto("ipop_5", "Heeriye", "Jasleen Royal, Arijit Singh", "Heeriye", 195000L, "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "520M", true));
        allIPopTracks.add(new TrackDto("ipop_6", "Liggi", "Ritviz", "Liggi Single", 182000L, "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80", "180M", true));
        allIPopTracks.add(new TrackDto("ipop_7", "Choo Lo", "The Local Train", "Aalas Ka Pedh", 233000L, "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=500&q=80", "290M", true));
        allIPopTracks.add(new TrackDto("ipop_8", "tere ho ke", "Zaeden", "Genesis 1:1", 185000L, "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80", "110M", true));
        allIPopTracks.add(new TrackDto("ipop_9", "Akhiyaan Gulaab", "Mitraz", "Teri Baaton Mein Aisa Uljha Jiya", 171000L, "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "215M", true));

        // 2. Artists
        allArtists.add(new IPopArtistDto("art_king", "King", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80", "12.4M", "Maan Meri Jaan", true));
        allArtists.add(new IPopArtistDto("art_prateek", "Prateek Kuhad", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80", "4.8M", "cold/mess", true));
        allArtists.add(new IPopArtistDto("art_anuv", "Anuv Jain", "https://c.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-500x500.jpg", "8.2M", "Husn", true));
        allArtists.add(new IPopArtistDto("art_ap", "AP Dhillon", "https://c.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-500x500.jpg", "14.1M", "With You", true));
        allArtists.add(new IPopArtistDto("art_jasleen", "Jasleen Royal", "https://c.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-500x500.jpg", "6.5M", "Heeriye", true));
        allArtists.add(new IPopArtistDto("art_ritviz", "Ritviz", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80", "5.1M", "Liggi", true));
        allArtists.add(new IPopArtistDto("art_zaeden", "Zaeden", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=500&q=80", "3.2M", "tere ho ke", true));
        allArtists.add(new IPopArtistDto("art_mitraz", "Mitraz", "https://c.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-500x500.jpg", "2.9M", "Akhiyaan Gulaab", true));

        // 3. Playlists
        allPlaylists.add(new IPopPlaylistDto(
                "pl_ipop_superhits",
                "I-Pop Superhits 2026",
                "The defining sound of modern Indian pop music. Updated weekly with the freshest hits.",
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&q=80",
                "🔥 TRENDING #1",
                allIPopTracks.size(),
                allIPopTracks
        ));

        allPlaylists.add(new IPopPlaylistDto(
                "pl_indie_chill",
                "Indie India: Acoustic & Chill",
                "Soothing indie melodies, acoustic guitars, and heartfelt poetry from India's best singer-songwriters.",
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&q=80",
                "🌿 VIBES",
                allIPopTracks.size(),
                allIPopTracks
        ));

        allPlaylists.add(new IPopPlaylistDto(
                "pl_pop_punjabi",
                "Pop Punjabi Heat",
                "Banging basslines, modern pop synths, and energetic Punjabi vocals.",
                "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&q=80",
                "⚡ ENERGETIC",
                allIPopTracks.size(),
                allIPopTracks
        ));

        allPlaylists.add(new IPopPlaylistDto(
                "pl_desi_lofi",
                "Desi Lo-Fi & Pop Beats",
                "Late night slow reverb, mellow beats, and aesthetic desi melodies.",
                "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&q=80",
                "🌙 LATE NIGHT",
                allIPopTracks.size(),
                allIPopTracks
        ));
    }

    public IPopHomeResponse getHomeFeed(String subgenre) {
        List<String> subgenres = List.of("All", "Indie Acoustic", "Desi Pop", "Late Night Beats", "Punjabi Pop", "Romantic I-Pop", "Hip-Hop Crossover");

        List<TrackDto> filteredTracks = allIPopTracks;
        if (subgenre != null && !subgenre.equalsIgnoreCase("All")) {
            filteredTracks = allIPopTracks.stream()
                    .filter(t -> t.title().toLowerCase().contains(subgenre.toLowerCase()) || t.artist().toLowerCase().contains(subgenre.toLowerCase()))
                    .collect(Collectors.toList());
            if (filteredTracks.isEmpty()) {
                filteredTracks = allIPopTracks;
            }
        }

        return new IPopHomeResponse(
                true,
                "Home of I-Pop",
                "Discover the pulse of Indian Pop, Indie singer-songwriters, and modern urban hits.",
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&q=80",
                "I-Pop Superstars • 2026 Spotlight",
                "Experience the new golden era of Indian Pop music featuring King, Prateek Kuhad, Anuv Jain, and more.",
                subgenres,
                filteredTracks,
                allPlaylists,
                allArtists,
                allIPopTracks.subList(0, Math.min(5, allIPopTracks.size()))
        );
    }

    public IPopPlaylistDto getPlaylist(String id) {
        return allPlaylists.stream()
                .filter(p -> p.id().equalsIgnoreCase(id))
                .findFirst()
                .orElse(allPlaylists.get(0));
    }

    public List<IPopArtistDto> getArtists() {
        return allArtists;
    }
}
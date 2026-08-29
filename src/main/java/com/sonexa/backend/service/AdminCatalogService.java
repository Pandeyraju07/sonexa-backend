package com.sonexa.backend.service;

import com.sonexa.backend.constant.ErrorCode;
import com.sonexa.backend.exception.BusinessException;
import com.sonexa.backend.model.dto.AdminDtos.*;
import com.sonexa.backend.model.entity.*;
import com.sonexa.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AdminCatalogService {

    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final MediaStorageService mediaStorageService;

    public AdminCatalogService(
            TrackRepository trackRepository,
            ArtistRepository artistRepository,
            AlbumRepository albumRepository,
            GenreRepository genreRepository,
            MediaStorageService mediaStorageService
    ) {
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
        this.mediaStorageService = mediaStorageService;
    }

    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
                true,
                trackRepository.count(),
                artistRepository.count(),
                albumRepository.count(),
                genreRepository.count()
        );
    }

    public AdminTrackListResponse listTracks() {
        List<AdminTrackDto> tracks = trackRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .map(this::toTrackDto)
                .toList();
        return new AdminTrackListResponse(true, tracks);
    }

    @Transactional
    public AdminTrackResponse createTrack(CreateTrackRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Track title is required");
        }
        Track track = new Track();
        track.setTitle(request.title().trim());
        track.setArtistName(blankTo(request.artistName(), "Unknown Artist"));
        track.setAlbumTitle(blankTo(request.albumTitle(), "Singles"));
        track.setDurationMs(request.durationMs() != null ? request.durationMs() : 180_000L);
        track.setAudioUrl(blankTo(request.audioUrl(),
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
        track.setCoverUrl(blankTo(request.coverUrl(),
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500"));
        track.setPlaysCount("0");
        track.setTrending(request.trending() == null || Boolean.TRUE.equals(request.trending()));
        if (request.artistId() != null) {
            track.setArtistId(request.artistId());
        } else {
            track.setArtistId(resolveOrCreateArtist(track.getArtistName()).getId());
        }
        track.setAlbumId(request.albumId());
        track = trackRepository.save(track);
        return new AdminTrackResponse(true, "Track created", toTrackDto(track));
    }

    @Transactional
    public AdminTrackResponse uploadTrack(
            MultipartFile audio,
            MultipartFile cover,
            String title,
            String artistName,
            String albumTitle,
            Long durationMs,
            Boolean trending,
            String genre,
            String subgenre,
            String language,
            Boolean explicit,
            String description,
            String tags,
            String releaseType,
            String releaseDate
    ) {
        if (audio == null || audio.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Audio file is required");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Track title is required");
        }
        try {
            String audioUrl = mediaStorageService.storeAudio(audio);
            String coverUrl = (cover != null && !cover.isEmpty())
                    ? mediaStorageService.storeCover(cover)
                    : "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500";

            Track track = new Track();
            track.setTitle(title.trim());
            String resolvedArtist = blankTo(artistName, "Unknown Artist");
            track.setArtistName(resolvedArtist);
            track.setAlbumTitle(blankTo(albumTitle, "Singles"));
            track.setDurationMs(durationMs != null ? durationMs : 180_000L);
            track.setAudioUrl(audioUrl);
            track.setCoverUrl(coverUrl);
            track.setPlaysCount("0");
            track.setTrending(trending == null || Boolean.TRUE.equals(trending));
            track.setGenre(blankToNull(genre));
            track.setSubgenre(blankToNull(subgenre));
            track.setLanguage(blankToNull(language));
            track.setExplicitContent(Boolean.TRUE.equals(explicit));
            track.setDescription(blankToNull(description));
            track.setTags(blankToNull(tags));
            track.setReleaseType(blankTo(releaseType, "Single"));
            track.setReleaseDate(blankToNull(releaseDate));
            track.setArtistId(resolveOrCreateArtist(resolvedArtist).getId());
            track = trackRepository.save(track);
            return new AdminTrackResponse(true, "Track uploaded", toTrackDto(track));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage());
        }
    }

    @Transactional
    public AdminTrackResponse updateTrack(Long id, UpdateTrackRequest request) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Track not found"));
        if (request.title() != null && !request.title().isBlank()) track.setTitle(request.title().trim());
        if (request.artistName() != null) track.setArtistName(request.artistName());
        if (request.albumTitle() != null) track.setAlbumTitle(request.albumTitle());
        if (request.durationMs() != null) track.setDurationMs(request.durationMs());
        if (request.audioUrl() != null) track.setAudioUrl(request.audioUrl());
        if (request.coverUrl() != null) track.setCoverUrl(request.coverUrl());
        if (request.trending() != null) track.setTrending(request.trending());
        track = trackRepository.save(track);
        return new AdminTrackResponse(true, "Track updated", toTrackDto(track));
    }

    @Transactional
    public SimpleMessageResponse deleteTrack(Long id) {
        if (!trackRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Track not found");
        }
        trackRepository.deleteById(id);
        return new SimpleMessageResponse(true, "Track deleted");
    }

    public AdminArtistListResponse listArtists() {
        List<AdminArtistDto> artists = artistRepository.findAll().stream()
                .map(this::toArtistDto)
                .toList();
        return new AdminArtistListResponse(true, artists);
    }

    @Transactional
    public AdminArtistResponse createArtist(CreateArtistRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Artist name is required");
        }
        Artist artist = new Artist(
                request.name().trim(),
                blankTo(request.genre(), "Pop"),
                blankTo(request.color1(), "#6B3CE9"),
                blankTo(request.color2(), "#E534B2"),
                blankTo(request.imageUrl(), "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400")
        );
        if (request.bio() != null) artist.setBio(request.bio());
        artist = artistRepository.save(artist);
        return new AdminArtistResponse(true, "Artist created", toArtistDto(artist));
    }

    public AdminAlbumListResponse listAlbums() {
        List<AdminAlbumDto> albums = albumRepository.findAll().stream()
                .map(this::toAlbumDto)
                .toList();
        return new AdminAlbumListResponse(true, albums);
    }

    @Transactional
    public AdminAlbumResponse createAlbum(CreateAlbumRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Album title is required");
        }
        Album album = new Album(
                request.title().trim(),
                blankTo(request.artistName(), "Various Artists"),
                blankTo(request.year(), "2026"),
                blankTo(request.coverUrl(), "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500"),
                request.trackCount() != null ? request.trackCount() : 0
        );
        album = albumRepository.save(album);
        return new AdminAlbumResponse(true, "Album created", toAlbumDto(album));
    }

    public AdminGenreListResponse listGenres() {
        List<AdminGenreDto> genres = genreRepository.findAll().stream()
                .map(this::toGenreDto)
                .toList();
        return new AdminGenreListResponse(true, genres);
    }

    @Transactional
    public AdminGenreResponse createGenre(CreateGenreRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Genre name is required");
        }
        Genre genre = new Genre(
                request.name().trim(),
                blankTo(request.color1(), "#6B3CE9"),
                blankTo(request.color2(), "#E534B2"),
                request.sortOrder() != null ? request.sortOrder() : 99
        );
        genre = genreRepository.save(genre);
        return new AdminGenreResponse(true, "Genre created", toGenreDto(genre));
    }

    private AdminTrackDto toTrackDto(Track t) {
        return new AdminTrackDto(
                t.publicId(),
                t.getId(),
                t.getTitle(),
                t.getArtistName(),
                t.getAlbumTitle(),
                t.getDurationMs(),
                t.getAudioUrl(),
                t.getCoverUrl(),
                t.getPlaysCount(),
                t.isTrending(),
                t.getGenre(),
                t.getSubgenre(),
                t.getLanguage(),
                t.isExplicitContent(),
                t.getDescription(),
                t.getTags(),
                t.getReleaseType(),
                t.getReleaseDate()
        );
    }

    private AdminArtistDto toArtistDto(Artist a) {
        return new AdminArtistDto(
                "art_" + a.getId(),
                a.getId(),
                a.getName(),
                a.getGenre(),
                a.getBio(),
                a.getImageUrl(),
                a.isVerified(),
                a.getFollowersCount()
        );
    }

    private AdminAlbumDto toAlbumDto(Album a) {
        return new AdminAlbumDto(
                a.publicId(),
                a.getId(),
                a.getTitle(),
                a.getArtistName(),
                a.getYear(),
                a.getCoverUrl(),
                a.getTrackCount()
        );
    }

    private AdminGenreDto toGenreDto(Genre g) {
        return new AdminGenreDto(
                "g_" + g.getId(),
                g.getId(),
                g.getName(),
                g.getColor1(),
                g.getColor2(),
                g.getSortOrder()
        );
    }

    private Artist resolveOrCreateArtist(String name) {
        return artistRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Artist artist = new Artist();
            artist.setName(name);
            artist.setGenre("Various");
            artist.setBio("");
            artist.setImageUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300");
            artist.setColor1("#6B3CE9");
            artist.setColor2("#E534B2");
            artist.setVerified(false);
            artist.setFollowersCount(0);
            return artistRepository.save(artist);
        });
    }

    private String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

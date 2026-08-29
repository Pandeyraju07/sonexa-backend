package com.sonexa.backend.model.dto;

import java.util.List;
import java.util.Map;

public class AdminDtos {

    public record AdminStatsResponse(
            boolean success,
            long tracks,
            long artists,
            long albums,
            long genres
    ) {}

    public record AdminTrackDto(
            String id,
            Long dbId,
            String title,
            String artistName,
            String albumTitle,
            Long durationMs,
            String audioUrl,
            String coverUrl,
            String playsCount,
            boolean trending,
            String genre,
            String subgenre,
            String language,
            boolean explicitContent,
            String description,
            String tags,
            String releaseType,
            String releaseDate
    ) {}

    public record AdminTrackListResponse(boolean success, List<AdminTrackDto> tracks) {}

    public record AdminTrackResponse(boolean success, String message, AdminTrackDto track) {}

    public record CreateTrackRequest(
            String title,
            String artistName,
            String albumTitle,
            Long durationMs,
            String audioUrl,
            String coverUrl,
            Boolean trending,
            Long artistId,
            Long albumId
    ) {}

    public record UpdateTrackRequest(
            String title,
            String artistName,
            String albumTitle,
            Long durationMs,
            String audioUrl,
            String coverUrl,
            Boolean trending
    ) {}

    public record AdminArtistDto(
            String id,
            Long dbId,
            String name,
            String genre,
            String bio,
            String imageUrl,
            boolean verified,
            int followersCount
    ) {}

    public record AdminArtistListResponse(boolean success, List<AdminArtistDto> artists) {}

    public record AdminArtistResponse(boolean success, String message, AdminArtistDto artist) {}

    public record CreateArtistRequest(
            String name,
            String genre,
            String bio,
            String imageUrl,
            String color1,
            String color2
    ) {}

    public record AdminAlbumDto(
            String id,
            Long dbId,
            String title,
            String artistName,
            String year,
            String coverUrl,
            int trackCount
    ) {}

    public record AdminAlbumListResponse(boolean success, List<AdminAlbumDto> albums) {}

    public record AdminAlbumResponse(boolean success, String message, AdminAlbumDto album) {}

    public record CreateAlbumRequest(
            String title,
            String artistName,
            String year,
            String coverUrl,
            Integer trackCount
    ) {}

    public record AdminGenreDto(
            String id,
            Long dbId,
            String name,
            String color1,
            String color2,
            int sortOrder
    ) {}

    public record AdminGenreListResponse(boolean success, List<AdminGenreDto> genres) {}

    public record AdminGenreResponse(boolean success, String message, AdminGenreDto genre) {}

    public record CreateGenreRequest(
            String name,
            String color1,
            String color2,
            Integer sortOrder
    ) {}

    public record SimpleMessageResponse(boolean success, String message) {}
}

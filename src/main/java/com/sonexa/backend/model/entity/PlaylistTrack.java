package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_tracks")
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long playlistId;
    private Long trackId;
    private String trackPublicId;
    private String trackTitle;
    private String trackArtist;
    private String trackAlbum;
    private Long durationMs;
    private String audioUrl;
    private String coverUrl;
    private int sortOrder;
    private LocalDateTime addedAt = LocalDateTime.now();

    public PlaylistTrack() {}

    public PlaylistTrack(Long playlistId, Long trackId, int sortOrder) {
        this.playlistId = playlistId;
        this.trackId = trackId;
        this.sortOrder = sortOrder;
        this.trackPublicId = "tr_" + trackId;
        this.addedAt = LocalDateTime.now();
    }

    public PlaylistTrack(Long playlistId, String trackPublicId, String trackTitle, String trackArtist,
                         String trackAlbum, Long durationMs, String audioUrl, String coverUrl, int sortOrder) {
        this.playlistId = playlistId;
        this.trackPublicId = trackPublicId;
        this.trackTitle = trackTitle;
        this.trackArtist = trackArtist;
        this.trackAlbum = trackAlbum;
        this.durationMs = durationMs;
        this.audioUrl = audioUrl;
        this.coverUrl = coverUrl;
        this.sortOrder = sortOrder;
        this.addedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public Long getTrackId() { return trackId; }
    public void setTrackId(Long trackId) { this.trackId = trackId; }
    public String getTrackPublicId() { return trackPublicId; }
    public void setTrackPublicId(String trackPublicId) { this.trackPublicId = trackPublicId; }
    public String getTrackTitle() { return trackTitle; }
    public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }
    public String getTrackArtist() { return trackArtist; }
    public void setTrackArtist(String trackArtist) { this.trackArtist = trackArtist; }
    public String getTrackAlbum() { return trackAlbum; }
    public void setTrackAlbum(String trackAlbum) { this.trackAlbum = trackAlbum; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}

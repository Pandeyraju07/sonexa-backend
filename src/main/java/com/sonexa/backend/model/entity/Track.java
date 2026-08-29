package com.sonexa.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String artistName;
    private String albumTitle;
    private Long durationMs;
    private String audioUrl;
    private String coverUrl;
    private String playsCount;
    private boolean trending;
    private Long albumId;
    private Long artistId;

    private String genre;
    private String subgenre;
    private String language;
    private boolean explicitContent;
    private String releaseType;
    private String releaseDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    public Track() {}

    public Track(String title, String artistName, String albumTitle, Long durationMs,
                 String coverUrl, String playsCount, boolean trending) {
        this.title = title;
        this.artistName = artistName;
        this.albumTitle = albumTitle;
        this.durationMs = durationMs;
        this.coverUrl = coverUrl;
        this.playsCount = playsCount;
        this.trending = trending;
        this.audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getAlbumTitle() { return albumTitle; }
    public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getPlaysCount() { return playsCount; }
    public void setPlaysCount(String playsCount) { this.playsCount = playsCount; }
    public boolean isTrending() { return trending; }
    public void setTrending(boolean trending) { this.trending = trending; }
    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getSubgenre() { return subgenre; }
    public void setSubgenre(String subgenre) { this.subgenre = subgenre; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public boolean isExplicitContent() { return explicitContent; }
    public void setExplicitContent(boolean explicitContent) { this.explicitContent = explicitContent; }
    public String getReleaseType() { return releaseType; }
    public void setReleaseType(String releaseType) { this.releaseType = releaseType; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }

    public String publicId() { return "tr_" + id; }
}

package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_events", indexes = {
        @Index(name = "idx_user_events_key", columnList = "userKey"),
        @Index(name = "idx_user_events_type", columnList = "eventType"),
        @Index(name = "idx_user_events_time", columnList = "timestamp")
})
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userKey;

    @Column(nullable = false, length = 50)
    private String eventType; // PLAY_STARTED, PLAY_30_SECONDS, PLAY_COMPLETED, SKIP, LIKE, UNLIKE, SEARCH, VOICE_SEARCH, CHANGE_VIBE, MOOD_SELECTED, ENERGY_CHANGED, WHY_THIS_CLICKED, SURPRISE_ME

    private String trackId;
    private String trackTitle;
    private String artist;
    private String genre;
    private String language;
    private String mood;
    private Double energy;

    @Column(length = 2000)
    private String metadataJson;

    private LocalDateTime timestamp = LocalDateTime.now();

    public UserEvent() {}

    public UserEvent(String userKey, String eventType, String trackId, String trackTitle, String artist, String genre, String language, String mood, Double energy, String metadataJson) {
        this.userKey = userKey;
        this.eventType = eventType;
        this.trackId = trackId;
        this.trackTitle = trackTitle;
        this.artist = artist;
        this.genre = genre;
        this.language = language;
        this.mood = mood;
        this.energy = energy;
        this.metadataJson = metadataJson;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getTrackId() { return trackId; }
    public void setTrackId(String trackId) { this.trackId = trackId; }
    public String getTrackTitle() { return trackTitle; }
    public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public Double getEnergy() { return energy; }
    public void setEnergy(Double energy) { this.energy = energy; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
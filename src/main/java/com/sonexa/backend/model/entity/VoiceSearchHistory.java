package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_search_history", indexes = {
        @Index(name = "idx_voice_history_user", columnList = "userKey"),
        @Index(name = "idx_voice_history_time", columnList = "timestamp")
})
public class VoiceSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userKey;

    @Column(nullable = false, length = 500)
    private String transcript;

    @Column(length = 50)
    private String intentType; // PLAY_MUSIC, SEARCH, CHANGE_VIBE, ETC.

    @Column(length = 2000)
    private String structuredIntentJson;

    private String language;
    private LocalDateTime timestamp = LocalDateTime.now();

    public VoiceSearchHistory() {}

    public VoiceSearchHistory(String userKey, String transcript, String intentType, String structuredIntentJson, String language) {
        this.userKey = userKey;
        this.transcript = transcript;
        this.intentType = intentType;
        this.structuredIntentJson = structuredIntentJson;
        this.language = language;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }
    public String getIntentType() { return intentType; }
    public void setIntentType(String intentType) { this.intentType = intentType; }
    public String getStructuredIntentJson() { return structuredIntentJson; }
    public void setStructuredIntentJson(String structuredIntentJson) { this.structuredIntentJson = structuredIntentJson; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
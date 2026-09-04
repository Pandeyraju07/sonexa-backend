package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_taste_profiles", indexes = {
        @Index(name = "idx_taste_profile_user", columnList = "userKey", unique = true)
})
public class UserTasteProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userKey;

    @Column(columnDefinition = "TEXT")
    private String topGenresJson = "{}"; // e.g. {"Pop": 0.85, "Hip-Hop": 0.60}

    @Column(columnDefinition = "TEXT")
    private String topArtistsJson = "{}"; // e.g. {"Arijit Singh": 0.92, "The Weeknd": 0.70}

    @Column(columnDefinition = "TEXT")
    private String topLanguagesJson = "{}"; // e.g. {"Hindi": 0.80, "Punjabi": 0.50, "English": 0.90}

    @Column(columnDefinition = "TEXT")
    private String moodDistributionJson = "{}"; // e.g. {"Calm": 0.40, "Energetic": 0.60}

    private Double averageEnergy = 0.50; // 0.0 to 1.0
    private Double noveltyScore = 0.50; // 0.0 (strictly familiar) to 1.0 (heavy exploration)
    private Double discoveryScore = 0.50;
    private Double nostalgiaScore = 0.50;
    private Double romanceScore = 0.50;
    private Double mainstreamScore = 0.50;

    private Double skipRate = 0.10;
    private Double completionRate = 0.85;

    @Column(columnDefinition = "TEXT")
    private String timeOfDayPatternsJson = "{}"; // e.g. {"Morning": "Calm", "Night": "Acoustic"}

    private String personalityType = "Explorer"; // Explorer, Harmonizer, Trailblazer, Night Owl, Maestro, Purist

    private LocalDateTime lastUpdated = LocalDateTime.now();

    public UserTasteProfile() {}

    public UserTasteProfile(String userKey) {
        this.userKey = userKey;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getTopGenresJson() { return topGenresJson; }
    public void setTopGenresJson(String topGenresJson) { this.topGenresJson = topGenresJson; }
    public String getTopArtistsJson() { return topArtistsJson; }
    public void setTopArtistsJson(String topArtistsJson) { this.topArtistsJson = topArtistsJson; }
    public String getTopLanguagesJson() { return topLanguagesJson; }
    public void setTopLanguagesJson(String topLanguagesJson) { this.topLanguagesJson = topLanguagesJson; }
    public String getMoodDistributionJson() { return moodDistributionJson; }
    public void setMoodDistributionJson(String moodDistributionJson) { this.moodDistributionJson = moodDistributionJson; }
    public Double getAverageEnergy() { return averageEnergy; }
    public void setAverageEnergy(Double averageEnergy) { this.averageEnergy = averageEnergy; }
    public Double getNoveltyScore() { return noveltyScore; }
    public void setNoveltyScore(Double noveltyScore) { this.noveltyScore = noveltyScore; }
    public Double getDiscoveryScore() { return discoveryScore; }
    public void setDiscoveryScore(Double discoveryScore) { this.discoveryScore = discoveryScore; }
    public Double getNostalgiaScore() { return nostalgiaScore; }
    public void setNostalgiaScore(Double nostalgiaScore) { this.nostalgiaScore = nostalgiaScore; }
    public Double getRomanceScore() { return romanceScore; }
    public void setRomanceScore(Double romanceScore) { this.romanceScore = romanceScore; }
    public Double getMainstreamScore() { return mainstreamScore; }
    public void setMainstreamScore(Double mainstreamScore) { this.mainstreamScore = mainstreamScore; }
    public Double getSkipRate() { return skipRate; }
    public void setSkipRate(Double skipRate) { this.skipRate = skipRate; }
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    public String getTimeOfDayPatternsJson() { return timeOfDayPatternsJson; }
    public void setTimeOfDayPatternsJson(String timeOfDayPatternsJson) { this.timeOfDayPatternsJson = timeOfDayPatternsJson; }
    public String getPersonalityType() { return personalityType; }
    public void setPersonalityType(String personalityType) { this.personalityType = personalityType; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
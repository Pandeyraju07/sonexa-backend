package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "music_journeys", indexes = {
        @Index(name = "idx_journey_user", columnList = "userKey")
})
public class MusicJourney {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userKey;

    @Column(nullable = false)
    private String title;

    private String theme; // CALM_TO_ENERGETIC, ROAD_TRIP, WORKOUT, STUDY_FLOW
    private Integer totalDurationMinutes = 60;
    private Integer currentPhaseIndex = 0;

    @Column(length = 6000)
    private String phasesJson = "[]"; // List of MusicJourneyPhase objects

    private LocalDateTime createdAt = LocalDateTime.now();

    public MusicJourney() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(Integer totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }
    public Integer getCurrentPhaseIndex() { return currentPhaseIndex; }
    public void setCurrentPhaseIndex(Integer currentPhaseIndex) { this.currentPhaseIndex = currentPhaseIndex; }
    public String getPhasesJson() { return phasesJson; }
    public void setPhasesJson(String phasesJson) { this.phasesJson = phasesJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_checkins", indexes = {
        @Index(name = "idx_emotion_user", columnList = "userKey")
})
public class EmotionCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userKey;

    @Column(nullable = false, length = 50)
    private String emotion; // Happy, Calm, Romantic, Sad, Energetic, Tired, Focused

    private int intensity = 3; // 1 to 5

    private LocalDateTime timestamp = LocalDateTime.now();

    public EmotionCheckIn() {}

    public EmotionCheckIn(String userKey, String emotion, int intensity) {
        this.userKey = userKey;
        this.emotion = emotion;
        this.intensity = intensity;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    public int getIntensity() { return intensity; }
    public void setIntensity(int intensity) { this.intensity = intensity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
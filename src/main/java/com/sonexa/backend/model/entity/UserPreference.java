package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_pref_user_type", columnList = "userKey,prefType")
})
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userKey;

    @Column(nullable = false)
    private String prefType; // GENRE, ARTIST, MOOD, LANGUAGE, HANDLE, DISPLAY_NAME

    @Column(nullable = false)
    private String prefValue;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserPreference() {}

    public UserPreference(String userKey, String prefType, String prefValue) {
        this.userKey = userKey;
        this.prefType = prefType;
        this.prefValue = prefValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getPrefType() { return prefType; }
    public void setPrefType(String prefType) { this.prefType = prefType; }
    public String getPrefValue() { return prefValue; }
    public void setPrefValue(String prefValue) { this.prefValue = prefValue; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

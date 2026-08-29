package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_library_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userKey", "trackPublicId"})
})
public class UserLibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userKey;

    @Column(nullable = false)
    private String trackPublicId;

    private boolean liked;
    private boolean downloaded;
    private LocalDateTime lastPlayedAt;
    private Integer playCount = 0;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserLibraryItem() {}

    public UserLibraryItem(String userKey, String trackPublicId, boolean liked) {
        this.userKey = userKey;
        this.trackPublicId = trackPublicId;
        this.liked = liked;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getTrackPublicId() { return trackPublicId; }
    public void setTrackPublicId(String trackPublicId) { this.trackPublicId = trackPublicId; }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public boolean isDownloaded() { return downloaded; }
    public void setDownloaded(boolean downloaded) { this.downloaded = downloaded; }
    public LocalDateTime getLastPlayedAt() { return lastPlayedAt; }
    public void setLastPlayedAt(LocalDateTime lastPlayedAt) { this.lastPlayedAt = lastPlayedAt; }
    public Integer getPlayCount() { return playCount != null ? playCount : 0; }
    public void setPlayCount(Integer playCount) { this.playCount = playCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
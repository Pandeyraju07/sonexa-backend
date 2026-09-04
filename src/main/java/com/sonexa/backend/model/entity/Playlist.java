package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String subtitle;
    private String artworkType;
    private String coverUrl;
    private boolean madeForYou;

    private String userKey;
    private String creatorName;
    private boolean isPrivate;
    private boolean isPinned;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Playlist() {}

    public Playlist(String title, String subtitle, String artworkType, boolean madeForYou) {
        this.title = title;
        this.subtitle = subtitle;
        this.artworkType = artworkType;
        this.madeForYou = madeForYou;
        this.coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500";
    }

    public Playlist(String title, String subtitle, String coverUrl, String userKey, String creatorName) {
        this.title = title;
        this.subtitle = subtitle;
        this.coverUrl = (coverUrl != null && !coverUrl.isBlank()) ? coverUrl : "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500";
        this.artworkType = "custom";
        this.madeForYou = false;
        this.userKey = userKey;
        this.creatorName = creatorName;
        this.isPrivate = false;
        this.isPinned = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getArtworkType() { return artworkType; }
    public void setArtworkType(String artworkType) { this.artworkType = artworkType; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public boolean isMadeForYou() { return madeForYou; }
    public void setMadeForYou(boolean madeForYou) { this.madeForYou = madeForYou; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String publicId() { return "pl_" + id; }
}

package com.sonexa.backend.model.entity;

import jakarta.persistence.*;

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

    public Playlist() {}

    public Playlist(String title, String subtitle, String artworkType, boolean madeForYou) {
        this.title = title;
        this.subtitle = subtitle;
        this.artworkType = artworkType;
        this.madeForYou = madeForYou;
        this.coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500";
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

    public String publicId() { return "pl_" + id; }
}

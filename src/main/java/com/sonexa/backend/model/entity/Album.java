package com.sonexa.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String artistName;

    /** Mapped away from reserved SQL keyword YEAR. */
    @Column(name = "release_year")
    private String year;

    private String coverUrl;
    private int trackCount;

    public Album() {}

    public Album(String title, String artistName, String year, String coverUrl, int trackCount) {
        this.title = title;
        this.artistName = artistName;
        this.year = year;
        this.coverUrl = coverUrl;
        this.trackCount = trackCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }

    public String publicId() { return "alb_" + id; }
}

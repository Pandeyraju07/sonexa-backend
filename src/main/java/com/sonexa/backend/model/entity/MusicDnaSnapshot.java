package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "music_dna_snapshots", indexes = {
        @Index(name = "idx_dna_user", columnList = "userKey")
})
public class MusicDnaSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userKey;

    private String personality = "Explorer";
    private int energy = 70;
    private int discovery = 65;
    private int nostalgia = 80;
    private int romance = 60;
    private int mainstream = 45;

    @Column(length = 2000)
    private String topGenresJson = "[]";

    @Column(length = 2000)
    private String topLanguagesJson = "[]";

    @Column(length = 2000)
    private String topArtistsJson = "[]";

    private LocalDateTime createdAt = LocalDateTime.now();

    public MusicDnaSnapshot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = personality; }
    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = energy; }
    public int getDiscovery() { return discovery; }
    public void setDiscovery(int discovery) { this.discovery = discovery; }
    public int getNostalgia() { return nostalgia; }
    public void setNostalgia(int nostalgia) { this.nostalgia = nostalgia; }
    public int getRomance() { return romance; }
    public void setRomance(int romance) { this.romance = romance; }
    public int getMainstream() { return mainstream; }
    public void setMainstream(int mainstream) { this.mainstream = mainstream; }
    public String getTopGenresJson() { return topGenresJson; }
    public void setTopGenresJson(String topGenresJson) { this.topGenresJson = topGenresJson; }
    public String getTopLanguagesJson() { return topLanguagesJson; }
    public void setTopLanguagesJson(String topLanguagesJson) { this.topLanguagesJson = topLanguagesJson; }
    public String getTopArtistsJson() { return topArtistsJson; }
    public void setTopArtistsJson(String topArtistsJson) { this.topArtistsJson = topArtistsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
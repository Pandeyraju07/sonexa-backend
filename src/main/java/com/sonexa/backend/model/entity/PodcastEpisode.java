package com.sonexa.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "podcast_episodes")
public class PodcastEpisode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long podcastId;

    @Column(nullable = false)
    private String title;

    private String description;
    private String durationLabel;
    private String audioUrl;
    private int episodeNumber;

    public PodcastEpisode() {}

    public PodcastEpisode(Long podcastId, String title, String description, String durationLabel, int episodeNumber) {
        this.podcastId = podcastId;
        this.title = title;
        this.description = description;
        this.durationLabel = durationLabel;
        this.episodeNumber = episodeNumber;
        this.audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPodcastId() { return podcastId; }
    public void setPodcastId(Long podcastId) { this.podcastId = podcastId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDurationLabel() { return durationLabel; }
    public void setDurationLabel(String durationLabel) { this.durationLabel = durationLabel; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public int getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(int episodeNumber) { this.episodeNumber = episodeNumber; }

    public String publicId() { return "ep_" + id; }
}

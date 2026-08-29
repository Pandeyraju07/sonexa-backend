package com.sonexa.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "podcasts")
public class Podcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String host;
    private String description;
    private String coverUrl;
    private String category;

    public Podcast() {}

    public Podcast(String title, String host, String description, String coverUrl, String category) {
        this.title = title;
        this.host = host;
        this.description = description;
        this.coverUrl = coverUrl;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String publicId() { return "pod_" + id; }
}

package com.sonexa.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String genre;
    private String bio;
    private String imageUrl;
    private String color1;
    private String color2;
    private int followersCount;
    private boolean verified;

    public Artist() {}

    public Artist(String name, String genre, String color1, String color2, String imageUrl) {
        this.name = name;
        this.genre = genre;
        this.color1 = color1;
        this.color2 = color2;
        this.imageUrl = imageUrl;
        this.verified = true;
        this.followersCount = 100_000;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getColor1() { return color1; }
    public void setColor1(String color1) { this.color1 = color1; }
    public String getColor2() { return color2; }
    public void setColor2(String color2) { this.color2 = color2; }
    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String publicId() { return "art_" + id; }
}

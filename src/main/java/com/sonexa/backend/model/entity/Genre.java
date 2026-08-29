package com.sonexa.backend.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String color1;
    private String color2;
    private String imageUrl;
    private int sortOrder;

    public Genre() {}

    public Genre(String name, String color1, String color2, int sortOrder) {
        this.name = name;
        this.color1 = color1;
        this.color2 = color2;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor1() { return color1; }
    public void setColor1(String color1) { this.color1 = color1; }
    public String getColor2() { return color2; }
    public void setColor2(String color2) { this.color2 = color2; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}

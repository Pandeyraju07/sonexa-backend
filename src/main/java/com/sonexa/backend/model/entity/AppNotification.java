package com.sonexa.backend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_notifications")
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userKey; // email or "global"

    @Column(nullable = false)
    private String title;

    private String message;
    private String iconKey;
    private String colorHex;
    private String timeAgo;
    private boolean readFlag;
    private LocalDateTime createdAt = LocalDateTime.now();

    public AppNotification() {}

    public AppNotification(String userKey, String title, String message, String iconKey, String colorHex, String timeAgo) {
        this.userKey = userKey;
        this.title = title;
        this.message = message;
        this.iconKey = iconKey;
        this.colorHex = colorHex;
        this.timeAgo = timeAgo;
        this.readFlag = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public boolean isReadFlag() { return readFlag; }
    public void setReadFlag(boolean readFlag) { this.readFlag = readFlag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String publicId() { return "notif_" + id; }
}

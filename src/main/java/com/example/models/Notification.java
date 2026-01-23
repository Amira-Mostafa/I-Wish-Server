package com.example.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Notification {
    private int notificationId;
    private int receiverId;
    private Integer wishId;
    private String type;
    private String message;
    private String isRead;
    private LocalDateTime createdAt;
    
    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(int notificationId, int receiverId, Integer wishId, String type, String message, String isRead) {
        this.notificationId = notificationId;
        this.receiverId = receiverId;
        this.wishId = wishId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor without wishId (for friend requests)
    public Notification(int notificationId, int receiverId, String type, String message, String isRead) {
        this(notificationId, receiverId, null, type, message, isRead);
    }
    
    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }
    
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    
    public Integer getWishId() { return wishId; }
    public void setWishId(Integer wishId) { this.wishId = wishId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getIsRead() { return isRead; }
    public void setIsRead(String isRead) { this.isRead = isRead; }
    
    public boolean isRead() { return "Y".equals(isRead); }
    public void setRead(boolean read) { this.isRead = read ? "Y" : "N"; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Helper method to get time ago
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        long days = ChronoUnit.DAYS.between(createdAt, now);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        } else {
            return createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
    }
}
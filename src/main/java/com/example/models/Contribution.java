package com.example.models;

import java.time.LocalDateTime;

public class Contribution {
    private int contributionId;
    private int userId;
    private int wishId;
    private double amount;
    private String message;
    private LocalDateTime contributedAt;
    
    // Constructors
    public Contribution() {
        this.contributedAt = LocalDateTime.now();
    }
    
    public Contribution(int contributionId, int userId, int wishId, double amount, String message) {
        this.contributionId = contributionId;
        this.userId = userId;
        this.wishId = wishId;
        this.amount = amount;
        this.message = message;
        this.contributedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getContributionId() { return contributionId; }
    public void setContributionId(int contributionId) { this.contributionId = contributionId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getWishId() { return wishId; }
    public void setWishId(int wishId) { this.wishId = wishId; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getContributedAt() { return contributedAt; }
    public void setContributedAt(LocalDateTime contributedAt) { this.contributedAt = contributedAt; }
}
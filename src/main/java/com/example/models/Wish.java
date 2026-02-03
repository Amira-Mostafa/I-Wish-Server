package com.example.models;

public class Wish {
    private int wishId;
    private int userId;
    private String name;
    private String description;
    private double price;
    private double raisedAmount;
    private String imagePath;
    private String isCompleted;
    
    public Wish() {}
    
    public Wish(int wishId, int userId, String name, String description, double price, String imagePath, String isCompleted) {
        this.wishId = wishId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imagePath = imagePath;
        this.isCompleted = isCompleted;
        this.raisedAmount = 0.0;
    }
    
    public Wish(int wishId, int userId, String name, String description, double price, String isCompleted) {
        this(wishId, userId, name, description, price, "", isCompleted);
    }
    
    public int getWishId() { return wishId; }
    public void setWishId(int wishId) { this.wishId = wishId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getRaisedAmount() { return raisedAmount; }
    public void setRaisedAmount(double raisedAmount) { this.raisedAmount = raisedAmount; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public String getIsCompleted() { return isCompleted; }
    public void setIsCompleted(String isCompleted) { this.isCompleted = isCompleted; }
    
    public boolean isCompleted() { return "Y".equals(isCompleted); }
    public void setCompleted(boolean completed) { this.isCompleted = completed ? "Y" : "N"; }
    
    public double getRemainingAmount() {
        return Math.max(0, price - raisedAmount);
    }
    
    public double getProgress() {
        if (price == 0) return 0;
        return Math.min(1.0, raisedAmount / price);
    }
    
    public int getContributorCount() {
        // This would come from the database
        return 0;
    }
}

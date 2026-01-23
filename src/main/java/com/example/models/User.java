package com.example.models;

public class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String imagePath;
    
    // Constructors
    public User() {}
    
    public User(int userId, String name, String email, String imagePath) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.imagePath = imagePath;
    }
    
    public User(int userId, String name, String email) {
        this(userId, name, email, "");
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
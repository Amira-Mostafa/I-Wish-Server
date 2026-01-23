package com.example.models;

public class UserFriend {
    private int requesterId;
    private int receiverId;
    private String status;
    
    // Constructors
    public UserFriend() {}
    
    public UserFriend(int requesterId, int receiverId, String status) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status;
    }
    
    // Getters and Setters
    public int getRequesterId() { return requesterId; }
    public void setRequesterId(int requesterId) { this.requesterId = requesterId; }
    
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
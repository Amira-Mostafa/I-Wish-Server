package com.example.models;

public class FriendRequest {
    private int requesterId;
    private int receiverId;
    private String status;
    private String requesterName;
    
    // Constructors
    public FriendRequest() {}
    
    public FriendRequest(int requesterId, int receiverId, String status, String requesterName) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status;
        this.requesterName = requesterName;
    }
    
    // Getters and Setters
    public int getRequesterId() { return requesterId; }
    public void setRequesterId(int requesterId) { this.requesterId = requesterId; }
    
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
}
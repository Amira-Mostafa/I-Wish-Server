package com.example.server.services;

import java.util.List;
import com.example.models.FriendRequest;
import com.example.models.User;

public class FriendService {
    
    private DataManager dataManager = DataManager.getInstance();
    
    public String getFriends(int userId) {
        setCurrentUser(userId);
        List<User> friends = dataManager.getFriends();
        return formatUserList(friends);
    }
    
    public String getAllUsers(int userId) {
        System.out.println("=== FriendService.getAllUsers ===");
        System.out.println("UserId: " + userId);
        
        setCurrentUser(userId);
        User currentUser = dataManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("ERROR: Failed to set current user for userId: " + userId);
            return "ERROR|Failed to load user";
        }
        
        List<User> users = dataManager.getAllUsers();
        System.out.println("DataManager returned " + users.size() + " users");
        return formatUserList(users);
    }
    
    public String searchUsers(int userId, String query) {
        System.out.println("=== FriendService.searchUsers ===");
        System.out.println("UserId: " + userId + ", Query: '" + query + "'");
        
        setCurrentUser(userId);
        User currentUser = dataManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("ERROR: Failed to set current user for userId: " + userId);
            return "ERROR|Failed to load user";
        }
        System.out.println("Current user set: " + currentUser.getName() + " (ID: " + currentUser.getUserId() + ")");
        
        List<User> users = dataManager.searchUsers(query != null ? query : "");
        System.out.println("DataManager returned " + users.size() + " users");
        
        String response = formatUserList(users);
        System.out.println("Formatted response length: " + response.length());
        return response;
    }
    
    public String getPendingFriendRequests(int userId) {
        setCurrentUser(userId);
        List<FriendRequest> requests = dataManager.getPendingFriendRequests();
        if (requests.isEmpty()) {
            return "SUCCESS|";
        }
        
        StringBuilder sb = new StringBuilder("SUCCESS|");
        for (int i = 0; i < requests.size(); i++) {
            FriendRequest req = requests.get(i);
            if (i > 0) sb.append(";");
            sb.append(req.getRequesterId()).append(",")
              .append(req.getReceiverId()).append(",")
              .append(req.getStatus()).append(",")
              .append(req.getRequesterName());
        }
        return sb.toString();
    }
    
    public String sendFriendRequest(int userId, int receiverId) {
        System.out.println("=== FriendService.sendFriendRequest ===");
        System.out.println("UserId: " + userId + ", ReceiverId: " + receiverId);
        
        setCurrentUser(userId);
        User currentUser = dataManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("ERROR: Failed to set current user");
            return "ERROR|Failed to load user";
        }
        
        if (receiverId == userId) {
            return "ERROR|Cannot send friend request to yourself";
        }
        
        boolean success = dataManager.sendFriendRequest(receiverId);
        if (success) {
            System.out.println("Friend request sent successfully");
            return "SUCCESS";
        } else {
            System.err.println("Failed to send friend request");
            return "ERROR|Friend request already exists or failed to send";
        }
    }
    
    public String respondToFriendRequest(int userId, int requesterId, boolean accept) {
        setCurrentUser(userId);
        boolean success = dataManager.respondToFriendRequest(requesterId, accept);
        return success ? "SUCCESS" : "ERROR|Failed to respond to friend request";
    }
    
    public String removeFriend(int userId, int friendId) {
        setCurrentUser(userId);
        boolean success = dataManager.removeFriend(friendId);
        return success ? "SUCCESS" : "ERROR|Failed to remove friend";
    }
    
    private void setCurrentUser(int userId) {
        String sql = "SELECT user_id, name, email, image_path FROM users WHERE user_id = ?";
        try (var conn = DatabaseConnection.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("image_path")
                );
                dataManager.setCurrentUser(user);
                System.out.println("Successfully set current user: " + user.getName() + " (ID: " + user.getUserId() + ")");
            } else {
                System.err.println("ERROR: User with ID " + userId + " not found in database!");
            }
        } catch (Exception e) {
            System.err.println("ERROR setting current user for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String formatUserList(List<User> users) {
        if (users.isEmpty()) {
            return "SUCCESS|";
        }
        
        StringBuilder sb = new StringBuilder("SUCCESS|");
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (i > 0) sb.append(";");
            sb.append(user.getUserId()).append(",")
              .append(user.getName()).append(",")
              .append(user.getEmail()).append(",")
              .append(user.getImagePath() != null ? user.getImagePath() : "");
        }
        return sb.toString();
    }
}

package com.example.server.services;

import java.util.List;
import com.example.models.User;
import com.example.models.Wish;

public class WishService {
    
    // Remove the singleton pattern from DataManager or create new instance per call
    public String getMyWishes(int userId) {
        try {
            // Get connection first
            var conn = DatabaseConnection.getConnection();
            
            // Create DataManager and set user
            DataManager dataManager = DataManager.getInstance();
            
            // Get user from database
            String sql = "SELECT user_id, name, email, image_path FROM users WHERE user_id = ?";
            try (var pstmt = conn.prepareStatement(sql)) {
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
                } else {
                    return "ERROR|User not found";
                }
            }
            
            List<Wish> wishes = dataManager.getCurrentUserWishes();
            return formatWishList(wishes);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    
    public String getFriendWishes(int userId, int friendId) {
        try {
            var conn = DatabaseConnection.getConnection();
            DataManager dataManager = DataManager.getInstance();
            
            String sql = "SELECT user_id, name, email, image_path FROM users WHERE user_id = ?";
            try (var pstmt = conn.prepareStatement(sql)) {
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
                } else {
                    return "ERROR|User not found";
                }
            }
            
            List<Wish> wishes = dataManager.getFriendWishes(friendId);
            return formatWishList(wishes);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    
    public String createWish(int userId, String name, String description, double price, String imageUrl) {
        try {
            var conn = DatabaseConnection.getConnection();
            DataManager dataManager = DataManager.getInstance();
            
            String sql = "SELECT user_id, name, email, image_path FROM users WHERE user_id = ?";
            try (var pstmt = conn.prepareStatement(sql)) {
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
                } else {
                    return "ERROR|User not found";
                }
            }
            
            Wish wish = dataManager.createWish(name, description, price, imageUrl);
            if (wish != null) {
                return "SUCCESS|" + wish.getWishId() + "|" + wish.getUserId() + "|" + 
                       wish.getName() + "|" + wish.getDescription() + "|" + 
                       wish.getPrice() + "|" + (wish.getImagePath() != null ? wish.getImagePath() : "") + "|" +
                       (wish.isCompleted() ? "Y" : "N");
            }
            return "ERROR|Failed to create wish";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    
    public String updateWish(int userId, int wishId, String name, String description, double price, String imageUrl) {
        try {
            var conn = DatabaseConnection.getConnection();
            DataManager dataManager = DataManager.getInstance();
            
            String sql = "SELECT user_id, name, email, image_path FROM users WHERE user_id = ?";
            try (var pstmt = conn.prepareStatement(sql)) {
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
                } else {
                    return "ERROR|User not found";
                }
            }
            
            boolean success = dataManager.updateWish(wishId, name, description, price, imageUrl);
            return success ? "SUCCESS" : "ERROR|Failed to update wish";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    
    public String deleteWish(int userId, int wishId) {
        try {
            var conn = DatabaseConnection.getConnection();
            DataManager dataManager = DataManager.getInstance();
            
            String sql = "SELECT user_id, name, email, image_path FROM users WHERE user_id = ?";
            try (var pstmt = conn.prepareStatement(sql)) {
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
                } else {
                    return "ERROR|User not found";
                }
            }
            
            boolean success = dataManager.deleteWish(wishId);
            return success ? "SUCCESS" : "ERROR|Failed to delete wish";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    
    private String formatWishList(List<Wish> wishes) {
        if (wishes.isEmpty()) {
            return "SUCCESS|";
        }
        
        StringBuilder sb = new StringBuilder("SUCCESS|");
        for (int i = 0; i < wishes.size(); i++) {
            Wish wish = wishes.get(i);
            if (i > 0) sb.append(";");
            sb.append(wish.getWishId()).append(",")
              .append(wish.getUserId()).append(",")
              .append(wish.getName()).append(",")
              .append(wish.getDescription() != null ? wish.getDescription() : "").append(",")
              .append(wish.getPrice()).append(",")
              .append(wish.getRaisedAmount()).append(",")
              .append(wish.getImagePath() != null ? wish.getImagePath() : "").append(",")
              .append(wish.isCompleted() ? "Y" : "N");
        }
        return sb.toString();
    }
}
package com.example.server.services;

import java.util.List;
import com.example.models.Notification;
import com.example.models.User;

public class NotificationService {
    
    private DataManager dataManager = DataManager.getInstance();
    
    public String getUserNotifications(int userId) {
        setCurrentUser(userId);
        List<Notification> notifications = dataManager.getUserNotifications();
        return formatNotificationList(notifications);
    }
    
    public String getUnreadNotificationCount(int userId) {
        setCurrentUser(userId);
        int count = dataManager.getUnreadNotificationCount();
        return "SUCCESS|" + count;
    }
    
    public String markNotificationAsRead(int userId, int notificationId) {
        setCurrentUser(userId);
        dataManager.markNotificationAsRead(notificationId);
        return "SUCCESS";
    }
    
    public String markAllNotificationsAsRead(int userId) {
        setCurrentUser(userId);
        dataManager.markAllNotificationsAsRead();
        return "SUCCESS";
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String formatNotificationList(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return "SUCCESS|";
        }
        
        StringBuilder sb = new StringBuilder("SUCCESS|");
        for (int i = 0; i < notifications.size(); i++) {
            Notification notif = notifications.get(i);
            if (i > 0) sb.append(";");
            sb.append(notif.getNotificationId()).append(",")
              .append(notif.getReceiverId()).append(",")
              .append(notif.getWishId()).append(",")
              .append(notif.getType()).append(",")
              .append(notif.getMessage()).append(",")
              .append(notif.isRead() ? "Y" : "N");
        }
        return sb.toString();
    }
}

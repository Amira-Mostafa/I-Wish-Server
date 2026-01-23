package tete.Database.DAOs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tete.Database.POJOs.Notification;

public class NotificationDAO {

    private Connection con;

    public NotificationDAO(Connection con) {
        this.con = con;
    }

    // Add a notification
    public int addNotification(Notification n) throws SQLException {

        int notificationId;

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT notifications_seq.NEXTVAL AS id FROM dual"
        );

        if (rs.next()) {
            notificationId = rs.getInt("id");
        } else {
            throw new SQLException("Failed to generate notification ID");
        }

        rs.close();
        stmt.close();

        n.setNotificationId(notificationId);

        String sql
                = "INSERT INTO notifications (notification_id, receiver_id, wish_id, type, message, is_read) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, notificationId);
        ps.setInt(2, n.getReceiverId());

        if (n.getWishId() != null) {
            ps.setInt(3, n.getWishId());
        } else {
            ps.setNull(3, Types.INTEGER);
        }

        ps.setString(4, n.getType());
        ps.setString(5, n.getMessage());
        ps.setString(6, String.valueOf(n.getIsRead()));

        ps.executeUpdate();
        ps.close();

        return notificationId;
    }

    // Get notifications for a user
    public List<Notification> getNotificationsByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE receiver_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        List<Notification> notifications = new ArrayList<>();
        while (rs.next()) {
            notifications.add(new Notification(
                    rs.getInt("notification_id"),
                    rs.getInt("receiver_id"),
                    rs.getObject("wish_id") != null ? rs.getInt("wish_id") : null,
                    rs.getString("type"),
                    rs.getString("message"),
                    rs.getString("is_read").charAt(0)
            ));
        }
        rs.close();
        ps.close();
        return notifications;
    }

    // Mark as read
    public void markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read='Y' WHERE notification_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, notificationId);
        ps.executeUpdate();
        ps.close();
    }

    // Delete notification
    public void deleteNotification(int notificationId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE notification_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, notificationId);
        ps.executeUpdate();
        ps.close();
    }
}

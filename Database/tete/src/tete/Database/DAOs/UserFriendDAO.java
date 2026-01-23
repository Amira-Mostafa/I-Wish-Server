
package tete.Database.DAOs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tete.Database.POJOs.UserFriend;

public class UserFriendDAO {
    private Connection con;

    public UserFriendDAO(Connection con) {
        this.con = con;
    }

    // Add friend request
    public void addFriendRequest(UserFriend uf) throws SQLException {
        String sql = "INSERT INTO users_friends (requester_id, receiver_id, status) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, uf.getRequesterId());
        ps.setInt(2, uf.getReceiverId());
        ps.setString(3, uf.getStatus()); // PENDING
        ps.executeUpdate();
        ps.close();
    }

    // Update friend status (ACCEPTED / DECLINED)
    public void updateFriendStatus(int requesterId, int receiverId, String status) throws SQLException {
        String sql = "UPDATE users_friends SET status=? WHERE requester_id=? AND receiver_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, requesterId);
        ps.setInt(3, receiverId);
        ps.executeUpdate();
        ps.close();
    }

    // Delete friend relationship
    public void deleteFriend(int requesterId, int receiverId) throws SQLException {
        String sql = "DELETE FROM users_friends WHERE requester_id=? AND receiver_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, requesterId);
        ps.setInt(2, receiverId);
        ps.executeUpdate();
        ps.close();
    }

    // Get all friends of a user (ACCEPTED only)
    public List<UserFriend> getFriendsOfUser(int userId) throws SQLException {
        String sql = "SELECT * FROM users_friends WHERE (requester_id=? OR receiver_id=?) AND status='ACCEPTED'";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();
        List<UserFriend> friends = new ArrayList<>();
        while(rs.next()) {
            friends.add(new UserFriend(
                rs.getInt("requester_id"),
                rs.getInt("receiver_id"),
                rs.getString("status")
            ));
        }
        rs.close();
        ps.close();
        return friends;
    }

    // Get pending friend requests for a user
    public List<UserFriend> getPendingRequests(int userId) throws SQLException {
        String sql = "SELECT * FROM users_friends WHERE receiver_id=? AND status='PENDING'";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        List<UserFriend> requests = new ArrayList<>();
        while(rs.next()) {
            requests.add(new UserFriend(
                rs.getInt("requester_id"),
                rs.getInt("receiver_id"),
                rs.getString("status")
            ));
        }
        rs.close();
        ps.close();
        return requests;
    }
}

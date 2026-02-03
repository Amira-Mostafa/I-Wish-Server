package com.example.server.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.lang.ThreadLocal;

import com.example.models.Contribution;
import com.example.models.FriendRequest;
import com.example.models.Notification;
import com.example.models.User;
import com.example.models.Wish;

public class DataManager {
    private static DataManager instance;
    private static final ThreadLocal<User> currentUserThreadLocal = new ThreadLocal<>();
    
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    private User getCurrentUserThreadLocal() {
        return currentUserThreadLocal.get();
    }
    
    private void setCurrentUserThreadLocal(User user) {
        currentUserThreadLocal.set(user);
    }
    
    private User requireCurrentUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Current user is not set. Call setCurrentUser() first.");
        }
        return user;
    }

    
    public boolean login(String email, String password) {
        System.out.println("=== DataManager.login() ===");
        System.out.println("Attempting login for email: " + email);
        
        String sql = "SELECT user_id, name, email, image_path FROM users WHERE email = ? AND password_hash = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("image_path")
                );
                setCurrentUser(user);
                System.out.println("✓ Login successful!");
                System.out.println("  User ID: " + user.getUserId());
                System.out.println("  Name: " + user.getName());
                System.out.println("  Email: " + user.getEmail());
                return true;
            } else {
                System.out.println("✗ Login failed - incorrect email or password");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean register(String name, String email, String password, String imagePath) {
        System.out.println("=== DataManager.register() ===");
        System.out.println("Registering: " + name + " (" + email + ")");
        System.out.println("Image path: " + (imagePath != null && !imagePath.isEmpty() ? imagePath : "none"));
        
        String checkSql = "SELECT COUNT(*) as count FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("✗ Email already exists: " + email);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            return false;
        }
        
        String sql = "INSERT INTO users (user_id, name, email, password_hash, image_path) VALUES (users_seq.NEXTVAL, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                pstmt.setString(4, imagePath);
            } else {
                pstmt.setString(4, null);
            }
            
            int rows = pstmt.executeUpdate();
            System.out.println("Registration rows affected: " + rows);
            
            if (rows > 0) {

                String getUserIdSql = "SELECT user_id, name, email, image_path FROM users WHERE email = ?";
                try (PreparedStatement getUserStmt = conn.prepareStatement(getUserIdSql)) {
                    getUserStmt.setString(1, email);
                    ResultSet userRs = getUserStmt.executeQuery();
                    
                    if (userRs.next()) {
                        User user = new User(
                            userRs.getInt("user_id"),
                            userRs.getString("name"),
                            userRs.getString("email"),
                            userRs.getString("image_path")
                        );
                        setCurrentUser(user);
                        System.out.println("✓ Registration successful!");
                        System.out.println("  User ID: " + user.getUserId());
                        System.out.println("  Name: " + user.getName());
                        System.out.println("  Image Path: " + (user.getImagePath() != null ? user.getImagePath() : "none"));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public User getCurrentUser() {
        return getCurrentUserThreadLocal();
    }
    
    public void setCurrentUser(User user) {
        setCurrentUserThreadLocal(user);
    }
    
    
    public void logout() {
        System.out.println("=== DataManager.logout() ===");
        User user = getCurrentUser();
        System.out.println("Logging out user: " + (user != null ? user.getName() : "none"));
        setCurrentUser(null);
        currentUserThreadLocal.remove(); 
    }
    
    
    public List<User> getFriends() {
        List<User> friends = new ArrayList<>();
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            System.err.println("ERROR: currentUser is null in getFriends");
            return friends;
        }
        
        String sql = "SELECT u.user_id, u.name, u.email, u.image_path " +
                     "FROM users u " +
                     "JOIN users_friends uf ON (" +
                     "    (uf.requester_id = u.user_id AND uf.receiver_id = ?) OR " +
                     "    (uf.receiver_id = u.user_id AND uf.requester_id = ?)" +
                     ") " +
                     "WHERE uf.status = 'ACCEPTED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setInt(2, currentUser.getUserId());
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                friends.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("image_path")
                ));
            }
            System.out.println("Found " + friends.size() + " friends for user: " + currentUser.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }
    
    public List<User> getAllUsers() {
        List<User> results = new ArrayList<>();
        User currentUser = getCurrentUser();
        
        if (currentUser == null) {
            System.err.println("ERROR: currentUser is null in getAllUsers");
            return results;
        }
        
        System.out.println("=== getAllUsers called ===");
        System.out.println("Current User ID: " + currentUser.getUserId());
        
        String sql = "SELECT u.user_id, u.name, u.email, u.image_path " +
                     "FROM users u " +
                     "WHERE u.user_id != ? " +
                     "  AND NOT EXISTS (" +
                     "      SELECT 1 FROM users_friends uf " +
                     "      WHERE ((uf.requester_id = u.user_id AND uf.receiver_id = ?) " +
                     "             OR (uf.receiver_id = u.user_id AND uf.requester_id = ?)) " +
                     "        AND uf.status = 'ACCEPTED'" +
                     "  ) " +
                     "ORDER BY u.name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setInt(2, currentUser.getUserId());
            pstmt.setInt(3, currentUser.getUserId());
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("image_path")
                );
                results.add(user);
            }
            System.out.println("Found " + results.size() + " users (excluding self and accepted friends)");
        } catch (SQLException e) {
            System.err.println("Error in getAllUsers: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
    
    public List<User> searchUsers(String query) {
        List<User> results = new ArrayList<>();
        User currentUser = getCurrentUser();
        
        if (currentUser == null) {
            System.err.println("ERROR: currentUser is null in searchUsers");
            return results;
        }
        
        System.out.println("=== searchUsers called ===");
        System.out.println("Query: '" + query + "'");
        System.out.println("Current User ID: " + currentUser.getUserId());
        
        if (query == null || query.trim().isEmpty()) {
            System.out.println("Query is empty, returning all users");
            return getAllUsers();
        }
        
        String sql = "SELECT u.user_id, u.name, u.email, u.image_path " +
                     "FROM users u " +
                     "WHERE (LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ?) " +
                     "  AND u.user_id != ? " +
                     "  AND NOT EXISTS (" +
                     "      SELECT 1 FROM users_friends uf " +
                     "      WHERE ((uf.requester_id = u.user_id AND uf.receiver_id = ?) " +
                     "             OR (uf.receiver_id = u.user_id AND uf.requester_id = ?)) " +
                     "        AND uf.status = 'ACCEPTED'" +
                     "  ) " +
                     "ORDER BY u.name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchTerm = "%" + query.toLowerCase().trim() + "%";
            System.out.println("Search term: '" + searchTerm + "'");
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            pstmt.setInt(3, currentUser.getUserId());
            pstmt.setInt(4, currentUser.getUserId());
            pstmt.setInt(5, currentUser.getUserId());
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("image_path")
                );
                results.add(user);
            }
            System.out.println("Search for '" + query + "' returned " + results.size() + " results");
        } catch (SQLException e) {
            System.err.println("Error in searchUsers: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
    
    public List<FriendRequest> getPendingFriendRequests() {
        List<FriendRequest> requests = new ArrayList<>();
        User currentUser = requireCurrentUser();
        String sql = "SELECT uf.requester_id, uf.receiver_id, uf.status, u.name as requester_name " +
                     "FROM users_friends uf " +
                     "JOIN users u ON u.user_id = uf.requester_id " +
                     "WHERE uf.receiver_id = ? AND uf.status = 'PENDING'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(new FriendRequest(
                    rs.getInt("requester_id"),
                    rs.getInt("receiver_id"),
                    rs.getString("status"),
                    rs.getString("requester_name")
                ));
            }
            System.out.println("Found " + requests.size() + " pending friend requests");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    public boolean sendFriendRequest(int receiverId) {
        System.out.println("=== DataManager.sendFriendRequest ===");
        User currentUser = requireCurrentUser();
        System.out.println("Current user ID: " + currentUser.getUserId());
        System.out.println("Receiver ID: " + receiverId);
        
        if (receiverId == currentUser.getUserId()) {
            System.out.println("Cannot send friend request to yourself");
            return false;
        }
        
        String checkSql = "SELECT status FROM users_friends " +
                         "WHERE (requester_id = ? AND receiver_id = ?) OR (requester_id = ? AND receiver_id = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, currentUser.getUserId());
            checkStmt.setInt(2, receiverId);
            checkStmt.setInt(3, receiverId);
            checkStmt.setInt(4, currentUser.getUserId());
            
            var rs = checkStmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                System.out.println("Friend request or relationship already exists with status: " + status);
                if ("PENDING".equals(status)) {
                    System.out.println("A pending friend request already exists");
                } else if ("ACCEPTED".equals(status)) {
                    System.out.println("You are already friends with this user");
                } else if ("DECLINED".equals(status)) {
                    System.out.println("Previous friend request was declined - allowing new request");

                    String deleteSql = "DELETE FROM users_friends WHERE " +
                                     "((requester_id = ? AND receiver_id = ?) OR (requester_id = ? AND receiver_id = ?)) " +
                                     "AND status = 'DECLINED'";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, currentUser.getUserId());
                        deleteStmt.setInt(2, receiverId);
                        deleteStmt.setInt(3, receiverId);
                        deleteStmt.setInt(4, currentUser.getUserId());
                        deleteStmt.executeUpdate();
                        System.out.println("Deleted old declined request, proceeding with new request");
                    }
                } else {
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking existing friend request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
        String sql = "INSERT INTO users_friends (requester_id, receiver_id, status) VALUES (?, ?, 'PENDING')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setInt(2, receiverId);
            
            int rows = pstmt.executeUpdate();
            System.out.println("Friend request sent from " + currentUser.getUserId() + " to " + receiverId + ", rows affected: " + rows);
            if (rows > 0) {
                System.out.println("✓ Friend request successfully created");
                return true;
            } else {
                System.err.println("✗ No rows affected - friend request not created");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error sending friend request: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("unique constraint")) {
                System.err.println("Duplicate friend request detected");
            }
        }
        return false;
    }
    
    public boolean respondToFriendRequest(int requesterId, boolean accept) {
        User currentUser = requireCurrentUser();
        String sql = "UPDATE users_friends SET status = ? WHERE requester_id = ? AND receiver_id = ? AND status = 'PENDING'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accept ? "ACCEPTED" : "DECLINED");
            pstmt.setInt(2, requesterId);
            pstmt.setInt(3, currentUser.getUserId());
            
            int rows = pstmt.executeUpdate();
            System.out.println("Friend request response: " + (accept ? "ACCEPTED" : "DECLINED") + 
                             " from " + requesterId + ", rows: " + rows);
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean removeFriend(int friendId) {
        User currentUser = requireCurrentUser();
        String sql = "DELETE FROM users_friends " +
                     "WHERE (requester_id = ? AND receiver_id = ?) " +
                     "   OR (requester_id = ? AND receiver_id = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, currentUser.getUserId());
            
            int rows = pstmt.executeUpdate();
            System.out.println("Removed friend " + friendId + ", rows: " + rows);
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    public List<Wish> getCurrentUserWishes() {
        List<Wish> wishes = new ArrayList<>();
        User currentUser = requireCurrentUser();
        String sql = "SELECT w.*, " +
                     "       COALESCE(SUM(uc.amount), 0) as raised_amount " +
                     "FROM wishes w " +
                     "LEFT JOIN users_contributions uc ON w.wish_id = uc.wish_id " +
                     "WHERE w.user_id = ? " +
                     "GROUP BY w.wish_id, w.user_id, w.name, w.description, w.price, w.image_path, w.is_completed " +
                     "ORDER BY w.wish_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Wish wish = createWishFromResultSet(rs);
                wish.setRaisedAmount(rs.getDouble("raised_amount"));
                wishes.add(wish);
            }
            System.out.println("Found " + wishes.size() + " wishes for user: " + currentUser.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wishes;
    }
    
    public List<Wish> getFriendWishes(int friendId) {
        List<Wish> wishes = new ArrayList<>();
        String sql = "SELECT w.*, " +
                     "       COALESCE(SUM(uc.amount), 0) as raised_amount " +
                     "FROM wishes w " +
                     "LEFT JOIN users_contributions uc ON w.wish_id = uc.wish_id " +
                     "WHERE w.user_id = ? " +
                     "GROUP BY w.wish_id, w.user_id, w.name, w.description, w.price, w.image_path, w.is_completed " +
                     "ORDER BY w.wish_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, friendId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Wish wish = createWishFromResultSet(rs);
                wish.setRaisedAmount(rs.getDouble("raised_amount"));
                wishes.add(wish);
            }
            System.out.println("Found " + wishes.size() + " wishes for friend: " + friendId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wishes;
    }
    
    public Wish createWish(String name, String description, double price, String imageUrl) {
        System.out.println("Creating wish: " + name + ", price: " + price);
        User currentUser = requireCurrentUser();
        
        String sql = "INSERT INTO wishes (user_id, name, description, price, image_path) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setBigDecimal(4, new BigDecimal(price));
            pstmt.setString(5, imageUrl);
            
            int rows = pstmt.executeUpdate();
            System.out.println("Wish creation rows: " + rows);
            
            if (rows > 0) {
                String getIdSql = "SELECT MAX(wish_id) FROM wishes WHERE user_id = ?";
                try (PreparedStatement getIdStmt = conn.prepareStatement(getIdSql)) {
                    getIdStmt.setInt(1, currentUser.getUserId());
                    ResultSet rs = getIdStmt.executeQuery();
                    
                    if (rs.next()) {
                        int wishId = rs.getInt(1);
                        System.out.println("Created wish with ID: " + wishId);
                        return new Wish(wishId, currentUser.getUserId(), name, description, price, imageUrl, "N");
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error creating wish: " + e.getMessage());
        }
        return null;
    }
    
    public boolean updateWish(int wishId, String name, String description, double price, String imageUrl) {
        User currentUser = requireCurrentUser();
        
        String checkSql = "SELECT is_completed FROM wishes WHERE wish_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, wishId);
            checkStmt.setInt(2, currentUser.getUserId());
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String isCompleted = rs.getString("is_completed");
                    if ("Y".equals(isCompleted)) {
                        System.out.println("Cannot update wish " + wishId + " - it is already completed");
                        return false; 
                    }
                } else {
                    System.out.println("Wish " + wishId + " not found or not owned by user");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        String sql = "UPDATE wishes SET name = ?, description = ?, price = ?, image_path = ? WHERE wish_id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setBigDecimal(3, new BigDecimal(price));
            pstmt.setString(4, imageUrl);
            pstmt.setInt(5, wishId);
            pstmt.setInt(6, currentUser.getUserId());
            
            int rows = pstmt.executeUpdate();
            System.out.println("Updated wish " + wishId + ", rows: " + rows);
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteWish(int wishId) {
        User currentUser = requireCurrentUser();
        
        String checkSql = "SELECT is_completed FROM wishes WHERE wish_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, wishId);
            checkStmt.setInt(2, currentUser.getUserId());
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String isCompleted = rs.getString("is_completed");
                    if ("Y".equals(isCompleted)) {
                        System.out.println("Cannot delete wish " + wishId + " - it is already completed");
                        return false; 
                    }
                } else {
                    System.out.println("Wish " + wishId + " not found or not owned by user");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        String sql = "DELETE FROM wishes WHERE wish_id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, wishId);
            pstmt.setInt(2, currentUser.getUserId());
            
            int rows = pstmt.executeUpdate();
            System.out.println("Deleted wish " + wishId + ", rows: " + rows);
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    private void createContributionNotification(Connection conn, int wishId, double amount, String wishName, int ownerId) throws SQLException {
        User currentUser = requireCurrentUser();
        if (ownerId != currentUser.getUserId()) {
            String notifSql = "INSERT INTO notifications (receiver_id, wish_id, type, message) " +
                             "VALUES (?, ?, 'WISH_BOUGHT', ?)";
            
            String message = currentUser.getName() + " contributed $" + 
                           String.format("%.2f", amount) + 
                           " to your wish: " + wishName;
            
            try (PreparedStatement notifStmt = conn.prepareStatement(notifSql)) {
                notifStmt.setInt(1, ownerId);
                notifStmt.setInt(2, wishId);
                notifStmt.setString(3, message);
                notifStmt.executeUpdate();
                System.out.println("Created contribution notification");
            }
        }
    }
    
    
    public List<Notification> getUserNotifications() {
        List<Notification> notifications = new ArrayList<>();
        User currentUser = requireCurrentUser();
        String sql = "SELECT n.*, w.name as wish_name " +
                     "FROM notifications n " +
                     "LEFT JOIN wishes w ON n.wish_id = w.wish_id " +
                     "WHERE n.receiver_id = ? " +
                     "ORDER BY n.notification_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Notification notification = new Notification(
                    rs.getInt("notification_id"),
                    rs.getInt("receiver_id"),
                    rs.getInt("wish_id"),
                    rs.getString("type"),
                    rs.getString("message"),
                    rs.getString("is_read")
                );
                notifications.add(notification);
            }
            System.out.println("Found " + notifications.size() + " notifications for user: " + currentUser.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }
    
    public int getUnreadNotificationCount() {
        User currentUser = requireCurrentUser();
        String sql = "SELECT COUNT(*) as unread_count FROM notifications WHERE receiver_id = ? AND is_read = 'N'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("unread_count");
                System.out.println("Unread notifications: " + count);
                return count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public void markNotificationAsRead(int notificationId) {
        User currentUser = requireCurrentUser();
        String sql = "UPDATE notifications SET is_read = 'Y' WHERE notification_id = ? AND receiver_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificationId);
            pstmt.setInt(2, currentUser.getUserId());
            int rows = pstmt.executeUpdate();
            System.out.println("Marked notification " + notificationId + " as read, rows: " + rows);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void markAllNotificationsAsRead() {
        User currentUser = requireCurrentUser();
        String sql = "UPDATE notifications SET is_read = 'Y' WHERE receiver_id = ? AND is_read = 'N'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            int rows = pstmt.executeUpdate();
            System.out.println("Marked all notifications as read, rows: " + rows);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    private Wish createWishFromResultSet(ResultSet rs) throws SQLException {
        Wish wish = new Wish(
            rs.getInt("wish_id"),
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getString("image_path"),
            rs.getString("is_completed")
        );
        return wish;
    }
    
    public int getContributionCount(int wishId) {
        String sql = "SELECT COUNT(DISTINCT user_id) as count FROM users_contributions WHERE wish_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, wishId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public double getTotalRaised(int wishId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM users_contributions WHERE wish_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, wishId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    public boolean testConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            boolean isConnected = conn != null && !conn.isClosed();
            System.out.println("Database connection test: " + (isConnected ? "SUCCESS" : "FAILED"));
            return isConnected;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}

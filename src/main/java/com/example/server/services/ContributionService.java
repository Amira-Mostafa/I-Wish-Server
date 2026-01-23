package com.example.server.services;

import com.example.models.Contribution;
import java.sql.*;

public class ContributionService {
    
    public String makeContribution(int userId, int wishId, double amount, String message) {
        System.out.println("=== ContributionService.makeContribution ===");
        System.out.println("UserId: " + userId + ", WishId: " + wishId + ", Amount: " + amount);
        
        try {
            Contribution contribution = makeContributionDirect(userId, wishId, amount, message);
            if (contribution != null) {
                System.out.println("Contribution successful! ID: " + contribution.getContributionId());
                return "SUCCESS|" + contribution.getContributionId() + "|" + 
                       contribution.getUserId() + "|" + contribution.getWishId() + "|" + 
                       contribution.getAmount() + "|" + (contribution.getMessage() != null ? contribution.getMessage() : "");
            }
        } catch (SQLException e) {
            System.err.println("Database error in makeContribution: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.err.println("Contribution failed - returning error");
        return "ERROR|Failed to make contribution";
    }
    
    private Contribution makeContributionDirect(int userId, int wishId, double amount, String message) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Check wish validity
            String checkSql = "SELECT w.user_id as owner_id, w.is_completed, w.price, w.name " +
                            "FROM wishes w " +
                            "WHERE w.wish_id = ?";
            
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, wishId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                conn.rollback();
                System.err.println("Wish not found: " + wishId);
                return null;
            }
            
            int ownerId = rs.getInt("owner_id");
            String isCompleted = rs.getString("is_completed");
            double wishPrice = rs.getDouble("price");
            String wishName = rs.getString("name");
            
            // Validation checks
            if (userId == ownerId) {
                conn.rollback();
                System.err.println("User cannot contribute to own wish");
                return null;
            }
            
            if ("Y".equals(isCompleted)) {
                conn.rollback();
                System.err.println("Wish is already completed");
                return null;
            }
            
            if (amount <= 0) {
                conn.rollback();
                System.err.println("Amount must be > 0");
                return null;
            }
            
            // Check remaining amount needed
            String checkRemainingSql = "SELECT COALESCE(SUM(uc.amount), 0) as total_raised " +
                                     "FROM users_contributions uc " +
                                     "WHERE uc.wish_id = ?";
            PreparedStatement checkRemainingStmt = conn.prepareStatement(checkRemainingSql);
            checkRemainingStmt.setInt(1, wishId);
            ResultSet remainingRs = checkRemainingStmt.executeQuery();
            
            double totalRaised = 0;
            if (remainingRs.next()) {
                totalRaised = remainingRs.getDouble("total_raised");
            }
            
            double remaining = wishPrice - totalRaised;
            System.out.println("Total raised: $" + totalRaised + ", Remaining: $" + remaining);
            
            if (amount > remaining) {
                conn.rollback();
                System.err.println("ERROR: Contribution amount $" + amount + " exceeds remaining amount $" + remaining);
                return null;
            }
            
            // 2. Insert contribution ONLY - triggers will handle the rest
            String insertSql = "INSERT INTO users_contributions (contribution_id, user_id, wish_id, amount) " +
                             "VALUES (users_contributions_seq.NEXTVAL, ?, ?, ?)";
            
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, wishId);
            insertStmt.setDouble(3, amount);
            
            int rows = insertStmt.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                System.err.println("Failed to insert contribution");
                return null;
            }
            
            // 3. Get the contribution ID
            String getIdSql = "SELECT users_contributions_seq.CURRVAL FROM dual";
            PreparedStatement getIdStmt = conn.prepareStatement(getIdSql);
            ResultSet idRs = getIdStmt.executeQuery();
            
            int contributionId = -1;
            if (idRs.next()) {
                contributionId = idRs.getInt(1);
            } else {
                conn.rollback();
                System.err.println("Could not get contribution ID");
                return null;
            }
            
            // 4. CREATE IMMEDIATE NOTIFICATION (separate from trigger)
            // This is for the "someone contributed" notification, not the "wish completed" notification
            String notifSql = "INSERT INTO notifications (notification_id, receiver_id, wish_id, type, message) " +
                            "VALUES (notifications_seq.NEXTVAL, ?, ?, 'WISH_BOUGHT', ?)";
            
            PreparedStatement notifStmt = conn.prepareStatement(notifSql);
            notifStmt.setInt(1, ownerId);
            notifStmt.setInt(2, wishId);
            
            // Get contributor name
            String contributorName = getUsername(conn, userId);
            String notifMessage = contributorName + " contributed $" + 
                                String.format("%.2f", amount) + 
                                " to your wish: " + wishName;
            
            notifStmt.setString(3, notifMessage);
            notifStmt.executeUpdate();
            
            conn.commit();
            System.out.println("✓ Contribution committed successfully!");
            
            // Message field removed from schema - pass empty string
            return new Contribution(contributionId, userId, wishId, amount, "");
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
                System.err.println("Transaction rolled back due to: " + e.getMessage());
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    private String getUsername(Connection conn, int userId) throws SQLException {
        String sql = "SELECT name FROM users WHERE user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getString("name") : "Someone";
    }
}
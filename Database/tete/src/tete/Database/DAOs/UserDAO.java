package tete.Database.DAOs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tete.Database.POJOs.User;

public class UserDAO {

    private Connection con;

    public UserDAO(Connection con) {
        this.con = con;
    }

    // Create a new user
    public void addUser(User user) throws SQLException {

        int userId;

        String seqSql = "SELECT users_seq.NEXTVAL AS id FROM dual";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(seqSql);

        if (rs.next()) {
            userId = rs.getInt("id");
        } else {
            throw new SQLException("Failed to generate user ID");
        }

        rs.close();
        stmt.close();

        user.setUserId(userId);

        String insertSql
                = "INSERT INTO users (user_id, name, email, password_hash, image_path) "
                + "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(insertSql);
        ps.setInt(1, user.getUserId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPasswordHash());
        ps.setString(5, user.getImagePath());

        ps.executeUpdate();
        ps.close();

    }

    // Read user by ID
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("image_path")
            );
        }
        rs.close();
        ps.close();
        return user;
    }

    // Read all users
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM users";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("image_path")
            ));
        }
        rs.close();
        stmt.close();
        return users;
    }

    // Update user
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name=?, email=?, password_hash=?, image_path=? WHERE user_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPasswordHash());
        ps.setString(4, user.getImagePath());
        ps.setInt(5, user.getUserId());
        ps.executeUpdate();
        ps.close();
    }

    // Delete user
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
        ps.close();
    }
}

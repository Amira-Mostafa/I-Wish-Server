package tete.Database.DAOs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tete.Database.POJOs.Wish;

public class WishDAO {

    private Connection con;

    public WishDAO(Connection con) {
        this.con = con;
    }

    // Add new wish
    public int addWish(Wish wish) throws SQLException {

        int wishId;

        String seqSql = "SELECT wishes_seq.NEXTVAL AS id FROM dual";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(seqSql);

        if (rs.next()) {
            wishId = rs.getInt("id");
        } else {
            throw new SQLException("Failed to generate wish ID");
        }

        rs.close();
        stmt.close();

        wish.setWishId(wishId);

        String insertSql
                = "INSERT INTO wishes (wish_id, user_id, name, description, price, image_path, is_completed) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(insertSql);
        ps.setInt(1, wishId);
        ps.setInt(2, wish.getUserId());
        ps.setString(3, wish.getName());
        ps.setString(4, wish.getDescription());
        ps.setDouble(5, wish.getPrice());
        ps.setString(6, wish.getImagePath());
        ps.setString(7, String.valueOf(wish.getIsCompleted()));

        ps.executeUpdate();
        ps.close();

        return wishId;
    }

    // Get wish by ID
    public Wish getWishById(int wishId) throws SQLException {
        String sql = "SELECT * FROM wishes WHERE wish_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, wishId);
        ResultSet rs = ps.executeQuery();
        Wish wish = null;
        if (rs.next()) {
            wish = new Wish(
                    rs.getInt("wish_id"),
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("image_path"),
                    rs.getString("is_completed").charAt(0)
            );
        }
        rs.close();
        ps.close();
        return wish;
    }

    // Get all wishes by user
    public List<Wish> getWishesByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM wishes WHERE user_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        List<Wish> wishes = new ArrayList<>();
        while (rs.next()) {
            wishes.add(new Wish(
                    rs.getInt("wish_id"),
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("image_path"),
                    rs.getString("is_completed").charAt(0)
            ));
        }
        rs.close();
        ps.close();
        return wishes;
    }

    // Update wish
    public void updateWish(Wish wish) throws SQLException {
        String sql = "UPDATE wishes SET name=?, description=?, price=?, image_path=?, is_completed=? WHERE wish_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, wish.getName());
        ps.setString(2, wish.getDescription());
        ps.setDouble(3, wish.getPrice());
        ps.setString(4, wish.getImagePath());
        ps.setString(5, String.valueOf(wish.getIsCompleted()));
        ps.setInt(6, wish.getWishId());
        ps.executeUpdate();
        ps.close();
    }

    // Delete wish
    public void deleteWish(int wishId) throws SQLException {
        String sql = "DELETE FROM wishes WHERE wish_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, wishId);
        ps.executeUpdate();
        ps.close();
    }
}

package tete.Database.DAOs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tete.Database.POJOs.UserContribution;

public class UserContributionDAO {

    private Connection con;

    public UserContributionDAO(Connection con) {
        this.con = con;
    }

    // Add a contribution
    public int addContribution(UserContribution uc) throws SQLException {

        int contributionId;

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT users_contributions_seq.NEXTVAL AS id FROM dual"
        );

        if (rs.next()) {
            contributionId = rs.getInt("id");
        } else {
            throw new SQLException("Failed to generate contribution ID");
        }

        rs.close();
        stmt.close();

        uc.setContributionId(contributionId);

        String sql
                = "INSERT INTO users_contributions (contribution_id, user_id, wish_id, amount) "
                + "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, contributionId);
        ps.setInt(2, uc.getUserId());
        ps.setInt(3, uc.getWishId());
        ps.setDouble(4, uc.getAmount());
        ps.executeUpdate();
        ps.close();

        return contributionId;
    }

    // Get contributions for a wish
    public List<UserContribution> getContributionsByWishId(int wishId) throws SQLException {
        String sql = "SELECT * FROM users_contributions WHERE wish_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, wishId);
        ResultSet rs = ps.executeQuery();
        List<UserContribution> contributions = new ArrayList<>();
        while (rs.next()) {
            contributions.add(new UserContribution(
                    rs.getInt("contribution_id"),
                    rs.getInt("user_id"),
                    rs.getInt("wish_id"),
                    rs.getDouble("amount")
            ));
        }
        rs.close();
        ps.close();
        return contributions;
    }

    // Delete a contribution
    public void deleteContribution(int contributionId) throws SQLException {
        String sql = "DELETE FROM users_contributions WHERE contribution_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, contributionId);
        ps.executeUpdate();
        ps.close();
    }
}

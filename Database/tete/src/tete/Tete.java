
package tete;

import java.sql.*;
import java.util.List;
import tete.Database.DAOs.*;
import tete.Database.POJOs.*;

public class Tete {

    public static void main(String[] args) throws SQLException {
        
        try {

            Class.forName("oracle.jdbc.OracleDriver");
            Connection con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    "wish_app",
                    "123"
            );

            System.out.println("Connected to Oracle");

            UserDAO userDAO = new UserDAO(con);            
            UserFriendDAO friendDAO = new UserFriendDAO(con);
            WishDAO wishDAO = new WishDAO(con);
            UserContributionDAO contributionDAO = new UserContributionDAO(con);
            NotificationDAO notificationDAO = new NotificationDAO(con);

            //Register users
            User salma = new User(0, "Salma", "salma7@gmail.com", "hash123", null);      
            User alaa   = new User(1, "Alaa", "alaa7@gmail.com", "hash456", null);
            
            userDAO.addUser(salma);
            userDAO.addUser(alaa);

            System.out.println("Users registered");

            
            // Friend request 
            friendDAO.addFriendRequest(
                    new UserFriend(salma.getUserId(), alaa.getUserId(), "PENDING")
            );
            
            System.out.println("Friend request sent");
            
            

            // Bob accepts
            friendDAO.updateFriendStatus(
                    salma.getUserId(),
                    alaa.getUserId(),
                    "ACCEPTED"
            );
            System.out.println("Friend request accepted");

            
            // Salma creates a wish
            Wish wish = new Wish(
                    0,
                    salma.getUserId(),
                    "Cookie Cake",
                    "One gaint chocolate cookie cake",
                    3000.00,
                    null,
                    'N'
            );
            wishDAO.addWish(wish);

            System.out.println("Wish created");
            
            
            // Fetch wish
            Wish salmaWish = wishDAO.getWishesByUserId(salma.getUserId()).get(0);

            // Alaa contributes
            UserContribution contribution = new UserContribution(
                    0,
                    alaa.getUserId(),
                    salmaWish.getWishId(),
                    3000.00
            );
            contributionDAO.addContribution(contribution);

            System.out.println("Contribution added");
            
            // Notifications (triggered ny dbms)
            System.out.println("\nSalma Notifications:");
            List<Notification> salmaNotes =
                    notificationDAO.getNotificationsByUserId(salma.getUserId());
            for (Notification n : salmaNotes) {
                System.out.println("- " + n.getMessage());
            }

            System.out.println("Alaa Notifications:");
            List<Notification> alaaNotes =
                    notificationDAO.getNotificationsByUserId(alaa.getUserId());
            for (Notification n : alaaNotes) {
                System.out.println("- " + n.getMessage());
            }
            
            con.close();
            System.out.println("Connection closed");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

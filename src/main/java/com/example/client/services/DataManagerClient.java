package com.example.client.services;

import java.util.ArrayList;
import java.util.List;

import com.example.client.network.ClientSocket;
import com.example.models.Contribution;
import com.example.models.Notification;
import com.example.models.User;
import com.example.models.Wish;

public class DataManagerClient {

    /* ================= AUTH ================= */

    public static boolean login(String email, String password) {
        String response = ClientSocket.send("LOGIN|" + email + "|" + password);

        if (response != null && response.startsWith("SUCCESS")) {
            // SUCCESS|userId|name|email|imagePath
            String[] p = response.split("\\|");
            String imagePath = p.length > 4 ? p[4] : "";
            User user = new User(
                    Integer.parseInt(p[1]),
                    p[2],
                    p[3],
                    imagePath
            );
            UserSession.setUser(user);
            return true;
        }
        return false;
    }

    public static boolean register(String name, String email, String password, String imagePath) {
        // If no image selected, use empty string
        String image = (imagePath != null && !imagePath.isEmpty()) ? imagePath : "";
        String response = ClientSocket.send(
                "REGISTER|" + name + "|" + email + "|" + password + "|" + image);
    
        System.out.println("Register response from server: " + response);
    
        if (response != null && response.startsWith("SUCCESS")) {
            String[] p = response.split("\\|");
            // SUCCESS|userId|name|email|imagePath
            String userImagePath = p.length > 4 ? p[4] : "";
            User user = new User(Integer.parseInt(p[1]), p[2], p[3], userImagePath);
            UserSession.setUser(user);
            return true;
        }
        return false;
    }
    

    /* ================= WISH ================= */

    public static boolean addWish(String title, String description,
                                  double price, String image) {

        String response = ClientSocket.send(
                "ADD_WISH|" + UserSession.getUserId() + "|" +
                title + "|" + description + "|" + price + "|" + image);

        return response.startsWith("SUCCESS");
    }

    public static List<Wish> getCurrentUserWishes() {
        String response = ClientSocket.send(
                "GET_MY_WISHES|" + UserSession.getUserId());

        List<Wish> wishes = new ArrayList<>();
        if (response != null && response.startsWith("SUCCESS")) {
            String data = response.substring(8); // Remove "SUCCESS|"
            if (!data.isEmpty()) {
                String[] wishStrings = data.split(";");
                for (String wishStr : wishStrings) {
                    String[] parts = wishStr.split(",");
                    if (parts.length >= 8) {
                        Wish wish = new Wish(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            parts[2],
                            parts[3],
                            Double.parseDouble(parts[4]),
                            parts[6],
                            parts[7]
                        );
                        wish.setRaisedAmount(Double.parseDouble(parts[5]));
                        wishes.add(wish);
                    }
                }
            }
        }
        return wishes;
    }

    /* ================= FRIEND ================= */

    public static List<User> getFriends() {
        String response = ClientSocket.send(
                "GET_FRIENDS|" + UserSession.getUserId());

        List<User> friends = new ArrayList<>();
        if (response != null && response.startsWith("SUCCESS")) {
            String data = response.substring(8); // Remove "SUCCESS|"
            if (!data.isEmpty()) {
                String[] userStrings = data.split(";");
                for (String userStr : userStrings) {
                    if (userStr == null || userStr.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = userStr.split(",", -1);
                    // Need at least 3 parts: user_id, name, email (image_path is optional)
                    if (parts.length >= 3) {
                        friends.add(new User(
                            Integer.parseInt(parts[0]),
                            parts[1],
                            parts[2],
                            parts.length > 3 ? parts[3] : ""
                        ));
                    }
                }
            }
        }
        return friends;
    }
    
    public static List<User> getAllUsers() {
        int userId = UserSession.getUserId();
        System.out.println("Getting all users for user: " + userId);
        
        String response = ClientSocket.send("GET_ALL_USERS|" + userId);
        return parseUserList(response);
    }
    
    public static List<User> searchUsers(String query) {
        int userId = UserSession.getUserId();
        String searchQuery = (query != null) ? query.trim() : "";
        System.out.println("Searching for: '" + searchQuery + "' by user: " + userId);
        
        String response = ClientSocket.send("SEARCH_USERS|" + userId + "|" + searchQuery);
        return parseUserList(response);
    }
    
    private static List<User> parseUserList(String response) {
        List<User> users = new ArrayList<>();
        
        if (response != null && response.startsWith("SUCCESS")) {
            String data = response.length() > 8 ? response.substring(8) : "";
            
            if (!data.isEmpty()) {
                String[] userStrings = data.split(";");
                
                for (String userStr : userStrings) {
                    if (userStr == null || userStr.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = userStr.split(",", -1);
                    
                    if (parts.length >= 3) {
                        try {
                            int parsedUserId = Integer.parseInt(parts[0]);
                            String name = parts[1];
                            String email = parts[2];
                            String imagePath = parts.length > 3 ? parts[3] : "";
                            
                            users.add(new User(parsedUserId, name, email, imagePath));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing user ID: " + parts[0]);
                        }
                    }
                }
            }
        } else {
            System.err.println("Failed to get users: " + response);
        }
        
        return users;
    }
    
    public static boolean sendFriendRequest(int receiverId) {
        int userId = UserSession.getUserId();
        System.out.println("Sending friend request from user " + userId + " to user " + receiverId);
        
        String response = ClientSocket.send(
                "SEND_FRIEND_REQUEST|" + userId + "|" + receiverId);
        
        System.out.println("Send friend request response: " + response);
        
        if (response == null) {
            System.err.println("No response from server");
            return false;
        }
        
        if (response.startsWith("SUCCESS")) {
            System.out.println("Friend request sent successfully");
            return true;
        } else {
            System.err.println("Friend request failed: " + response);
            return false;
        }
    }
    
    public static boolean acceptFriendRequest(int requesterId) {
        String response = ClientSocket.send(
                "ACCEPT_FRIEND_REQUEST|" + UserSession.getUserId() + "|" + requesterId);
        return response != null && response.startsWith("SUCCESS");
    }
    
    public static boolean declineFriendRequest(int requesterId) {
        String response = ClientSocket.send(
                "DECLINE_FRIEND_REQUEST|" + UserSession.getUserId() + "|" + requesterId);
        return response != null && response.startsWith("SUCCESS");
    }
    
    public static boolean removeFriend(int friendId) {
        String response = ClientSocket.send(
                "REMOVE_FRIEND|" + UserSession.getUserId() + "|" + friendId);
        return response != null && response.startsWith("SUCCESS");
    }
    
    public static List<com.example.models.FriendRequest> getPendingFriendRequests() {
        String response = ClientSocket.send(
                "GET_PENDING_FRIEND_REQUESTS|" + UserSession.getUserId());
        
        List<com.example.models.FriendRequest> requests = new ArrayList<>();
        if (response != null && response.startsWith("SUCCESS")) {
            String data = response.substring(8);
            if (!data.isEmpty()) {
                String[] requestStrings = data.split(";");
                for (String reqStr : requestStrings) {
                    String[] parts = reqStr.split(",");
                    if (parts.length >= 4) {
                        requests.add(new com.example.models.FriendRequest(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            parts[2],
                            parts[3]
                        ));
                    }
                }
            }
        }
        return requests;
    }
    
    public static List<Wish> getFriendWishes(int friendId) {
        String response = ClientSocket.send(
                "GET_FRIEND_WISHES|" + UserSession.getUserId() + "|" + friendId);
        
        List<Wish> wishes = new ArrayList<>();
        if (response != null && response.startsWith("SUCCESS")) {
            String data = response.substring(8);
            if (!data.isEmpty()) {
                String[] wishStrings = data.split(";");
                for (String wishStr : wishStrings) {
                    String[] parts = wishStr.split(",");
                    if (parts.length >= 8) {
                        Wish wish = new Wish(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            parts[2],
                            parts[3],
                            Double.parseDouble(parts[4]),
                            parts[6],
                            parts[7]
                        );
                        wish.setRaisedAmount(Double.parseDouble(parts[5]));
                        wishes.add(wish);
                    }
                }
            }
        }
        return wishes;
    }
    
    public static boolean updateWish(int wishId, String title, String description, double price, String image) {
        String response = ClientSocket.send(
                "UPDATE_WISH|" + UserSession.getUserId() + "|" + wishId + "|" +
                title + "|" + description + "|" + price + "|" + image);
        return response != null && response.startsWith("SUCCESS");
    }
    
    public static boolean deleteWish(int wishId) {
        String response = ClientSocket.send(
                "DELETE_WISH|" + UserSession.getUserId() + "|" + wishId);
        return response != null && response.startsWith("SUCCESS");
    }

    /* ================= CONTRIBUTION ================= */

    public static Contribution makeContribution(
        int wishId, double amount, String message) {

        int userId = UserSession.getUserId();
        System.out.println("=== DataManagerClient.makeContribution ===");
        System.out.println("UserId: " + userId + ", WishId: " + wishId + ", Amount: " + amount);
        
        String response = ClientSocket.send(
                "CONTRIBUTE|" + userId + "|" +
                wishId + "|" + amount + "|" + (message != null ? message : ""));

        System.out.println("Contribution response: " + response);

        if (response != null && response.startsWith("SUCCESS")) {
            String[] parts = response.split("\\|");
            
            if (parts.length < 5) {
                System.err.println("Invalid response format: " + response);
                return null;
            }

            try {
                int contributionId = Integer.parseInt(parts[1]);
                int returnedUserId = Integer.parseInt(parts[2]);
                int returnedWishId = Integer.parseInt(parts[3]);
                double returnedAmount = Double.parseDouble(parts[4]);
                String returnedMessage = parts.length > 5 ? parts[5] : "";

                System.out.println("Contribution successful! ID: " + contributionId);
                return new Contribution(
                        contributionId,
                        returnedUserId,
                        returnedWishId,
                        returnedAmount,
                        returnedMessage
                );
            } catch (NumberFormatException e) {
                System.err.println("Error parsing contribution response: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            String errorMsg = response != null && response.startsWith("ERROR|") 
                ? response.substring(6) 
                : "Unknown error";
            System.err.println("Contribution failed: " + errorMsg);
            return null;
        }
    }


    /* ================= NOTIFICATION ================= */

    public static List<Notification> getUserNotifications() {
        String response = ClientSocket.send(
                "GET_NOTIFICATIONS|" + UserSession.getUserId());

        List<Notification> notifications = new ArrayList<>();
        if (response != null && response.startsWith("SUCCESS")) {
            String data = response.substring(8);
            if (!data.isEmpty()) {
                String[] notifStrings = data.split(";");
                for (String notifStr : notifStrings) {
                    String[] parts = notifStr.split(",");
                    if (parts.length >= 6) {
                        notifications.add(new Notification(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            parts[3],
                            parts[4],
                            parts[5]
                        ));
                    }
                }
            }
        }
        return notifications;
    }

    public static int getUnreadNotificationCount() {
        String response = ClientSocket.send(
                "GET_UNREAD_NOTIFICATION_COUNT|" + UserSession.getUserId());
        
        if (response != null && response.startsWith("SUCCESS")) {
            String[] parts = response.split("\\|");
            if (parts.length >= 2) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
    
    public static boolean markNotificationAsRead(int notificationId) {
        String response = ClientSocket.send(
                "MARK_NOTIFICATION_READ|" + UserSession.getUserId() + "|" + notificationId);
        return response != null && response.startsWith("SUCCESS");
    }
    
    public static boolean markAllNotificationsAsRead() {
        String response = ClientSocket.send(
                "MARK_ALL_NOTIFICATIONS_READ|" + UserSession.getUserId());
        return response != null && response.startsWith("SUCCESS");
    }
}

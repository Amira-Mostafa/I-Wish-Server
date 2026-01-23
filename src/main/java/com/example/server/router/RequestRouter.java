package com.example.server.router;

import com.example.server.services.AuthService;
import com.example.server.services.ContributionService;
import com.example.server.services.DataManager;
import com.example.server.services.FriendService;
import com.example.server.services.NotificationService;
import com.example.server.services.WishService;
import com.example.server.socket.ClientSession;

public class RequestRouter {

    private AuthService authService = new AuthService();
    private FriendService friendService = new FriendService();
    private WishService wishService = new WishService();
    private ContributionService contributionService = new ContributionService();
    private NotificationService notificationService = new NotificationService();
    private final DataManager dataManager = DataManager.getInstance();

    public String route(String request, ClientSession session)  {
        try {
            String[] parts = request.split("\\|");
            String command = parts[0];

            switch (command) {
                // Authentication
                case "REGISTER":
                    if (parts.length < 4) {
                        return "ERROR|Missing registration parameters";
                    }
                    String imagePath = parts.length > 4 ? parts[4] : "";
                    return authService.register(parts[1], parts[2], parts[3], imagePath);

                case "LOGIN":
                    return authService.login(parts[1], parts[2]);
                
                // Friends
                case "GET_FRIENDS":
                    return friendService.getFriends(Integer.parseInt(parts[1]));

                case "GET_ALL_USERS":
                    return friendService.getAllUsers(Integer.parseInt(parts[1]));

                case "SEARCH_USERS":
                    String query = parts.length > 2 ? parts[2] : "";
                    System.out.println("Router: SEARCH_USERS - userId: " + parts[1] + ", query: '" + query + "'");
                    return friendService.searchUsers(Integer.parseInt(parts[1]), query);

                case "GET_PENDING_FRIEND_REQUESTS":
                    return friendService.getPendingFriendRequests(Integer.parseInt(parts[1]));

                case "SEND_FRIEND_REQUEST":
                    return friendService.sendFriendRequest(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

                case "ACCEPT_FRIEND_REQUEST":
                    return friendService.respondToFriendRequest(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), true);

                case "DECLINE_FRIEND_REQUEST":
                    return friendService.respondToFriendRequest(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), false);

                case "REMOVE_FRIEND":
                    return friendService.removeFriend(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                
                // Wishes
                case "GET_MY_WISHES":
                    return wishService.getMyWishes(Integer.parseInt(parts[1]));

                case "GET_FRIEND_WISHES":
                    return wishService.getFriendWishes(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

                case "ADD_WISH":
                    return wishService.createWish(Integer.parseInt(parts[1]), parts[2], parts[3], 
                                                  Double.parseDouble(parts[4]), parts.length > 5 ? parts[5] : "");

                case "UPDATE_WISH":
                    return wishService.updateWish(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), 
                                                   parts[3], parts[4], Double.parseDouble(parts[5]), 
                                                   parts.length > 6 ? parts[6] : "");

                case "DELETE_WISH":
                    return wishService.deleteWish(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                
                // Contributions - Using the new ContributionService
                case "CONTRIBUTE":
                    if (parts.length < 4) {
                        return "ERROR|Missing parameters for contribution";
                    }
                    System.out.println("Router: CONTRIBUTE - userId: " + parts[1] + ", wishId: " + parts[2] + ", amount: " + parts[3]);
                    // Message field removed - not in database schema
                    return contributionService.makeContribution(
                        Integer.parseInt(parts[1]), 
                        Integer.parseInt(parts[2]), 
                        Double.parseDouble(parts[3]), 
                        ""  // No message field in schema
                    );
                
                // Notifications
                case "GET_NOTIFICATIONS":
                    return notificationService.getUserNotifications(Integer.parseInt(parts[1]));

                case "GET_UNREAD_NOTIFICATION_COUNT":
                    return notificationService.getUnreadNotificationCount(Integer.parseInt(parts[1]));

                case "MARK_NOTIFICATION_READ":
                    return notificationService.markNotificationAsRead(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

                case "MARK_ALL_NOTIFICATIONS_READ":
                    return notificationService.markAllNotificationsAsRead(Integer.parseInt(parts[1]));

                default:
                    return "ERROR|Unknown command: " + command;
            }
            
        } catch (ArrayIndexOutOfBoundsException e) {
            return "ERROR|Missing parameters";
        } catch (NumberFormatException e) {
            return "ERROR|Invalid number format";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|Invalid request format: " + e.getMessage();
        }
    }
}
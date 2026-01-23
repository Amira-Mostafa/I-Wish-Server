package com.example.client.controllers;

import java.io.IOException;
import java.util.List;

import com.example.client.services.DataManagerClient;
import com.example.client.services.UserSession;
import com.example.models.Notification;
import com.example.models.User;
import com.example.models.Wish;
import com.example.utils.UIUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class DashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private GridPane statsGrid;
    @FXML private VBox recentActivity;
    @FXML private VBox quickActions;
    @FXML private Button logoutBtn;
    @FXML private ImageView profileImageView;
    @FXML private ImageView dashboardProfileImageView;
    
    // Add navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button friendsBtn;
    @FXML private Button wishListBtn;
    @FXML private Button notificationsBtn;

    private User currentUser;
    
    

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("✓ DashboardController.setCurrentUser() called");
        System.out.println("  User ID: " + user.getUserId());
        System.out.println("  User Name: " + user.getName());
    }
    
    @FXML
    public void initialize() {
        System.out.println("=== DashboardController.initialize() ===");
        

        // Try to get user from DataManagerClient if not passed
        if (currentUser == null) {
            System.out.println("WARNING: User not passed via setCurrentUser()");
            currentUser = UserSession.getUser();
            System.out.println("Got from UserSession: " +
                (currentUser != null ? "ID=" + currentUser.getUserId() : "NULL"));
        }
        
        
        if (currentUser == null) {
            System.out.println("ERROR: Still no user! Using dummy for testing...");
            // Use dummy user for testing
            currentUser = new User(0, "Test User", "test@test.com", "default.jpg");

        }
        
        System.out.println("Loading dashboard for user: " + currentUser.getName());
        loadDashboardData();
        updateNavigationBadges();
    }
    
    private void updateNavigationBadges() {
        // Update notification count
        int unreadCount = DataManagerClient.getUnreadNotificationCount();
        if (notificationsBtn != null) {
            UIUtils.updateButtonWithCount(notificationsBtn, "🔔 Notifications", unreadCount);
        }
        
        // Update friend request count
        int pendingRequests = DataManagerClient.getPendingFriendRequests().size();
        if (friendsBtn != null) {
            UIUtils.updateButtonWithCount(friendsBtn, "👥 Friends", pendingRequests);
        }
    }
    
    private void loadDashboardData() {
    User currentUser = UserSession.getUser();
    if (currentUser == null) {
        System.err.println("ERROR: currentUser is null!");
        return;
    }

    welcomeLabel.setText("Welcome back, " + currentUser.getName() + "!");
    
    // Load and display profile image
    loadProfileImage(currentUser);
    
    loadStats();
    loadRecentActivity();
    loadQuickActions();
}

private void loadProfileImage(User user) {
    try {
        String imagePath = user.getImagePath();
        Image image;
        
        if (imagePath != null && !imagePath.isEmpty()) {
            image = new Image(imagePath, true);
        } else {
            image = new Image(UIUtils.DEFAULT_USER_AVATAR, true);
        }
        
        // Set image for header profile
        if (profileImageView != null) {
            profileImageView.setImage(image);
            Circle clip = new Circle(20, 20, 20);
            profileImageView.setClip(clip);
        }
        
        // Set image for dashboard profile
        if (dashboardProfileImageView != null) {
            dashboardProfileImageView.setImage(image);
            Circle dashboardClip = new Circle(40, 40, 40);
            dashboardProfileImageView.setClip(dashboardClip);
        }
    } catch (Exception e) {
        System.err.println("Error loading profile image: " + e.getMessage());
        try {
            Image defaultImage = new Image(UIUtils.DEFAULT_USER_AVATAR, true);
            if (profileImageView != null) profileImageView.setImage(defaultImage);
            if (dashboardProfileImageView != null) dashboardProfileImageView.setImage(defaultImage);
        } catch (Exception ex) {
            // Ignore
        }
    }
}

    
private void loadStats() {
    if (statsGrid == null) return;

    statsGrid.getChildren().clear();

    List<Wish> userWishes = DataManagerClient.getCurrentUserWishes();
    List<User> friends = DataManagerClient.getFriends();
    int unreadCount = DataManagerClient.getUnreadNotificationCount();

    long giftsGiven = DataManagerClient.getUserNotifications().stream()
            .filter(n -> n.getType().contains("WISH_BOUGHT"))
            .count();

    VBox friendsCard = UIUtils.createStatCard(
            "👥 Friends",
            friends.size() + " connections",
            "#4CAF50",
            "View Friends"
    );
    friendsCard.setOnMouseClicked(e -> handleFriends());

    VBox wishesCard = UIUtils.createStatCard(
            "🎯 My Wishes",
            userWishes.size() + " wishes",
            "#2196F3",
            "View Wishes"
    );
    wishesCard.setOnMouseClicked(e -> handleWishList());

    VBox giftsCard = UIUtils.createStatCard(
            "🎁 Gifts Given",
            giftsGiven + " gifts",
            "#9C27B0",
            "View Impact"
    );
    giftsCard.setOnMouseClicked(e -> showImpact());

    VBox notificationsCard = UIUtils.createStatCard(
            "🔔 Notifications",
            unreadCount + " unread",
            "#FF9800",
            "View All"
    );
    notificationsCard.setOnMouseClicked(e -> handleNotifications());

    statsGrid.add(friendsCard, 0, 0);
    statsGrid.add(wishesCard, 1, 0);
    statsGrid.add(giftsCard, 0, 1);
    statsGrid.add(notificationsCard, 1, 1);
}

    
private void loadRecentActivity() {
    if (recentActivity == null) return;

    recentActivity.getChildren().clear();

    List<Notification> notifications = DataManagerClient.getUserNotifications();
    int count = Math.min(notifications.size(), 4);

    if (count == 0) {
        Label noActivity = new Label("No recent activity");
        noActivity.setStyle("-fx-font-size: 14; -fx-text-fill: #999; -fx-padding: 20;");
        noActivity.setAlignment(javafx.geometry.Pos.CENTER);
        recentActivity.getChildren().add(noActivity);
    } else {
        for (int i = 0; i < count; i++) {
            Notification n = notifications.get(i);
            recentActivity.getChildren().add(
                    createActivityItem(n.getMessage(), n.getTimeAgo())
            );
        }
    }
}

    
    private HBox createActivityItem(String description, String time) {
        HBox item = new HBox(15);
        item.setPadding(new javafx.geometry.Insets(15));
        item.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1;");
        
        VBox text = new VBox(5);
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 14));
        descLabel.setTextFill(Color.GRAY);
        descLabel.setWrapText(true);
        
        text.getChildren().add(descLabel);
        HBox.setHgrow(text, Priority.ALWAYS);
        
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("Arial", 11));
        timeLabel.setTextFill(Color.LIGHTGRAY);
        
        item.getChildren().addAll(text, timeLabel);
        return item;
    }
    
    private void loadQuickActions() {
        if (quickActions == null) return;
        
        quickActions.getChildren().clear();
        
        GridPane actionsGrid = new GridPane();
        actionsGrid.setHgap(15);
        actionsGrid.setVgap(15);
        
        Button addWishBtn = UIUtils.createActionButton("➕ Add Wish", "Add a new wish to your list");
        addWishBtn.setOnAction(e -> showAddWishDialog());
        
        Button addFriendBtn = UIUtils.createActionButton("👥 Add Friend", "Connect with new friends");
        addFriendBtn.setOnAction(e -> showAddFriendDialog());
        
        Button contributeBtn = UIUtils.createActionButton("💝 Contribute", "Help friends with their wishes");
        contributeBtn.setOnAction(e -> handleFriends());
        
        Button viewNotificationsBtn = UIUtils.createActionButton("🔔 Notifications", "View all notifications");
        viewNotificationsBtn.setOnAction(e -> handleNotifications());
        
        actionsGrid.add(addWishBtn, 0, 0);
        actionsGrid.add(addFriendBtn, 1, 0);
        actionsGrid.add(contributeBtn, 0, 1);
        actionsGrid.add(viewNotificationsBtn, 1, 1);
        
        Label title = new Label("Quick Actions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        quickActions.getChildren().addAll(title, new Separator(), actionsGrid);
    }
    
    private void showImpact() {
        List<User> friends = DataManagerClient.getFriends();
        List<Wish> userWishes = DataManagerClient.getCurrentUserWishes();
    
        long giftsGiven = DataManagerClient.getUserNotifications().stream()
                .filter(n -> n.getType().contains("WISH_BOUGHT"))
                .count();
    
        double totalRaised = userWishes.stream()
                .mapToDouble(Wish::getRaisedAmount)
                .sum();
    
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("My Impact");
        alert.setHeaderText("Your Contribution Impact");
        alert.setContentText(
                "Friends: " + friends.size() + "\n" +
                "Wishes created: " + userWishes.size() + "\n" +
                "Gifts contributed: " + giftsGiven + "\n" +
                "Total raised: $" + String.format("%.2f", totalRaised)
        );
        alert.showAndWait();
    }
    
    
    private void showAddWishDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/add-wish.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Wish");
            stage.setScene(new Scene(root, 500, 600));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error", "Cannot open Add Wish dialog: " + ex.getMessage());
        }
    }
    
    private void showAddFriendDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/add-friend.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add Friend");
            stage.setScene(new Scene(root, 500, 600));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error", "Cannot open Add Friend dialog: " + ex.getMessage());
        }
    }
    
    private void showErrorAndRedirect(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        
        // Redirect to login
        handleLogout();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleDashboard() {
        // Already on dashboard
    }
    
    @FXML
    private void handleFriends() {
        loadScene("/com/example/views/friends.fxml");
    }
    
    @FXML
    private void handleWishList() {
        loadScene("/com/example/views/wishlist.fxml");
    }
    
    @FXML
    private void handleNotifications() {
        loadScene("/com/example/views/notifications.fxml");
    }
    
    @FXML
    private void handleLogout() {
        UserSession.logout();
        loadScene("/com/example/views/login.fxml");
    }
    
    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1550, 800));
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error", "Cannot load scene: " + ex.getMessage());
        }
    }
}
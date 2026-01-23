package com.example.client.controllers;

import java.io.IOException;
import java.util.List;

import com.example.models.Notification;
import com.example.client.services.DataManagerClient;
import com.example.utils.DialogUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.example.utils.UIUtils;   

public class NotificationsController {
    
    @FXML private Button dashboardBtn;
    @FXML private Button friendsBtn;
    @FXML private Button wishListBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox contentArea;
    
    @FXML
    public void initialize() {
        loadNotificationsContent();
        updateNavigationBadges();
    }
    
    private void updateNavigationBadges() {
        int unreadCount = DataManagerClient.getUnreadNotificationCount();
        if (notificationsBtn != null) {
            UIUtils.updateButtonWithCount(notificationsBtn, "🔔 Notifications", unreadCount);
        }
        
        int pendingRequests = DataManagerClient.getPendingFriendRequests().size();
        if (friendsBtn != null) {
            UIUtils.updateButtonWithCount(friendsBtn, "👥 Friends", pendingRequests);
        }
    }
    
    private void loadNotificationsContent() {
        contentArea.getChildren().clear();

        int unreadCount = DataManagerClient.getUnreadNotificationCount();
        
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🔔 Notifications");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        if (unreadCount > 0) {
            Label count = new Label(String.valueOf(unreadCount));
            count.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-weight: bold;");
            titleRow.getChildren().addAll(title, count);
        } else {
            titleRow.getChildren().add(title);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        if (unreadCount > 0) {
            Button markAllReadBtn = new Button("Mark All as Read");
            markAllReadBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-background-radius: 8;");
            markAllReadBtn.setOnAction(e -> {
                DataManagerClient.markAllNotificationsAsRead();
                loadNotificationsContent();
            });
            titleRow.getChildren().add(markAllReadBtn);
        }
        
        titleRow.getChildren().add(spacer);

        // Notification items
        VBox notificationsList = new VBox(15);
        
        List<Notification> notifications = DataManagerClient.getUserNotifications();
        
        if (notifications.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            
            Label noNotifications = new Label("No notifications yet");
            noNotifications.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            noNotifications.setTextFill(Color.GRAY);
            
            Label suggestion = new Label("When you get friend requests or wish updates, they'll appear here!");
            suggestion.setFont(Font.font("Arial", 14));
            suggestion.setTextFill(Color.LIGHTGRAY);
            suggestion.setAlignment(Pos.CENTER);
            
            emptyState.getChildren().addAll(noNotifications, suggestion);
            notificationsList.getChildren().add(emptyState);
        } else {
            for (Notification notification : notifications) {
                notificationsList.getChildren().add(createNotificationItem(notification));
            }
        }

        contentArea.getChildren().addAll(titleRow, notificationsList);
    }
    
    private VBox createNotificationItem(Notification notification) {
        VBox notificationItem = new VBox(10);
        notificationItem.setPadding(new Insets(15));
        notificationItem.setStyle("-fx-background-color: white; " +
                                "-fx-background-radius: 10; " +
                                "-fx-border-color: " + (notification.isRead() ? "#e0e0e0" : "#764ba2") + "; " +
                                "-fx-border-radius: 10; " +
                                "-fx-border-width: " + (notification.isRead() ? "1" : "2") + ";");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        String typeIcon = notification.getType().contains("COMPLETED") ? "🎉" : 
                         notification.getType().contains("FRIEND") ? "👥" : "🎁";
        Label title = new Label(typeIcon + " " + notification.getType().replace("_", " "));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Label timeLabel = new Label(notification.getTimeAgo());
        timeLabel.setFont(Font.font("Arial", 11));
        timeLabel.setTextFill(Color.LIGHTGRAY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        if (!notification.isRead()) {
            Label unreadDot = new Label("●");
            unreadDot.setTextFill(Color.web("#764ba2"));
            unreadDot.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            header.getChildren().addAll(title, spacer, unreadDot, timeLabel);
        } else {
            header.getChildren().addAll(title, spacer, timeLabel);
        }

        Label message = new Label(notification.getMessage());
        message.setFont(Font.font("Arial", 14));
        message.setWrapText(true);
        message.setTextFill(Color.GRAY);

        Button actionButton = new Button("View Details");
        actionButton.setStyle("-fx-background-color: #764ba2; " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 8; " +
                             "-fx-cursor: hand; " +
                             "-fx-padding: 8 15;");
        actionButton.setOnAction(e -> {
            if (!notification.isRead()) {
                DataManagerClient.markNotificationAsRead(notification.getNotificationId());
                loadNotificationsContent();
            }
            DialogUtils.showSuccess("Notification Details", 
                notification.getType().replace("_", " ") + "\n\n" + notification.getMessage());
        });

        notificationItem.getChildren().addAll(header, message, actionButton);
        return notificationItem;
    }
    
    @FXML
    private void handleDashboard() {
        loadScene("/com/example/views/dashboard.fxml");
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
        // Already on notifications page
        loadNotificationsContent();
    }
    
    @FXML
    private void handleLogout() {
        loadScene("/com/example/views/login.fxml");
    }
    
    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1550, 800));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
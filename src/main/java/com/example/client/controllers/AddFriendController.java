package com.example.client.controllers;

import java.util.List;

import com.example.models.User;
import com.example.client.services.DataManagerClient;
import com.example.utils.UIUtils;
import com.example.utils.DialogUtils;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AddFriendController {
    
    @FXML private TextField searchField;
    @FXML private VBox resultsContainer;
    @FXML private Button searchButton;
    @FXML private Button cancelButton;
    
    // No need for instance variable - using static methods
    
    @FXML
    public void initialize() {
        searchButton.setOnAction(e -> handleSearch());
        cancelButton.setOnAction(e -> handleCancel());
        
        // Allow Enter key to trigger search
        searchField.setOnAction(e -> handleSearch());
        
        // Load all users initially
        loadAllUsers();
    }
    
    private void loadAllUsers() {
        System.out.println("Loading all users...");
        List<User> allUsers = DataManagerClient.getAllUsers();
        System.out.println("Found " + allUsers.size() + " users");
        
        resultsContainer.getChildren().clear();
        
        if (allUsers.isEmpty()) {
            Label noUsers = new Label("No users found.\n\nThere are no other users in the system yet.");
            noUsers.setStyle("-fx-text-fill: #666; -fx-font-size: 14; -fx-padding: 20; -fx-alignment: center;");
            noUsers.setWrapText(true);
            resultsContainer.getChildren().add(noUsers);
        } else {
            for (User user : allUsers) {
                resultsContainer.getChildren().add(createUserItem(user));
            }
        }
    }
    
    private void handleSearch() {
        String query = searchField.getText().trim();
        
        System.out.println("Searching for: " + query);
        
        // If query is empty, show all users
        List<User> searchResults;
        if (query.isEmpty()) {
            searchResults = DataManagerClient.getAllUsers();
        } else {
            searchResults = DataManagerClient.searchUsers(query);
        }
        
        System.out.println("Search returned " + searchResults.size() + " results");
        
        resultsContainer.getChildren().clear();
        
        if (searchResults.isEmpty()) {
            Label noResults = new Label("No users found matching: \"" + query + "\"\n\nTry searching by name or email.");
            noResults.setStyle("-fx-text-fill: #666; -fx-font-size: 14; -fx-padding: 20; -fx-alignment: center;");
            noResults.setWrapText(true);
            resultsContainer.getChildren().add(noResults);
        } else {
            for (User user : searchResults) {
                resultsContainer.getChildren().add(createUserItem(user));
            }
        }
    }
    
    private HBox createUserItem(User user) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-border-width: 1;");
        
        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(50);
        avatar.setFitHeight(50);
        avatar.setPreserveRatio(true);
        try {
            if (user.getImagePath() != null && !user.getImagePath().isEmpty()) {
                avatar.setImage(new Image(user.getImagePath()));
            } else {
                avatar.setImage(new Image(UIUtils.DEFAULT_USER_AVATAR));
            }
        } catch (Exception e) {
            avatar.setImage(new Image(UIUtils.DEFAULT_USER_AVATAR));
        }
        
        // Make avatar circular
        avatar.setClip(new Circle(25, 25, 25));
        
        // User info
        VBox info = new VBox(5);
        Label nameLabel = new Label(user.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        info.getChildren().addAll(nameLabel, emailLabel);
        
        // Add Friend button
        Button addFriendBtn = UIUtils.createGradientButton("Add Friend", 120, 40);
        addFriendBtn.setOnAction(e -> {
            System.out.println("Add Friend button clicked for user: " + user.getName() + " (ID: " + user.getUserId() + ")");
            boolean success = DataManagerClient.sendFriendRequest(user.getUserId());
            if (success) {
                DialogUtils.showSuccess("Friend Request Sent", 
                    "Your friend request has been sent to " + user.getName() + "!\n\n" +
                    "They will be notified and can accept your request.");
                // Remove this user from the results
                resultsContainer.getChildren().remove(item);
            } else {
                DialogUtils.showError("Request Failed", 
                    "Unable to send friend request.\n\n" +
                    "You may have already sent a request to this user, or they may have already sent one to you.\n\n" +
                    "Please check your pending friend requests.");
            }
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        item.getChildren().addAll(avatar, info, spacer, addFriendBtn);
        return item;
    }
    
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
}
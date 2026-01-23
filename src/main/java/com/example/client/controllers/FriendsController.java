package com.example.client.controllers;

import java.io.IOException;
import java.util.List;

import com.example.models.User;
import com.example.models.Wish;
import com.example.client.services.DataManagerClient;
import com.example.utils.UIUtils;
import com.example.utils.DialogUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class FriendsController {
    
    @FXML private Button dashboardBtn;
    @FXML private Button friendsBtn;
    @FXML private Button wishListBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox contentArea;
    
    // Using static methods from DataManagerClient
    
    @FXML
    public void initialize() {
        loadFriendsContent();
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
    
    private void loadFriendsContent() {
        contentArea.getChildren().clear();
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        
        Label title = new Label("👥 Friends");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        Button addFriendButton = UIUtils.createGradientButton("+ Add Friend", 150, 40);
        addFriendButton.setOnAction(e -> showAddFriendDialog());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(title, spacer, addFriendButton);
        
        // Pending Requests
        List<com.example.models.FriendRequest> pendingRequests = DataManagerClient.getPendingFriendRequests();
        if (!pendingRequests.isEmpty()) {
            VBox pendingSection = new VBox(10);
            pendingSection.setPadding(new Insets(20));
            pendingSection.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            
            Label pendingTitle = new Label("Pending Requests (" + pendingRequests.size() + ")");
            pendingTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            
            VBox requests = new VBox(10);
            for (com.example.models.FriendRequest request : pendingRequests) {
                requests.getChildren().add(createFriendRequestItem(request));
            }
            
            pendingSection.getChildren().addAll(pendingTitle, requests);
            contentArea.getChildren().add(pendingSection);
        }

        // Search Box
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(20));
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search friends by name or email...");
        searchField.setPrefHeight(45);
        searchField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-padding: 10;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button searchButton = UIUtils.createGradientButton("Search", 100, 45);
        searchButton.setOnAction(e -> {
            String query = searchField.getText();
            if (!query.isEmpty()) {
                List<User> results = DataManagerClient.searchUsers(query);
                showSearchResults(results, query);
            }
        });
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // Friends List
        VBox friendsListSection = new VBox(10);
        friendsListSection.setPadding(new Insets(20));
        friendsListSection.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        List<User> friends = DataManagerClient.getFriends();
        Label friendsTitle = new Label("My Friends (" + friends.size() + ")");
        friendsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        VBox friendsList = new VBox(10);
        if (friends.isEmpty()) {
            Label noFriends = new Label("You don't have any friends yet. Add some friends to see their wishes!");
            noFriends.setStyle("-fx-font-size: 16; -fx-text-fill: #666; -fx-padding: 40; -fx-alignment: center;");
            friendsList.getChildren().add(noFriends);
        } else {
            for (User friend : friends) {
                List<Wish> friendWishes = DataManagerClient.getFriendWishes(friend.getUserId());
                String wishCount = friendWishes.size() + " wish" + (friendWishes.size() != 1 ? "es" : "");
                friendsList.getChildren().add(createFriendItem(friend, wishCount));
            }
        }
        
        friendsListSection.getChildren().addAll(friendsTitle, friendsList);
        contentArea.getChildren().addAll(header, searchBox, friendsListSection);
    }
    
    private HBox createFriendRequestItem(com.example.models.FriendRequest request) {
        HBox requestItem = new HBox(20);
        requestItem.setPadding(new Insets(15));
        requestItem.setStyle("-fx-background-color: white; " +
                           "-fx-background-radius: 10; " +
                           "-fx-border-color: #e0e0e0; " +
                           "-fx-border-radius: 10; " +
                           "-fx-border-width: 1;");
        
        // User avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");
        try {
            avatar.setImage(new Image(UIUtils.DEFAULT_USER_AVATAR, 40, 40, true, true));
        } catch (Exception e) {
            // Ignore
        }
        
        VBox info = new VBox(5);
        Label nameLabel = new Label(request.getRequesterName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Label requestLabel = new Label("Sent you a friend request");
        requestLabel.setFont(Font.font("Arial", 12));
        requestLabel.setTextFill(Color.GRAY);
        
        info.getChildren().addAll(nameLabel, requestLabel);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Button acceptBtn = UIUtils.createGradientButton("Accept", 100, 40);
        acceptBtn.setStyle(acceptBtn.getStyle().replace("#764ba2", "#4CAF50").replace("#667eea", "#2E7D32"));
        acceptBtn.setOnAction(e -> {
            boolean success = DataManagerClient.acceptFriendRequest(request.getRequesterId());
            if (success) {
                DialogUtils.showSuccess("Friend Request Accepted", 
                    "You are now friends with " + request.getRequesterName() + "!\n\n" +
                    "You can now see each other's wishes and contribute to them.");
            } else {
                DialogUtils.showError("Error", "Failed to accept friend request. Please try again.");
            }
            loadFriendsContent();
        });
        
        Button declineBtn = new Button("Decline");
        declineBtn.setStyle("-fx-background-color: #f8f9fa; " +
                          "-fx-text-fill: #f44336; " +
                          "-fx-background-radius: 8; " +
                          "-fx-border-color: #f44336; " +
                          "-fx-border-radius: 8; " +
                          "-fx-border-width: 1; " +
                          "-fx-cursor: hand; " +
                          "-fx-padding: 8 15;");
        declineBtn.setOnAction(e -> {
            boolean success = DataManagerClient.declineFriendRequest(request.getRequesterId());
            if (success) {
                DialogUtils.showSuccess("Request Declined", 
                    "You have declined the friend request from " + request.getRequesterName() + ".");
            }
            loadFriendsContent();
        });
        
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(acceptBtn, declineBtn);
        
        requestItem.getChildren().addAll(avatar, info, buttons);
        return requestItem;
    }
    
    private HBox createFriendItem(User friend, String wishCount) {
        HBox friendItem = new HBox(15);
        friendItem.setPadding(new Insets(15));
        friendItem.setStyle("-fx-background-color: white; " +
                          "-fx-background-radius: 10; " +
                          "-fx-border-color: #e0e0e0; " +
                          "-fx-border-radius: 10; " +
                          "-fx-border-width: 1;");
        
        // Friend avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");
        try {
            String imagePath = friend.getImagePath();
            if (imagePath == null || imagePath.isEmpty()) {
                avatar.setImage(new Image(UIUtils.DEFAULT_USER_AVATAR, 40, 40, true, true));
            } else {
                avatar.setImage(new Image(friend.getImagePath(), 40, 40, true, true));
            }
        } catch (Exception e) {
            // Use default if fails
            try {
                avatar.setImage(new Image(UIUtils.DEFAULT_USER_AVATAR, 40, 40, true, true));
            } catch (Exception ex) {
                // Ignore
            }
        }
        
        VBox info = new VBox(5);
        Label nameLabel = new Label(friend.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Label wishesLabel = new Label(wishCount);
        wishesLabel.setFont(Font.font("Arial", 12));
        wishesLabel.setTextFill(Color.GRAY);
        
        info.getChildren().addAll(nameLabel, wishesLabel);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Button viewWishes = new Button("View Wishes");
        viewWishes.setStyle("-fx-background-color: #764ba2; " +
                           "-fx-text-fill: white; " +
                           "-fx-background-radius: 8; " +
                           "-fx-cursor: hand; " +
                           "-fx-padding: 8 15;");
        viewWishes.setOnAction(e -> showFriendsWishes(friend));
        
        Button removeFriend = new Button("Remove");
        removeFriend.setStyle("-fx-background-color: #f8f9fa; " +
                             "-fx-text-fill: #f44336; " +
                             "-fx-background-radius: 8; " +
                             "-fx-border-color: #f44336; " +
                             "-fx-border-radius: 8; " +
                             "-fx-border-width: 1; " +
                             "-fx-cursor: hand; " +
                             "-fx-padding: 8 15;");
        removeFriend.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Friend");
            alert.setHeaderText("Remove " + friend.getName() + "?");
            alert.setContentText("Are you sure you want to remove this friend?");
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                DataManagerClient.removeFriend(friend.getUserId());
                loadFriendsContent();
            }
        });
        
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(viewWishes, removeFriend);
        
        friendItem.getChildren().addAll(avatar, info, buttons);
        return friendItem;
    }
    
    private void showFriendsWishes(User friend) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        VBox wishesContent = new VBox(20);
        wishesContent.setPadding(new Insets(30));
        wishesContent.setStyle("-fx-background-color: #f8f9fa;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button backButton = new Button("← Back to Friends");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #764ba2;");
        backButton.setOnAction(e -> loadFriendsContent());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label title = new Label(friend.getName() + "'s Wishes");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        header.getChildren().addAll(backButton, spacer, title);
        
        List<Wish> friendWishes = DataManagerClient.getFriendWishes(friend.getUserId());
        Label subtitle = new Label(friendWishes.size() + " wishes");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.GRAY);

        VBox wishItemsContainer = new VBox(15);
        if (friendWishes.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            
            Label noWishes = new Label(friend.getName() + " hasn't created any wishes yet.");
            noWishes.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            noWishes.setTextFill(Color.GRAY);
            
            emptyState.getChildren().add(noWishes);
            wishItemsContainer.getChildren().add(emptyState);
        } else {
            for (Wish item : friendWishes) {
                wishItemsContainer.getChildren().add(createFriendWishItem(item, friend));
            }
        }

        wishesContent.getChildren().addAll(header, subtitle, new Separator(), wishItemsContainer);
        scrollPane.setContent(wishesContent);
        
        // Create a new stage for viewing friend's wishes
        Stage stage = new Stage();
        stage.setTitle(friend.getName() + "'s Wishes");
        stage.setScene(new Scene(scrollPane, 800, 600));
        stage.show();
    }
    
    private HBox createFriendWishItem(Wish item, User friend) {
        HBox wishItem = new HBox(20);
        wishItem.setPadding(new Insets(20));
        wishItem.setStyle("-fx-background-color: white; " +
                         "-fx-background-radius: 15; " +
                         "-fx-border-color: #e0e0e0; " +
                         "-fx-border-radius: 15; " +
                         "-fx-border-width: 1; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Image on left
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setPrefWidth(120);
        
        ImageView wishImage = new ImageView();
        wishImage.setFitWidth(100);
        wishImage.setFitHeight(100);
        wishImage.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        
        try {
            String imageUrl = item.getImagePath() != null && !item.getImagePath().isEmpty() 
                ? item.getImagePath() 
                : UIUtils.DEFAULT_WISH_IMAGE;
            wishImage.setImage(new Image(imageUrl, 100, 100, true, true));
        } catch (Exception e) {
            // Use default image if loading fails
        }
        
        imageBox.getChildren().add(wishImage);

        // Main content in middle
        VBox itemInfo = new VBox(10);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        
        Label title = new Label(item.getName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        if (item.isCompleted()) {
            Label completedLabel = new Label("✓ COMPLETED");
            completedLabel.setStyle("-fx-background-color: #4CAF50; " +
                                  "-fx-text-fill: white; " +
                                  "-fx-padding: 5 10; " +
                                  "-fx-background-radius: 5; " +
                                  "-fx-font-size: 11;");
            header.getChildren().add(completedLabel);
        }
        
        header.getChildren().add(title);
        
        Label description = new Label(item.getDescription());
        description.setFont(Font.font("Arial", 14));
        description.setTextFill(Color.GRAY);
        description.setWrapText(true);
        
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        
        Label price = new Label(String.format("$%.2f", item.getPrice()));
        price.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        price.setTextFill(Color.web("#764ba2"));
        
        Label raised = new Label(String.format("($%.0f raised)", item.getRaisedAmount()));
        raised.setFont(Font.font("Arial", 14));
        raised.setTextFill(Color.GRAY);
        
        priceRow.getChildren().addAll(price, raised);
        
        ProgressBar progressBar = new ProgressBar(item.getProgress());
        progressBar.setPrefHeight(8);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: #764ba2; -fx-background-color: #e0e0e0;");
        
        HBox progressInfo = new HBox(10);
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label progressText = new Label(String.format("$%.0f raised", item.getRaisedAmount()));
        progressText.setFont(Font.font("Arial", 12));
        progressText.setTextFill(Color.GRAY);
        
        if (item.getRemainingAmount() > 0) {
            Label remainingText = new Label(String.format("• $%.0f remaining", item.getRemainingAmount()));
            remainingText.setFont(Font.font("Arial", 12));
            remainingText.setTextFill(Color.GRAY);
            progressInfo.getChildren().addAll(progressText, remainingText);
        } else {
            progressInfo.getChildren().add(progressText);
        }
        
        if (item.getContributorCount() > 0) {
            Label contributorText = new Label(String.format("• %d contributor%s", 
                item.getContributorCount(), item.getContributorCount() == 1 ? "" : "s"));
            contributorText.setFont(Font.font("Arial", 12));
            contributorText.setTextFill(Color.GRAY);
            progressInfo.getChildren().add(contributorText);
        }
        
        itemInfo.getChildren().addAll(header, description, priceRow, progressBar, progressInfo);

        // Buttons on right
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPrefWidth(150);
        
        if (!item.isCompleted()) {
            Button contributeButton = UIUtils.createGradientButton("Contribute", 120, 40);
            contributeButton.setStyle(contributeButton.getStyle().replace("#764ba2", "#4CAF50").replace("#667eea", "#2E7D32"));
            // Check if there are any contributions (raised amount > 0 means at least one contribution exists)
            boolean hasContributions = item.getRaisedAmount() > 0;
            contributeButton.setText(hasContributions ? "Contribute" : "Be the First!");
            contributeButton.setOnAction(e -> showContributionDialog(item, friend));
            buttonBox.getChildren().add(contributeButton);
        } else {
            Label completedLabel = new Label("Already Funded!");
            completedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-size: 14;");
            buttonBox.getChildren().add(completedLabel);
        }

        wishItem.getChildren().addAll(imageBox, itemInfo, buttonBox);
        return wishItem;
    }
    
    private void showContributionDialog(Wish item, User friend) {
        try {
            System.out.println("Loading contribution dialog...");
            java.net.URL url = getClass().getResource("/com/example/views/contribution.fxml");
            System.out.println("First attempt URL: " + url);
            
            if (url == null) {
                url = getClass().getClassLoader().getResource("com/example/views/contribution.fxml");
                System.out.println("Second attempt URL: " + url);
            }
            
            if (url == null) {
                throw new IOException("Resource not found: contribution.fxml");
            }
            
            System.out.println("Loading FXML from: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            ContributionController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Controller not found for contribution.fxml. Check fx:controller attribute.");
            }
            System.out.println("Controller found, setting wish ID: " + item.getWishId());
            controller.setWishId(item.getWishId());
            
            Stage stage = new Stage();
            stage.setTitle("Contribute to " + friend.getName() + "'s Wish");
            stage.setScene(new Scene(root, 500, 400));
            stage.showAndWait();
            
            // Refresh after contribution
            loadFriendsContent();
        } catch (javafx.fxml.LoadException ex) {
            ex.printStackTrace();
            String errorMsg = "FXML Load Error\n\n";
            errorMsg += "Error: " + ex.getMessage() + "\n\n";
            if (ex.getCause() != null) {
                errorMsg += "Cause: " + ex.getCause().getMessage() + "\n\n";
            }
            errorMsg += "Please check:\n";
            errorMsg += "1. The controller class exists: com.example.controllers.ContributionController\n";
            errorMsg += "2. The FXML file syntax is correct\n";
            errorMsg += "3. The project has been rebuilt";
            showError("Cannot open Contribution dialog", errorMsg);
        } catch (Exception ex) {
            ex.printStackTrace();
            String errorMsg = "Cannot open Contribution dialog\n\n";
            errorMsg += "Error: " + ex.getMessage() + "\n\n";
            if (ex.getCause() != null) {
                errorMsg += "Cause: " + ex.getCause().getMessage() + "\n\n";
            }
            errorMsg += "Please check:\n";
            errorMsg += "1. The file exists: src/main/resources/com/example/views/contribution.fxml\n";
            errorMsg += "2. The project has been rebuilt\n";
            errorMsg += "3. Resources are properly configured";
            showError("Cannot open Contribution dialog", errorMsg);
        }
    }
    
    private void showSearchResults(List<User> results, String query) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        VBox resultsContent = new VBox(20);
        resultsContent.setPadding(new Insets(30));
        resultsContent.setStyle("-fx-background-color: #f8f9fa;");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button backButton = new Button("← Back to Friends");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #764ba2;");
        backButton.setOnAction(e -> loadFriendsContent());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label title = new Label("Search Results for: \"" + query + "\" (" + results.size() + ")");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        header.getChildren().addAll(backButton, spacer, title);
        
        VBox resultsList = new VBox(10);
        if (results.isEmpty()) {
            Label noResults = new Label("No users found matching: \"" + query + "\"");
            noResults.setStyle("-fx-font-size: 16; -fx-text-fill: #666; -fx-padding: 40; -fx-alignment: center;");
            resultsList.getChildren().add(noResults);
        } else {
            for (User user : results) {
                resultsList.getChildren().add(createUserSearchItem(user));
            }
        }
        
        resultsContent.getChildren().addAll(header, resultsList);
        scrollPane.setContent(resultsContent);
        
        // Replace current content with search results
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
    
    private HBox createUserSearchItem(User user) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 10; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 10; " +
                     "-fx-border-width: 1;");
        
        // User avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");
        try {
            String imagePath = user.getImagePath();
            if (imagePath == null || imagePath.isEmpty()) {
                avatar.setImage(new Image(UIUtils.DEFAULT_USER_AVATAR, 40, 40, true, true));
            } else {
                avatar.setImage(new Image(user.getImagePath(), 40, 40, true, true));
            }
        } catch (Exception e) {
            // Use default if fails
        }
        
        VBox info = new VBox(5);
        Label nameLabel = new Label(user.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setFont(Font.font("Arial", 12));
        emailLabel.setTextFill(Color.GRAY);
        
        info.getChildren().addAll(nameLabel, emailLabel);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Button addFriendBtn = UIUtils.createGradientButton("Add Friend", 120, 40);
        addFriendBtn.setOnAction(e -> {
            boolean success = DataManagerClient.sendFriendRequest(user.getUserId());
            if (success) {
                showAlert("Success", "Friend request sent to " + user.getName() + "!");
                loadFriendsContent();
            } else {
                showError("Error", "Failed to send friend request. You may have already sent one.");
            }
        });
        
        item.getChildren().addAll(avatar, info, addFriendBtn);
        return item;
    }
    
    private void showAddFriendDialog() {
        try {
            System.out.println("Opening Add Friend dialog...");
            java.net.URL url = getClass().getResource("/com/example/views/add-friend.fxml");
            if (url == null) {
                url = getClass().getClassLoader().getResource("com/example/views/add-friend.fxml");
            }
            if (url == null) {
                System.err.println("FXML file not found: add-friend.fxml");
                showError("Error", "Cannot find add-friend.fxml file. Please check if the file exists in src/main/resources/com/example/views/");
                return;
            }
            
            System.out.println("Loading FXML from: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            Stage stage = new Stage();
            stage.setTitle("Add Friend");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();
            // Refresh after dialog closes
            loadFriendsContent();
        } catch (javafx.fxml.LoadException ex) {
            ex.printStackTrace();
            String errorMsg = "FXML Load Error\n\n";
            errorMsg += "Error: " + ex.getMessage() + "\n\n";
            if (ex.getCause() != null) {
                errorMsg += "Cause: " + ex.getCause().getMessage() + "\n\n";
            }
            errorMsg += "Please check:\n";
            errorMsg += "1. The file exists: src/main/resources/com/example/views/add-friend.fxml\n";
            errorMsg += "2. The controller class exists: com.example.client.controllers.AddFriendController\n";
            errorMsg += "3. The FXML file syntax is correct\n";
            errorMsg += "4. The project has been rebuilt";
            showError("Cannot open Add Friend dialog", errorMsg);
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Cannot open Add Friend dialog", "Error: " + ex.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleDashboard() {
        loadScene("/com/example/views/dashboard.fxml");
    }
    
    @FXML
    private void handleFriends() {
        // Already on friends page
        loadFriendsContent();
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
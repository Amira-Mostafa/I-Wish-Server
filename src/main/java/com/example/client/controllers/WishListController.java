package com.example.client.controllers;

import java.io.IOException;
import java.util.List;

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
import javafx.scene.control.Separator;
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

public class WishListController {
    
    @FXML private Button dashboardBtn;
    @FXML private Button friendsBtn;
    @FXML private Button wishListBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox contentArea;
    
    @FXML
    public void initialize() {
        loadWishListContent();
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
    
    private void loadWishListContent() {
        contentArea.getChildren().clear();
        
        // Header
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(20);
        
        Label title = new Label("🎯 My Wish List");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        Button addWishButton = UIUtils.createGradientButton("+ Add New Wish", 150, 40);
        addWishButton.setOnAction(e -> showAddWishDialog());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        titleRow.getChildren().addAll(title, spacer, addWishButton);
        
        Label subtitle = new Label("Create and manage your wishes");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.GRAY);

        // Wish items
        List<Wish> wishItems = DataManagerClient.getCurrentUserWishes();

        VBox wishItemsContainer = new VBox(15);
        if (wishItems.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            
            Label noWishes = new Label("You haven't created any wishes yet");
            noWishes.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            noWishes.setTextFill(Color.GRAY);
            
            Label suggestion = new Label("Click 'Add New Wish' to start adding items to your wish list!");
            suggestion.setFont(Font.font("Arial", 14));
            suggestion.setTextFill(Color.LIGHTGRAY);
            suggestion.setAlignment(Pos.CENTER);
            
            emptyState.getChildren().addAll(noWishes, suggestion);
            wishItemsContainer.getChildren().add(emptyState);
        } else {
            for (Wish item : wishItems) {
                wishItemsContainer.getChildren().add(createWishItem(item));
            }
        }

        contentArea.getChildren().addAll(titleRow, subtitle, new Separator(), wishItemsContainer);
    }
    
    private HBox createWishItem(Wish item) {
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
        
        // Only show edit and delete buttons for incomplete wishes
        if (!item.isCompleted()) {
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color: #764ba2; " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 8; " +
                               "-fx-cursor: hand; " +
                               "-fx-padding: 8 15; " +
                               "-fx-pref-width: 120;");
            editButton.setOnAction(e -> showEditWishDialog(item));
            
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #f8f9fa; " +
                                "-fx-text-fill: #f44336; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: #f44336; " +
                                "-fx-border-radius: 8; " +
                                "-fx-border-width: 1; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 8 15; " +
                                "-fx-pref-width: 120;");
            deleteButton.setOnAction(e -> {
                if (DialogUtils.showConfirmation("Delete Wish", 
                    "Are you sure you want to delete \"" + item.getName() + "\"?\n\n" +
                    "This action cannot be undone.")) {
                    DataManagerClient.deleteWish(item.getWishId());
                    loadWishListContent();
                }
            });
            
            buttonBox.getChildren().addAll(editButton, deleteButton);
        } else {
            // For completed wishes, show a "Completed" label instead
            Label completedLabel = new Label("✓ Completed");
            completedLabel.setStyle("-fx-text-fill: #4CAF50; " +
                                   "-fx-font-weight: bold; " +
                                   "-fx-font-size: 14px;");
            buttonBox.getChildren().add(completedLabel);
        }

        wishItem.getChildren().addAll(imageBox, itemInfo, buttonBox);
        return wishItem;
    }
    
    private void showAddWishDialog() {
        try {
            java.net.URL url = getClass().getResource("/com/example/views/add-wish.fxml");
            if (url == null) {
                url = getClass().getClassLoader().getResource("com/example/views/add-wish.fxml");
            }
            if (url == null) {
                throw new IOException("Resource not found: add-wish.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Wish");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();
            // Refresh after dialog closes
            loadWishListContent();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Cannot open Add Wish dialog", ex.getMessage());
        }
    }
    
    private void showEditWishDialog(Wish wish) {
        try {
            java.net.URL url = getClass().getResource("/com/example/views/edit-wish.fxml");
            if (url == null) {
                url = getClass().getClassLoader().getResource("com/example/views/edit-wish.fxml");
            }
            if (url == null) {
                throw new IOException("Resource not found: edit-wish.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            EditWishController controller = loader.getController();
            // Set data after load() - initialize() will have been called automatically
            controller.setWishData(
                wish.getWishId(),
                wish.getName() != null ? wish.getName() : "",
                wish.getDescription() != null ? wish.getDescription() : "",
                wish.getPrice(),
                wish.getImagePath() != null ? wish.getImagePath() : ""
            );
            
            Stage stage = new Stage();
            stage.setTitle("Edit Wish");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();
            // Refresh after dialog closes
            loadWishListContent();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Cannot open Edit Wish dialog", ex.getMessage());
        }
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
        loadScene("/com/example/views/friends.fxml");
    }
    
    @FXML
    private void handleWishList() {
        // Already on wish list page
        loadWishListContent();
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
package com.example.client.controllers;

import com.example.client.services.DataManagerClient;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditWishController {
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField imageField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    //private DataManagerClient dataManager = DataManagerClient.getInstance();
    //private DataManager dataManager = DataManager.getInstance();
    
    private int wishId; // Store the wish ID to edit
    
    // Method to set the wish data (call this after the dialog is shown, or in initialize)
    public void setWishData(int wishId, String name, String description, double price, String imageUrl) {
        this.wishId = wishId;
        
        // Ensure fields are initialized before setting values
        if (nameField != null) {
            nameField.setText(name != null ? name : "");
        }
        if (descriptionField != null) {
            descriptionField.setText(description != null ? description : "");
        }
        if (priceField != null) {
            priceField.setText(String.valueOf(price));
        }
        if (imageField != null) {
            imageField.setText(imageUrl != null ? imageUrl : "");
        }
    }
    
    @FXML
    public void initialize() {
        // Ensure fields are initialized
        if (nameField == null || descriptionField == null || priceField == null || imageField == null) {
            System.err.println("Warning: Some FXML fields are not initialized!");
            return;
        }
        
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }
    
    private void handleSave() {
        // Handle null values safely - check both field and getText() result
        if (nameField == null || descriptionField == null || priceField == null || imageField == null) {
            showError("Form fields are not initialized. Please close and try again.");
            return;
        }
        
        String name = (nameField.getText() != null) ? nameField.getText().trim() : "";
        String description = (descriptionField.getText() != null) ? descriptionField.getText().trim() : "";
        String priceText = (priceField.getText() != null) ? priceField.getText().trim() : "";
        String imageUrl = (imageField.getText() != null) ? imageField.getText().trim() : "";

        if (name.isEmpty() || priceText.isEmpty()) {
            showError("Please fill in all required fields (*)");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Price must be greater than 0");
                return;
            }

            boolean success = DataManagerClient.updateWish(wishId, name, description, price, imageUrl);
            if (success) {
                closeWindow();
            } else {
                showError("Failed to update wish. Please try again.");
            }
        } catch (NumberFormatException ex) {
            showError("Please enter a valid price");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("An error occurred: " + ex.getMessage());
        }
    }
    
    private void handleCancel() {
        closeWindow();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}

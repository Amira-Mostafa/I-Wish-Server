package com.example.client.controllers;

import com.example.client.services.DataManagerClient;
import com.example.models.Contribution;
import com.example.models.Wish;
import com.example.utils.DialogUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ContributionController {
    
    @FXML private TextField amountField;
    @FXML private Label remainingAmountLabel;
    @FXML private Label errorLabel;
    @FXML private Button contributeButton;
    @FXML private Button cancelButton;
    
    private int wishId; // Store the wish ID to contribute to
    private Wish wish; // Store wish details for validation
    
    // Method to set the wish ID (call this before showing the dialog)
    public void setWishId(int wishId) {
        this.wishId = wishId;
        // Load wish details to get remaining amount
        loadWishDetails();
    }
    
    private void loadWishDetails() {
        // Get wish details from friends' wishes or search
        // For now, we'll validate on the server side
        remainingAmountLabel.setText("Enter the amount you'd like to contribute");
    }
    
    @FXML
    public void initialize() {
        if (amountField == null || contributeButton == null || cancelButton == null) {
            System.err.println("Warning: Some FXML fields are not initialized!");
            return;
        }
        
        contributeButton.setOnAction(e -> handleContribute());
        cancelButton.setOnAction(e -> handleCancel());
        
        // Update remaining amount label as user types
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRemainingAmount();
        });
    }
    
    private void updateRemainingAmount() {
        // This will be updated when we have wish details
        // For now, just show placeholder
    }
    
    private void handleContribute() {
        if (amountField == null) {
            showError("Form fields are not initialized. Please close and try again.");
            return;
        }
        
        String amountText = (amountField.getText() != null) ? amountField.getText().trim() : "";
        
        if (amountText.isEmpty()) {
            showError("Please enter an amount");
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountText);
            
            if (amount <= 0) {
                showError("Amount must be greater than 0");
                return;
            }
            
            // Make the contribution (no message field - removed from schema)
            System.out.println("Attempting to contribute to wish ID: " + wishId);
            Contribution contribution = DataManagerClient.makeContribution(wishId, amount, "");
            
            if (contribution != null) {
                DialogUtils.showSuccess("Contribution Successful!", 
                    String.format("You've contributed $%.2f to this wish.\n\nThank you for your generosity!", amount));
                
                // Close the dialog
                Stage stage = (Stage) contributeButton.getScene().getWindow();
                stage.close();
            } else {
                System.err.println("Contribution returned null - check console for server-side errors");
                showError("Failed to make contribution.\n\n" +
                         "Possible reasons:\n" +
                         "• You cannot contribute to your own wish\n" +
                         "• The wish has already been completed\n" +
                         "• The contribution amount exceeds the remaining amount needed\n" +
                         "• The wish no longer exists\n\n" +
                         "Please check the console for detailed error messages.");
            }
            
        } catch (NumberFormatException e) {
            showError("Please enter a valid amount");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("An error occurred: " + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
    }
    
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
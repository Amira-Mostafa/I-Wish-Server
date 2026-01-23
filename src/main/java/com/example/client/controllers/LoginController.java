package com.example.client.controllers;

import java.io.IOException;

import com.example.client.services.DataManagerClient;
import com.example.client.services.UserSession;
import com.example.utils.UIUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button signInButton;
    @FXML private Hyperlink createAccountLink;
    
    @FXML
    public void initialize() {
        // Style the hyperlink
        UIUtils.styleHyperlink(createAccountLink);
        
        signInButton.setOnAction(e -> handleLogin());
        createAccountLink.setOnAction(e -> showRegisterScreen());
        
        // Set default test values (optional, for testing)
        // emailField.setText("test@example.com");
        // passwordField.setText("password123");
    }
    
    private void handleLogin() {
        System.out.println("=== LoginController.handleLogin() ===");
        
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        System.out.println("Email: " + email);
        System.out.println("Password length: " + password.length());
        
        // Clear previous errors
        errorLabel.setVisible(false);
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        System.out.println("Calling dataManager.login()...");
        if (DataManagerClient.login(email, password)) {
            System.out.println("Login successful! Loading dashboard...");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/dashboard.fxml"));
                Parent root = loader.load();

                DashboardController dashboardController = loader.getController();
                if (dashboardController != null) {
                    dashboardController.setCurrentUser(UserSession.getUser());
                }
                
                Stage stage = (Stage) signInButton.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 800));
            } catch (IOException ex) {
                ex.printStackTrace();
                showError("Error loading dashboard: " + ex.getMessage());
            }
        } else {
            System.out.println("Login failed in DataManager");
            showError("Invalid email or password");
        }
    }
    
    private void showRegisterScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) createAccountLink.getScene().getWindow();
            stage.setScene(new Scene(root, 1550, 800));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        System.err.println("Login error: " + message);
    }
}
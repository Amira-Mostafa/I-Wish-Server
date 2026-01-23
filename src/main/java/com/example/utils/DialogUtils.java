package com.example.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class DialogUtils {
    
    private static void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 15; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);"
        );
        
        // Style buttons
        dialogPane.lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: linear-gradient(to right, #764ba2, #667eea); " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-padding: 8 20;"
        );
        
        if (dialogPane.lookupButton(ButtonType.CANCEL) != null) {
            dialogPane.lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-text-fill: #333; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; " +
                "-fx-border-width: 1; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 8 20;"
            );
        }
    }
    
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }
    
    public static void showAlert(String title, String message) {
        showSuccess(title, message);
    }
    
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }
    
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
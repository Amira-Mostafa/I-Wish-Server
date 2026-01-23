package com.example.utils;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;

public class UIUtils {
    
    public static final String DEFAULT_WISH_IMAGE = "https://images.unsplash.com/photo-1579586337278-3f3d5b3b3b3b?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80";
    public static final String DEFAULT_USER_AVATAR = "https://images.unsplash.com/photo-1494790108755-2616b612b786?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80";
    
    public static Button createGradientButton(String text, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setStyle("-fx-background-color: linear-gradient(to right, #764ba2, #667eea); " +
                       "-fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-background-radius: 8; -fx-cursor: hand; " +
                       "-fx-effect: dropshadow(gaussian, rgba(118,75,162,0.3), 10, 0, 0, 3);");
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(118,75,162,0.4), 12, 0, 0, 4);"));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: linear-gradient(to right, #764ba2, #667eea); " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(118,75,162,0.3), 10, 0, 0, 3);"));
        
        return button;
    }
    
    public static VBox createStatCard(String title, String value, String color, String actionText) {
        VBox card = new VBox(10);
        card.setPadding(new javafx.geometry.Insets(20));
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 15; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 15; " +
                     "-fx-border-width: 1; " +
                     "-fx-cursor: hand;");
        
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: " + color + "; " +
            "-fx-border-radius: 15; " +
            "-fx-border-width: 2; " +
            "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 15; " +
            "-fx-border-width: 1; " +
            "-fx-cursor: hand;"));
        
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#333"));
        
        titleBox.getChildren().add(titleLabel);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setTextFill(javafx.scene.paint.Color.web(color));
        
        Label actionLabel = new Label(actionText);
        actionLabel.setFont(Font.font("Arial", 12));
        actionLabel.setTextFill(javafx.scene.paint.Color.web(color));
        actionLabel.setStyle("-fx-font-weight: bold;");
        
        card.getChildren().addAll(titleBox, valueLabel, actionLabel);
        return card;
    }
    
    public static void styleHyperlink(Hyperlink link) {
        link.setStyle("-fx-text-fill: #764ba2; -fx-underline: true; -fx-cursor: hand;");
    }
    
    public static void addBadgeToButton(Button button, int count) {
        if (count > 0) {
            // Create a StackPane to overlay the badge on the button
            javafx.scene.layout.StackPane stackPane = new javafx.scene.layout.StackPane();
            stackPane.getChildren().add(button);
            
            Label badge = new Label(String.valueOf(count));
            badge.setStyle("-fx-background-color: #f44336; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-size: 10; " +
                          "-fx-font-weight: bold; " +
                          "-fx-background-radius: 10; " +
                          "-fx-padding: 2 6; " +
                          "-fx-min-width: 18; " +
                          "-fx-alignment: center;");
            
            // Position badge at top-right
            javafx.scene.layout.StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
            stackPane.getChildren().add(badge);
            
            // Note: This requires the button to be in a container that can be replaced
            // For now, we'll update button text to include count
            String originalText = button.getText();
            if (!originalText.contains("(")) {
                button.setText(originalText + " (" + count + ")");
            }
        }
    }
    
    public static void updateButtonWithCount(Button button, String baseText, int count) {
        if (count > 0) {
            button.setText(baseText + " (" + count + ")");
        } else {
            button.setText(baseText);
        }
    }
    
    public static Button createActionButton(String text, String tooltipText) {
        Button button = new Button(text);
        button.setPrefSize(200, 80);
        button.setStyle("-fx-background-color: white; " +
                       "-fx-border-color: #764ba2; " +
                       "-fx-border-width: 2; " +
                       "-fx-background-radius: 12; " +
                       "-fx-border-radius: 12; " +
                       "-fx-font-size: 14; " +
                       "-fx-font-weight: bold; " +
                       "-fx-text-fill: #764ba2; " +
                       "-fx-cursor: hand; " +
                       "-fx-alignment: center-left; " +
                       "-fx-padding: 15;");
        
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #764ba2; " +
            "-fx-border-color: #764ba2; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-font-size: 14; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white; " +
            "-fx-cursor: hand; " +
            "-fx-alignment: center-left; " +
            "-fx-padding: 15;"));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #764ba2; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-font-size: 14; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #764ba2; " +
            "-fx-cursor: hand; " +
            "-fx-alignment: center-left; " +
            "-fx-padding: 15;"));
        
        return button;
    }
}
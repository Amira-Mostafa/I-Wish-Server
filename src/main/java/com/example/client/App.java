package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.server.services.WishServer;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("=== Testing Database Connection ===");
    
        try {
            // Test connection directly
            com.example.server.services.DatabaseConnection.getConnection();
            System.out.println("✓ Database connection successful!");
        } catch (Exception e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Start the server in a separate thread
        System.out.println("=== Starting Server ===");
        Thread serverThread = new Thread(() -> {
            try {
                WishServer server = new WishServer();
                if (server != null) {
                    server.start();
                }
            } catch (Exception e) {
                // If server is already running, that's okay - just continue
                if (e.getMessage() != null && e.getMessage().contains("Address already in use")) {
                    System.out.println("Server already running on port 8888, continuing...");
                } else {
                    System.err.println("Failed to start server: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        serverThread.setDaemon(true); // Allow JVM to exit even if server thread is running
        serverThread.start();
        
        // Give the server a moment to start
        Thread.sleep(1000);

        Parent root = FXMLLoader.load(getClass().getResource("/com/example/views/login.fxml"));
        
        Scene scene = new Scene(root, 1550, 800);
        
        stage.setTitle("🎁 I-yWish - Make wishes come true together");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
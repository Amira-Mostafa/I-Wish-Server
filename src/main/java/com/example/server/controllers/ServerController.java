package com.example.server.controllers;

import com.example.server.services.WishServer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ServerController {

    @FXML
    private Label statusLabel;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    private WishServer server;
    private Thread serverThread;

    @FXML
    private void startServer() {
        server = new WishServer();

        serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        statusLabel.setText("Server is running");
        statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");

        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    @FXML
    private void stopServer() {
        if (server != null) {
            server.stop();
        }

        statusLabel.setText("Server is stopped");
        statusLabel.setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) startButton.getScene().getWindow();
            if (stage != null) {
                stage.setOnCloseRequest(event -> {
                    if (server != null) {
                        server.stop();
                    }
                });
            }
        });
    }   
}

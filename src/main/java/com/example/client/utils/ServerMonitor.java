package com.example.client.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent; // <-- THIS ONE is required!
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerMonitor {

    private static ServerMonitor instance;
    private ScheduledExecutorService scheduler;
    private boolean lastStateUp = true;

    private ServerMonitor() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public static ServerMonitor getInstance() {
        if (instance == null) {
            instance = new ServerMonitor();
        }
        return instance;
    }

    public void startMonitoring(Stage stage, String loginFXML, String notAvailableFXML) {
        scheduler.scheduleAtFixedRate(() -> {
            boolean isUp = checkServer();
            if (isUp != lastStateUp) {
                lastStateUp = isUp;
                Platform.runLater(() -> {
                    try {
                        String fxmlToLoad = isUp ? loginFXML : notAvailableFXML;
                        Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(fxmlToLoad));
                        stage.setScene(new javafx.scene.Scene(root, 1550, 800));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private boolean checkServer() {
        try (Socket socket = new Socket("localhost", 8888)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

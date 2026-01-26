package com.example.client;

import com.example.client.utils.ServerMonitor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Load initial scene based on server state
        boolean serverUp = checkServerOnce();
        String initialFXML = serverUp ?
                "/com/example/views/login.fxml" :
                "/com/example/views/notAvailableServer.fxml";

        Parent root = FXMLLoader.load(getClass().getResource(initialFXML));
        stage.setScene(new Scene(root, 1550, 800));
        stage.setTitle("🎁 i-Wish");
        stage.show();

        // Start background monitoring for live switching
        ServerMonitor.getInstance().startMonitoring(
                stage,
                "/com/example/views/login.fxml",
                "/com/example/views/notAvailableServer.fxml"
        );
    }

    private boolean checkServerOnce() {
        try (java.net.Socket socket = new java.net.Socket("localhost", 8888)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        ServerMonitor.getInstance().stopMonitoring();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

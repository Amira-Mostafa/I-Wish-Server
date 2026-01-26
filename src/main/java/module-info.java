module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;
    requires java.sql.rowset;

    // JavaFX Application entry point
    exports com.example.server;

    // FXML controllers (reflection)
    opens com.example.server.controllers to javafx.fxml;

    // Existing client-side stuff
    opens com.example.client to javafx.fxml;
    opens com.example.client.controllers to javafx.fxml;
    opens com.example.models to javafx.fxml;
    opens com.example.views to javafx.fxml;

    exports com.example.client;
    exports com.example.server.socket;
    exports com.example.models;
    exports com.example.utils;
    exports com.example.client.utils;

}

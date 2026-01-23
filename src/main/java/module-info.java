module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;
    requires java.sql.rowset; 
    

    opens com.example.client to javafx.fxml;
    opens com.example.client.controllers to javafx.fxml;
    opens com.example.models to javafx.fxml;
    opens com.example.views to javafx.fxml;
    
    exports com.example.client;
    exports com.example.server.socket;
    exports com.example.models;
    exports com.example.utils;
}
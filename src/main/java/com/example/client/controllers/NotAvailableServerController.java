package com.example.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the "Server Not Available" screen.
 * Works together with ServerMonitor for automatic live switching.
 */
public class NotAvailableServerController {

    @FXML
    private Label errorLabel; // optional, for showing messages


    @FXML
    public void initialize() {
        // Live monitoring is handled by ServerMonitor singleton.
        // Nothing needs to be done here for auto-switching.
    }
}

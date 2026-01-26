package com.example.client.utils;

import java.net.Socket;

public class ServerConnectionChecker {

    private static final String HOST = "localhost";
    private static final int PORT = 8888;
    private static final int TIMEOUT_MS = 1500;

    public static boolean isServerAvailable() {
        try (Socket socket = new Socket(HOST, PORT)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

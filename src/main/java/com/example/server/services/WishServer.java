package com.example.server.services;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.server.handlers.ClientHandler;

public class WishServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    
    public WishServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            threadPool = Executors.newFixedThreadPool(10); // Handle up to 10 clients
            System.out.println("🎁 i-Wish Server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
    
     public void start(){
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
                
            } catch (IOException e) {
                System.err.println("Error accepting client: " + e.getMessage());
            }
        }
    }
    
    
    public static void main(String[] args) throws SQLException {
        WishServer server = new WishServer();
        server.start();
    }
}
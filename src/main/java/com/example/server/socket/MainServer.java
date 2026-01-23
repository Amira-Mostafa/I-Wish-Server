package com.example.server.socket;

import java.net.ServerSocket;
import java.net.Socket;

import com.example.server.handlers.ClientHandler;

public class MainServer {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server started on port 8888");

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

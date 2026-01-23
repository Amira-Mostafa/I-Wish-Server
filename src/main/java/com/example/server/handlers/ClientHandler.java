package com.example.server.handlers;

import java.io.*;
import java.net.Socket;
import com.example.server.router.RequestRouter;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private RequestRouter router = new RequestRouter();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String request;
            while ((request = in.readLine()) != null) {
                String response = router.route(request);
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");
        }
    }
}

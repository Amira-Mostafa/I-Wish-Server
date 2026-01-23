package com.example.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {

    public static String send(String request) {
        try {
            Socket socket = new Socket("localhost", 8888);

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out.println(request);
            String response = in.readLine();

            socket.close();
            return response;

        } catch (IOException e) {
            return "ERROR|Server unavailable";
        }
    }
}

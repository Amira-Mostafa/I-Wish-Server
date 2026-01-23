package com.example.server.socket;

import java.net.Socket;

import com.example.models.User;

public class ClientSession {

    private Socket socket;
    private User user;
    private boolean authenticated = false;

    public ClientSession(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public User getUser() {
        return user;
    }

    public void authenticate(User user) {
        this.user = user;
        this.authenticated = true;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}

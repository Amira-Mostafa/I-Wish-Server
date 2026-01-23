package com.example.server.services;

import com.example.models.User;

public class AuthService {

    private DataManager dataManager = DataManager.getInstance();

    public String register(String name, String email, String password, String imagePath) {
        boolean success = dataManager.register(name, email, password, imagePath);
        if (success) {
            User user = dataManager.getCurrentUser();
            return "SUCCESS|" + user.getUserId() + "|" + user.getName() + "|" + user.getEmail() + "|" + 
                   (user.getImagePath() != null ? user.getImagePath() : "");
        }
         return "ERROR|Registration failed";
    }

    public String login(String email, String password) {
        boolean success = dataManager.login(email, password);
        if (success) {
            User user = dataManager.getCurrentUser();
            return "SUCCESS|" + user.getUserId() + "|" + user.getName() + "|" + user.getEmail() + "|" + 
                   (user.getImagePath() != null ? user.getImagePath() : "");
        }
        return "ERROR|Invalid credentials";
    }
}

package com.example.client.services;

import com.example.models.User;

public class UserSession {

    private static User currentUser;

    private UserSession() {}

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static int getUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public static String getUserName() {
        return currentUser != null ? currentUser.getName() : "";
    }

    public static void logout() {
        currentUser = null;
    }
}

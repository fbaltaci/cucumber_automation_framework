package com.example.bookstore.util;

import java.util.UUID;

public class UserUtil {
    public static String generateUniqueUsername() {
        return "testuser_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateDefaultPassword() {
        return "SecurePass_123!";
    }
}

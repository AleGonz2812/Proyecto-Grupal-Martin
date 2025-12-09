package com.eventos.utils;

import org.mindrot.jbcrypt.BCrypt;

public class GeneratePassword {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("\nSQL UPDATE:");
        System.out.println("UPDATE usuarios SET password = '" + hash + "' WHERE email = 'admin@eventos.com';");
    }
}

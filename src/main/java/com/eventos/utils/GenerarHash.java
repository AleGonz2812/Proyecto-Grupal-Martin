package com.eventos.utils;

/**
 * Clase temporal para generar hash de contraseña
 */
public class GenerarHash {
    public static void main(String[] args) {
        // Generar hash para la contraseña "admin123"
        String password = "admin123";
        String hash = PasswordUtil.hashPassword(password);
        
        System.out.println("===========================================");
        System.out.println("Contraseña: " + password);
        System.out.println("Hash BCrypt:");
        System.out.println(hash);
        System.out.println("===========================================");
        System.out.println("\nEjecuta esto en MySQL Workbench:");
        System.out.println("USE eventos_db;");
        System.out.println("UPDATE usuarios SET password = '" + hash + "' WHERE email = 'admin@eventos.com';");
    }
}

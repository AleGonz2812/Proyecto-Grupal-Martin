package com.eventos.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para hash y verificación de contraseñas usando BCrypt
 */
public class PasswordUtil {
    
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Genera un hash BCrypt de una contraseña en texto plano
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Verifica si una contraseña en texto plano coincide con su hash
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

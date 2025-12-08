package com.eventos.utils;

import com.eventos.exceptions.ValidationException;
import java.time.LocalDateTime;

/**
 * Utilidad para validación de datos
 */
public class Validator {
    
    /**
     * Valida que un email sea válido
     */
    public static void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("El email es obligatorio");
        }
        
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(regex)) {
            throw new ValidationException("El formato del email no es válido");
        }
    }
    
    /**
     * Valida que una contraseña sea segura
     */
    public static void validarPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ValidationException("La contraseña debe tener al menos 8 caracteres");
        }
        
        boolean tieneNumero = password.matches(".*\\d.*");
        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        
        if (!tieneNumero || !tieneMayuscula || !tieneMinuscula) {
            throw new ValidationException(
                "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
            );
        }
    }
    
    /**
     * Valida que un texto no esté vacío
     */
    public static void validarNoVacio(String texto, String nombreCampo) {
        if (texto == null || texto.isBlank()) {
            throw new ValidationException(nombreCampo + " no puede estar vacío");
        }
    }
    
    /**
     * Valida que un número sea positivo
     */
    public static void validarPositivo(Number numero, String nombreCampo) {
        if (numero == null || numero.doubleValue() <= 0) {
            throw new ValidationException(nombreCampo + " debe ser un valor positivo");
        }
    }
    
    /**
     * Valida fechas de evento
     */
    public static void validarFechasEvento(LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime ahora = LocalDateTime.now();
        
        if (inicio == null || fin == null) {
            throw new ValidationException("Las fechas de inicio y fin son obligatorias");
        }
        
        if (inicio.isBefore(ahora)) {
            throw new ValidationException("La fecha de inicio no puede ser en el pasado");
        }
        
        if (fin.isBefore(inicio)) {
            throw new ValidationException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }
}

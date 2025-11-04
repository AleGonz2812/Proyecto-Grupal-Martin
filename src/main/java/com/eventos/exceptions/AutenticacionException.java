package com.eventos.exceptions;

/**
 * Excepción para errores de autenticación
 */
public class AutenticacionException extends EventosException {
    
    public AutenticacionException(String message) {
        super(message);
    }
    
    public AutenticacionException(String message, Throwable cause) {
        super(message, cause);
    }
}

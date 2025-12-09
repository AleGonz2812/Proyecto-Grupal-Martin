package com.eventos.exceptions;

/**
 * Excepción para errores de validación de datos
 */
public class ValidationException extends EventosException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

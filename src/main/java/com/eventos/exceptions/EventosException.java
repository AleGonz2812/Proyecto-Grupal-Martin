package com.eventos.exceptions;

/**
 * Excepción base para todas las excepciones del sistema de eventos
 */
public class EventosException extends RuntimeException {
    
    public EventosException(String message) {
        super(message);
    }
    
    public EventosException(String message, Throwable cause) {
        super(message, cause);
    }
}

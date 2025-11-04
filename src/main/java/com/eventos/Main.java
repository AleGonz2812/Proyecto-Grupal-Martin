package com.eventos;

import com.eventos.config.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de la aplicación
 * Sistema de Gestión de Eventos
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("Iniciando Sistema de Gestión de Eventos v1.0.0");
        
        try {
            // Inicializar Hibernate
            logger.info("Inicializando conexión a base de datos...");
            HibernateUtil.getEntityManagerFactory();
            logger.info("Conexión establecida correctamente");
            
            // TODO: Iniciar interfaz de usuario (JavaFX o Swing)
            logger.info("Sistema listo para usar");
            
        } catch (Exception e) {
            logger.error("Error al iniciar la aplicación", e);
            System.exit(1);
        }
        
        // Cerrar recursos al salir
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Cerrando aplicación...");
            HibernateUtil.shutdown();
            logger.info("Aplicación cerrada correctamente");
        }));
    }
}

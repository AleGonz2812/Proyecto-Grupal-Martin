package com.eventos.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad para gestionar la conexión con Hibernate/JPA.
 * Implementa el patrón Singleton para la EntityManagerFactory.
 */
public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static EntityManagerFactory entityManagerFactory;
    
    private HibernateUtil() {
        // Constructor privado para evitar instanciación
    }
    
    /**
     * Obtiene la EntityManagerFactory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            try {
                logger.info("Inicializando EntityManagerFactory...");
                entityManagerFactory = Persistence.createEntityManagerFactory("EventosPU");
                logger.info("EntityManagerFactory inicializado correctamente");
            } catch (Exception e) {
                logger.error("Error al inicializar EntityManagerFactory", e);
                throw new RuntimeException("No se pudo crear EntityManagerFactory", e);
            }
        }
        return entityManagerFactory;
    }
    
    /**
     * Crea un nuevo EntityManager
     */
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }
    
    /**
     * Cierra la EntityManagerFactory
     */
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            logger.info("Cerrando EntityManagerFactory...");
            entityManagerFactory.close();
            logger.info("EntityManagerFactory cerrado");
        }
    }
}

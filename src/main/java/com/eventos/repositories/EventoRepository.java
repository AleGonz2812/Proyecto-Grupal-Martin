package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.Evento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar operaciones de Evento en la base de datos
 */
public class EventoRepository extends GenericRepository<Evento, Long> {
    
    public EventoRepository() {
        super(Evento.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }
    
    /**
     * Sobrescribe findAll para cargar las relaciones con JOIN FETCH
     * @return Lista de todos los eventos con sus relaciones cargadas
     */
    @Override
    public List<Evento> findAll() {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT DISTINCT e FROM Evento e " +
            "LEFT JOIN FETCH e.tipoEvento " +
            "LEFT JOIN FETCH e.sede " +
            "ORDER BY e.fechaInicio DESC", Evento.class);
        return query.getResultList();
    }
    
    /**
     * Busca eventos por tipo de evento
     * @param tipoEventoId ID del tipo de evento
     * @return Lista de eventos de ese tipo
     */
    public List<Evento> findByTipoEvento(Long tipoEventoId) {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT e FROM Evento e WHERE e.tipoEvento.id = :tipoId", Evento.class);
        query.setParameter("tipoId", tipoEventoId);
        return query.getResultList();
    }
    
    /**
     * Busca eventos por sede
     * @param sedeId ID de la sede
     * @return Lista de eventos en esa sede
     */
    public List<Evento> findBySede(Long sedeId) {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT e FROM Evento e WHERE e.sede.id = :sedeId", Evento.class);
        query.setParameter("sedeId", sedeId);
        return query.getResultList();
    }
    
    /**
     * Busca eventos por estado
     * Valores válidos: "PROGRAMADO", "EN_CURSO", "FINALIZADO", "CANCELADO"
     * @param estado Estado del evento (usar EstadoEvento.VALOR.name())
     * @return Lista de eventos con ese estado
     */
    public List<Evento> findByEstado(String estado) {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT e FROM Evento e WHERE e.estado = com.eventos.models.EstadoEvento." + estado, Evento.class);
        return query.getResultList();
    }
    
    /**
     * Busca eventos entre dos fechas
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Lista de eventos en ese rango
     */
    public List<Evento> findByFechaRange(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT e FROM Evento e WHERE e.fechaInicio >= :inicio AND e.fechaInicio <= :fin " +
            "ORDER BY e.fechaInicio", Evento.class);
        query.setParameter("inicio", fechaInicio);
        query.setParameter("fin", fechaFin);
        return query.getResultList();
    }
    
    /**
     * Busca eventos próximos (a partir de hoy)
     * @return Lista de eventos futuros ordenados por fecha
     */
    public List<Evento> findProximosEventos() {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT e FROM Evento e WHERE e.fechaInicio >= :ahora " +
            "ORDER BY e.fechaInicio", Evento.class);
        query.setParameter("ahora", LocalDateTime.now());
        return query.getResultList();
    }
    
    /**
     * Busca eventos con entradas disponibles
     * @return Lista de eventos que aún tienen plazas
     */
    public List<Evento> findConEntradasDisponibles() {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT e FROM Evento e WHERE e.aforoActual < e.aforoMaximo " +
            "AND e.estado = com.eventos.models.EstadoEvento.PROGRAMADO ORDER BY e.fechaInicio", Evento.class);
        return query.getResultList();
    }
    
    /**
     * Busca eventos por nombre (búsqueda parcial)
     * @param nombre Término de búsqueda
     * @return Lista de eventos que coinciden
     */
    public List<Evento> searchByNombre(String nombre) {
        EntityManager em = getEntityManager();
        TypedQuery<Evento> query = em.createQuery(
            "SELECT e FROM Evento e WHERE LOWER(e.nombre) LIKE LOWER(:nombre)", Evento.class);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }
}

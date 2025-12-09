package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.Compra;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de Compra en la base de datos
 */
public class CompraRepository extends GenericRepository<Compra, Long> {
    
    public CompraRepository() {
        super(Compra.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }
    
    /**
     * Busca todas las compras de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de compras del usuario ordenadas por fecha
     */
    public List<Compra> findByUsuario(Long usuarioId) {
        EntityManager em = getEntityManager();
        TypedQuery<Compra> query = em.createQuery(
            "SELECT c FROM Compra c WHERE c.usuario.id = :usuarioId " +
            "ORDER BY c.fechaCompra DESC", Compra.class);
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }
    
    /**
     * Busca una compra por su código de confirmación único
     * @param codigoConfirmacion Código de confirmación de la compra
     * @return Optional con la compra si existe
     */
    public Optional<Compra> findByCodigoConfirmacion(String codigoConfirmacion) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Compra> query = em.createQuery(
                "SELECT c FROM Compra c WHERE c.codigoConfirmacion = :codigo", Compra.class);
            query.setParameter("codigo", codigoConfirmacion);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Busca compras por estado
     * Valores válidos: "PENDIENTE", "COMPLETADA", "CANCELADA", "REEMBOLSADA"
     * @param estado Estado de la compra (usar EstadoCompra.VALOR.name())
     * @return Lista de compras con ese estado
     */
    public List<Compra> findByEstado(String estado) {
        EntityManager em = getEntityManager();
        TypedQuery<Compra> query = em.createQuery(
            "SELECT c FROM Compra c WHERE c.estado = com.eventos.models.EstadoCompra." + estado, Compra.class);
        return query.getResultList();
    }
    
    /**
     * Busca compras realizadas en un rango de fechas
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Lista de compras en ese periodo
     */
    public List<Compra> findByFechaRange(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        EntityManager em = getEntityManager();
        TypedQuery<Compra> query = em.createQuery(
            "SELECT c FROM Compra c WHERE c.fechaCompra >= :inicio " +
            "AND c.fechaCompra <= :fin ORDER BY c.fechaCompra DESC", Compra.class);
        query.setParameter("inicio", fechaInicio);
        query.setParameter("fin", fechaFin);
        return query.getResultList();
    }
    
    /**
     * Calcula el total de ingresos de un usuario
     * @param usuarioId ID del usuario
     * @return Total gastado por el usuario
     */
    public Double getTotalGastadoPorUsuario(Long usuarioId) {
        EntityManager em = getEntityManager();
        TypedQuery<Double> query = em.createQuery(
            "SELECT SUM(c.total) FROM Compra c WHERE c.usuario.id = :usuarioId " +
            "AND c.estado = com.eventos.models.EstadoCompra.CONFIRMADA", Double.class);
        query.setParameter("usuarioId", usuarioId);
        Double total = query.getSingleResult();
        return total != null ? total : 0.0;
    }
    
    /**
     * Obtiene las últimas N compras realizadas
     * @param limit Número de compras a obtener
     * @return Lista de compras recientes
     */
    public List<Compra> findUltimasCompras(int limit) {
        EntityManager em = getEntityManager();
        TypedQuery<Compra> query = em.createQuery(
            "SELECT c FROM Compra c ORDER BY c.fechaCompra DESC", Compra.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}

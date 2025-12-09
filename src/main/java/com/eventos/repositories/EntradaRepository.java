package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.Entrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de Entrada en la base de datos
 */
public class EntradaRepository extends GenericRepository<Entrada, Long> {
    
    public EntradaRepository() {
        super(Entrada.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }
    
    /**
     * Busca todas las entradas de un evento
     * @param eventoId ID del evento
     * @return Lista de entradas del evento
     */
    public List<Entrada> findByEvento(Long eventoId) {
        EntityManager em = getEntityManager();
        TypedQuery<Entrada> query = em.createQuery(
            "SELECT e FROM Entrada e WHERE e.evento.id = :eventoId", Entrada.class);
        query.setParameter("eventoId", eventoId);
        return query.getResultList();
    }
    
    /**
     * Busca todas las entradas de una compra
     * @param compraId ID de la compra
     * @return Lista de entradas de esa compra
     */
    public List<Entrada> findByCompra(Long compraId) {
        EntityManager em = getEntityManager();
        TypedQuery<Entrada> query = em.createQuery(
            "SELECT e FROM Entrada e WHERE e.compra.id = :compraId", Entrada.class);
        query.setParameter("compraId", compraId);
        return query.getResultList();
    }
    
    /**
     * Busca una entrada por su número único
     * @param numeroEntrada Número de la entrada
     * @return Optional con la entrada si existe
     */
    public Optional<Entrada> findByNumero(String numeroEntrada) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Entrada> query = em.createQuery(
                "SELECT e FROM Entrada e WHERE e.numeroEntrada = :numero", Entrada.class);
            query.setParameter("numero", numeroEntrada);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Busca una entrada por su código QR
     * @param codigoQR Código QR de la entrada
     * @return Optional con la entrada si existe
     */
    public Optional<Entrada> findByCodigoQR(String codigoQR) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Entrada> query = em.createQuery(
                "SELECT e FROM Entrada e WHERE e.codigoQR = :codigo", Entrada.class);
            query.setParameter("codigo", codigoQR);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Busca entradas validadas o sin validar
     * @param validada true para validadas, false para sin validar
     * @return Lista de entradas según el estado
     */
    public List<Entrada> findByValidada(boolean validada) {
        EntityManager em = getEntityManager();
        TypedQuery<Entrada> query = em.createQuery(
            "SELECT e FROM Entrada e WHERE e.validada = :validada", Entrada.class);
        query.setParameter("validada", validada);
        return query.getResultList();
    }
    
    /**
     * Cuenta cuántas entradas hay para un evento
     * @param eventoId ID del evento
     * @return Número de entradas vendidas
     */
    public long countByEvento(Long eventoId) {
        EntityManager em = getEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(e) FROM Entrada e WHERE e.evento.id = :eventoId", Long.class);
        query.setParameter("eventoId", eventoId);
        return query.getSingleResult();
    }
    
    /**
     * Cuenta cuántas entradas validadas hay para un evento
     * @param eventoId ID del evento
     * @return Número de entradas validadas
     */
    public long countValidadasByEvento(Long eventoId) {
        EntityManager em = getEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(e) FROM Entrada e WHERE e.evento.id = :eventoId " +
            "AND e.validada = true", Long.class);
        query.setParameter("eventoId", eventoId);
        return query.getSingleResult();
    }
}

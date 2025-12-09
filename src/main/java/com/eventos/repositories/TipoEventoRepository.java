package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.TipoEvento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repositorio para gestionar operaciones de TipoEvento en la base de datos
 */
public class TipoEventoRepository extends GenericRepository<TipoEvento, Long> {
    
    public TipoEventoRepository() {
        super(TipoEvento.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }
    
    /**
     * Busca tipos de evento por categoría (CULTURAL, DEPORTIVO, CORPORATIVO, ENTRETENIMIENTO)
     * @param categoria Categoría del tipo de evento
     * @return Lista de tipos de evento en esa categoría
     */
    public List<TipoEvento> findByCategoria(String categoria) {
        EntityManager em = getEntityManager();
        TypedQuery<TipoEvento> query = em.createQuery(
            "SELECT t FROM TipoEvento t WHERE t.categoria = :categoria", TipoEvento.class);
        query.setParameter("categoria", categoria);
        return query.getResultList();
    }
    
    /**
     * Busca tipos de evento por nombre (búsqueda parcial)
     * @param nombre Término de búsqueda
     * @return Lista de tipos de evento que coinciden
     */
    public List<TipoEvento> searchByNombre(String nombre) {
        EntityManager em = getEntityManager();
        TypedQuery<TipoEvento> query = em.createQuery(
            "SELECT t FROM TipoEvento t WHERE LOWER(t.nombre) LIKE LOWER(:nombre)", TipoEvento.class);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }
}

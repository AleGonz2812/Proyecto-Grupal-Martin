package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.Sede;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repositorio para gestionar operaciones de Sede en la base de datos
 */
public class SedeRepository extends GenericRepository<Sede, Long> {
    
    public SedeRepository() {
        super(Sede.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }
    
    /**
     * Busca sedes activas
     * @return Lista de sedes disponibles
     */
    public List<Sede> findActivas() {
        EntityManager em = getEntityManager();
        TypedQuery<Sede> query = em.createQuery(
            "SELECT s FROM Sede s WHERE s.activa = true", Sede.class);
        return query.getResultList();
    }
    
    /**
     * Busca sedes por ciudad
     * @param ciudad Nombre de la ciudad
     * @return Lista de sedes en esa ciudad
     */
    public List<Sede> findByCiudad(String ciudad) {
        EntityManager em = getEntityManager();
        TypedQuery<Sede> query = em.createQuery(
            "SELECT s FROM Sede s WHERE LOWER(s.ciudad) = LOWER(:ciudad)", Sede.class);
        query.setParameter("ciudad", ciudad);
        return query.getResultList();
    }
    
    /**
     * Busca sedes por provincia
     * @param provincia Nombre de la provincia
     * @return Lista de sedes en esa provincia
     */
    public List<Sede> findByProvincia(String provincia) {
        EntityManager em = getEntityManager();
        TypedQuery<Sede> query = em.createQuery(
            "SELECT s FROM Sede s WHERE LOWER(s.provincia) = LOWER(:provincia)", Sede.class);
        query.setParameter("provincia", provincia);
        return query.getResultList();
    }
    
    /**
     * Busca sedes con capacidad mínima
     * @param capacidadMinima Capacidad mínima requerida
     * @return Lista de sedes con esa capacidad o mayor
     */
    public List<Sede> findByCapacidadMinima(int capacidadMinima) {
        EntityManager em = getEntityManager();
        TypedQuery<Sede> query = em.createQuery(
            "SELECT s FROM Sede s WHERE s.capacidad >= :capacidad " +
            "AND s.activa = true ORDER BY s.capacidad", Sede.class);
        query.setParameter("capacidad", capacidadMinima);
        return query.getResultList();
    }
    
    /**
     * Busca sedes por nombre (búsqueda parcial)
     * @param nombre Término de búsqueda
     * @return Lista de sedes que coinciden
     */
    public List<Sede> searchByNombre(String nombre) {
        EntityManager em = getEntityManager();
        TypedQuery<Sede> query = em.createQuery(
            "SELECT s FROM Sede s WHERE LOWER(s.nombre) LIKE LOWER(:nombre)", Sede.class);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }
}

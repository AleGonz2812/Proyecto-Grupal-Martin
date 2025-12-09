package com.eventos.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio genérico que proporciona operaciones CRUD básicas
 * Todos los repositorios específicos heredarán de esta clase
 * 
 * @param <T> Tipo de entidad (Usuario, Evento, etc.)
 * @param <ID> Tipo del ID de la entidad (Long, Integer, etc.)
 */
public abstract class GenericRepository<T, ID> {
    
    protected final Class<T> entityClass;
    
    /**
     * Constructor que recibe la clase de la entidad
     * @param entityClass Clase de la entidad a gestionar
     */
    protected GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    /**
     * Método abstracto para obtener el EntityManager
     * Cada repositorio implementará su propia forma de obtenerlo
     */
    protected abstract EntityManager getEntityManager();
    
    /**
     * Guarda una nueva entidad en la base de datos
     * @param entity Entidad a guardar
     * @return Entidad guardada con ID generado
     */
    public T save(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al guardar la entidad", e);
        }
    }
    
    /**
     * Actualiza una entidad existente
     * @param entity Entidad con los datos actualizados
     * @return Entidad actualizada
     */
    public T update(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T updated = em.merge(entity);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al actualizar la entidad", e);
        }
    }
    
    /**
     * Busca una entidad por su ID
     * @param id ID de la entidad a buscar
     * @return Optional con la entidad si existe, vacío si no
     */
    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        T entity = em.find(entityClass, id);
        return Optional.ofNullable(entity);
    }
    
    /**
     * Obtiene todas las entidades de este tipo
     * @return Lista con todas las entidades
     */
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        
        TypedQuery<T> query = em.createQuery(cq);
        return query.getResultList();
    }
    
    /**
     * Elimina una entidad por su ID
     * @param id ID de la entidad a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(ID id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al eliminar la entidad", e);
        }
    }
    
    /**
     * Cuenta el total de entidades
     * @return Número total de registros
     */
    public long count() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));
        
        return em.createQuery(cq).getSingleResult();
    }
    
    /**
     * Verifica si existe una entidad con el ID dado
     * @param id ID a verificar
     * @return true si existe, false si no
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}

package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.Rol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de Rol en la base de datos
 */
public class RolRepository extends GenericRepository<Rol, Long> {
    
    public RolRepository() {
        super(Rol.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }
    
    /**
     * Busca un rol por su nombre (ADMIN, USUARIO, EMPLEADO)
     * @param nombre Nombre del rol
     * @return Optional con el rol si existe
     */
    public Optional<Rol> findByNombre(String nombre) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Rol> query = em.createQuery(
                "SELECT r FROM Rol r WHERE r.nombre = :nombre", Rol.class);
            query.setParameter("nombre", nombre);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

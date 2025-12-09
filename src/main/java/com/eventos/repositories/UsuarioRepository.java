package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de Usuario en la base de datos
 * Extiende GenericRepository para heredar operaciones CRUD básicas
 */
public class UsuarioRepository extends GenericRepository<Usuario, Long> {
    
    /**
     * Constructor que inicializa el repositorio con la clase Usuario
     */
    public UsuarioRepository() {
        super(Usuario.class);
    }
    
    /**
     * Obtiene el EntityManager desde HibernateUtil
     */
    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }
    
    /**
     * Busca un usuario por su email (único)
     * @param email Email del usuario a buscar
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class);
            query.setParameter("email", email);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Busca usuarios por rol
     * @param rolId ID del rol a filtrar
     * @return Lista de usuarios con ese rol
     */
    public List<Usuario> findByRol(Long rolId) {
        EntityManager em = getEntityManager();
        TypedQuery<Usuario> query = em.createQuery(
            "SELECT u FROM Usuario u WHERE u.rol.id = :rolId", Usuario.class);
        query.setParameter("rolId", rolId);
        return query.getResultList();
    }
    
    /**
     * Busca usuarios activos
     * @return Lista de usuarios con estado activo
     */
    public List<Usuario> findActivos() {
        EntityManager em = getEntityManager();
        TypedQuery<Usuario> query = em.createQuery(
            "SELECT u FROM Usuario u WHERE u.activo = true", Usuario.class);
        return query.getResultList();
    }
    
    /**
     * Busca usuarios por nombre o apellidos (búsqueda parcial)
     * @param searchTerm Término de búsqueda
     * @return Lista de usuarios que coinciden
     */
    public List<Usuario> searchByName(String searchTerm) {
        EntityManager em = getEntityManager();
        TypedQuery<Usuario> query = em.createQuery(
            "SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(:search) " +
            "OR LOWER(u.apellidos) LIKE LOWER(:search)", Usuario.class);
        query.setParameter("search", "%" + searchTerm + "%");
        return query.getResultList();
    }
    
    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    public boolean existsByEmail(String email) {
        EntityManager em = getEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class);
        query.setParameter("email", email);
        return query.getSingleResult() > 0;
    }
    
    /**
     * Verifica si existe un usuario con el DNI dado
     * @param dni DNI a verificar
     * @return true si existe, false si no
     */
    public boolean existsByDni(String dni) {
        EntityManager em = getEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(u) FROM Usuario u WHERE u.dni = :dni", Long.class);
        query.setParameter("dni", dni);
        return query.getSingleResult() > 0;
    }
}

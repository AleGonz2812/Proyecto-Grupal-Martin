package com.eventos.repositories;

import com.eventos.config.HibernateUtil;
import com.eventos.models.TipoEntrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

public class TipoEntradaRepository extends GenericRepository<TipoEntrada, Long> {

    public TipoEntradaRepository() {
        super(TipoEntrada.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }

    public Optional<TipoEntrada> findByNombre(String nombre) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<TipoEntrada> q = em.createQuery(
                    "SELECT t FROM TipoEntrada t WHERE LOWER(t.nombre) = LOWER(:n)", TipoEntrada.class);
            q.setParameter("n", nombre);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}

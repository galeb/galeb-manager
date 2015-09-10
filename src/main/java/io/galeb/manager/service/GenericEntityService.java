package io.galeb.manager.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Rule;

@Service
public class GenericEntityService {

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public boolean isNew(AbstractEntity<?> entity) {
        List<Rule> result = em.createQuery("SELECT x FROM " + entity.getClass().getSimpleName() + " x "
                                         + "WHERE x.id = :id")
                              .setParameter("id", entity.getId()).getResultList();

        return result.isEmpty();
    }

}

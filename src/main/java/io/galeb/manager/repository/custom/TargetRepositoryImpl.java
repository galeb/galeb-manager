package io.galeb.manager.repository.custom;

import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TargetRepositoryImpl implements TargetRepositoryCustom {

    private static final String SELECT_POOL    = "SELECT p FROM Pool p WHERE p.name = :pool";
    private static final String SELECT_TARGETS = "SELECT t FROM Target t WHERE t.parent.name = :pool";

    private static final Log LOGGER = LogFactory.getLog(TargetRepositoryImpl.class);

    @PersistenceContext private EntityManager em;

    @Override
    public List<Target> allAvaliablesOf(String pool) {
        Pool poolObj = em.createQuery(SELECT_POOL, Pool.class).setParameter("pool", pool).getSingleResult();
        if (poolObj == null) {
            LOGGER.error("Pool NOT FOUND");
            return Collections.emptyList();
        } else {
            TypedQuery<Target> targets = em.createQuery(SELECT_TARGETS, Target.class).setParameter("pool", pool);
            return targets.getResultList().stream()
                    .filter(target -> Optional.ofNullable(target.getProperties().get("health")).orElse("UNKNOWN").equals("OK"))
                    .collect(Collectors.toList());
        }
    }
}

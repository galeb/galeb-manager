package io.galeb.manager.repository.custom;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Repository;

import io.galeb.manager.entity.Target;

@Repository
public class TargetRepositoryImpl extends AbstractRepositoryImplementation<Target>
                                  implements TargetRepositoryCustom {

    public static final String FIND_ALL = "SELECT ta FROM Target ta "
                                            + "INNER JOIN ta.project.teams t "
                                            + "INNER JOIN t.accounts a "
                                            + "WHERE 1 = :hasRoleAdmin OR "
                                            + "ta.global = TRUE OR "
                                            + "a.name = :principalName";

    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(TargetRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unused")
    private JpaEntityInformation<Target, ?> entityInformation;

    @PostConstruct
    public void init() {
        entityInformation = JpaEntityInformationSupport.getMetadata(Target.class, em);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected String getFindAllStr() {
        return FIND_ALL;
    }

}

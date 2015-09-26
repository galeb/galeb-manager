package io.galeb.manager.repository.custom;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Repository;

import io.galeb.manager.entity.Rule;

@Repository
public class RuleRepositoryImpl extends AbstractRepositoryImplementation<Rule>
                                implements RuleRepositoryCustom {

    public static final String FIND_ALL = "SELECT r FROM Rule r "
                                           + "INNER JOIN r.pool.project.teams t "
                                           + "INNER JOIN t.accounts a "
                                           + "WHERE 1 = :hasRoleAdmin OR "
                                                + "r.global = TRUE OR "
                                                + "a.name = :principalName";

    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(RuleRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private JpaEntityInformation<Rule, ?> entityInformation;

    @PostConstruct
    public void init() {
        entityInformation = JpaEntityInformationSupport.getMetadata(Rule.class, em);
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

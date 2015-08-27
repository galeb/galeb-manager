package io.galeb.manager.repository.custom;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Repository;

import io.galeb.manager.entity.Project;

@Repository
public class ProjectRepositoryImpl extends AbstractRepositoryCustom<Project>
                                   implements ProjectRepositoryCustom {

    private static final String FIND_ALL = "SELECT p FROM Project p "
                                           + "INNER JOIN p.teams t "
                                           + "INNER JOIN t.accounts a "
                                           + "WHERE 1 = :hasRoleAdmin OR "
                                           + "a.name = :principalName";

    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(ProjectRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unused")
    private JpaEntityInformation<Project, ?> entityInformation;

    @PostConstruct
    public void init() {
        entityInformation = JpaEntityInformationSupport.getMetadata(Project.class, em);
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

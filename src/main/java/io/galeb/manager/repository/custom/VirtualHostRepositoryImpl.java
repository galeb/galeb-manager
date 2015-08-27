package io.galeb.manager.repository.custom;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Repository;

import io.galeb.manager.entity.VirtualHost;

@Repository
public class VirtualHostRepositoryImpl extends AbstractRepositoryCustom<VirtualHost>
                                       implements VirtualHostRepositoryCustom {

    private static final String FIND_ALL = "SELECT v FROM VirtualHost v "
                                            + "INNER JOIN v.project.teams t "
                                            + "INNER JOIN t.accounts a "
                                            + "WHERE 1 = :hasRoleAdmin OR "
                                            + "a.name = :principalName";

    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(VirtualHostRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unused")
    private JpaEntityInformation<VirtualHost, ?> entityInformation;

    @PostConstruct
    public void init() {
        entityInformation = JpaEntityInformationSupport.getMetadata(VirtualHost.class, em);
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

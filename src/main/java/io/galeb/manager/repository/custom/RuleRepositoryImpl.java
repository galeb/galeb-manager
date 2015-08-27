package io.galeb.manager.repository.custom;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Repository;

import io.galeb.manager.entity.Rule;

@Repository
public class RuleRepositoryImpl implements RuleRepositoryCustom {

    private static final Log LOGGER = LogFactory.getLog(RuleRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    private JpaEntityInformation<Rule, ?> entityInformation;

    @PostConstruct
    public void init() {
        entityInformation = JpaEntityInformationSupport.getMetadata(Rule.class, em);
    }

    @Override
    @Transactional
    public Rule save(Rule rule) {
        Rule ruleSaved = null;
        try {
            if (entityInformation.isNew(rule)) {
                em.persist(rule);
                ruleSaved = rule;
            } else {
                Rule oldRule = em.find(Rule.class, rule.getId());
                if (oldRule.getParent() == null) {
                    rule.setParent(null);
                }
                ruleSaved = em.merge(rule);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return ruleSaved;
    }

}

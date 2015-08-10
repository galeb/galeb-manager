package io.galeb.handler;

import io.galeb.engine.farm.RuleEngine;
import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Farm;
import io.galeb.entity.Rule;
import io.galeb.entity.VirtualHost;
import io.galeb.repository.FarmRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.jms.core.JmsTemplate;

@RepositoryEventHandler(Rule.class)
public class RuleHandler {

    private static Log LOGGER = LogFactory.getLog(RuleHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private FarmRepository farmRepository;

    private void setBestFarm(final Rule rule) {
        long farmId = -1L;
        if (rule.getParent() != null) {
            final VirtualHost virtualhost = rule.getParent();
            farmId = virtualhost.getFarmId();
        } else {
            final Farm farm = farmRepository.findByEnvironmentAndStatus(rule.getEnvironment(), EntityStatus.OK)
                    .stream().findFirst().orElse(null);
            farmId = farm.getId();
        }
        rule.setFarmId(farmId);
    }

    @HandleBeforeCreate
    public void beforeCreate(Rule rule) {
        LOGGER.info("Rule: HandleBeforeCreate");
        setBestFarm(rule);
    }

    @HandleAfterCreate
    public void afterCreate(Rule rule) {
        LOGGER.info("Rule: HandleAfterCreate");
        jms.convertAndSend(RuleEngine.QUEUE_CREATE, rule);
    }

    @HandleBeforeSave
    public void beforeSave(Rule rule) {
        LOGGER.info("Rule: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Rule rule) {
        LOGGER.info("Rule: HandleAfterSave");
        jms.convertAndSend(RuleEngine.QUEUE_UPDATE, rule);
    }

    @HandleBeforeDelete
    public void beforeDelete(Rule rule) {
        LOGGER.info("Rule: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Rule rule) {
        LOGGER.info("Rule: HandleAfterDelete");
        jms.convertAndSend(RuleEngine.QUEUE_REMOVE, rule);
    }

}

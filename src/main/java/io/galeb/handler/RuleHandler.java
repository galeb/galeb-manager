package io.galeb.handler;

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

import io.galeb.engine.farm.RuleEngine;
import io.galeb.entity.Rule;

@RepositoryEventHandler(Rule.class)
public class RuleHandler {

    private static Log LOGGER = LogFactory.getLog(RuleHandler.class);

    @Autowired
    private JmsTemplate jms;

    @HandleBeforeCreate
    public void beforeCreate(Rule rule) {
        LOGGER.info("Rule: HandleBeforeCreate");
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

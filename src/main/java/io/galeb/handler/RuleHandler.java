package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.entity.Rule;

@RepositoryEventHandler(Rule.class)
public class RuleHandler {

    private static Log LOGGER = LogFactory.getLog(RuleHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Rule rule) {
        LOGGER.info("Rule: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(Rule rule) {
        LOGGER.info("Rule: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Rule rule) {
        LOGGER.info("Rule: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Rule rule) {
        LOGGER.info("Rule: HandleAfterSave");
    }

}

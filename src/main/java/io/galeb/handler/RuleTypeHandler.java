package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.entity.RuleType;

@RepositoryEventHandler(RuleType.class)
public class RuleTypeHandler {

    private static Log LOGGER = LogFactory.getLog(RuleTypeHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(RuleType ruleType) {
        LOGGER.info("RuleType: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(RuleType ruleType) {
        LOGGER.info("RuleType: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(RuleType ruleType) {
        LOGGER.info("RuleType: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(RuleType ruleType) {
        LOGGER.info("RuleType: HandleAfterSave");
    }

}

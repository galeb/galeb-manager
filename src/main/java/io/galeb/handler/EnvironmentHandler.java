package io.galeb.handler;

import io.galeb.entity.Environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler(Environment.class)
public class EnvironmentHandler {

    private static Log LOGGER = LogFactory.getLog(EnvironmentHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Environment environment) {
        LOGGER.info("Environment: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(Environment environment) {
        LOGGER.info("Environment: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Environment environment) {
        LOGGER.info("Environment: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Environment environment) {
        LOGGER.info("Environment: HandleAfterSave");
    }

}

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

import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Environment;

@RepositoryEventHandler(Environment.class)
public class EnvironmentHandler {

    private static Log LOGGER = LogFactory.getLog(EnvironmentHandler.class);

    @Autowired
    private JmsTemplate jms;

    @HandleBeforeCreate
    public void beforeCreate(Environment environment) {
        LOGGER.info("Environment: HandleBeforeCreate");
        environment.setStatus(EntityStatus.OK);
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

    @HandleBeforeDelete
    public void beforeDelete(Environment environment) {
        LOGGER.info("Environment: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Environment environment) {
        LOGGER.info("Environment: HandleAfterDelete");
    }

}

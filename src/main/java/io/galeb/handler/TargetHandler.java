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

import io.galeb.engine.farm.TargetEngine;
import io.galeb.entity.Target;

@RepositoryEventHandler(Target.class)
public class TargetHandler {

    private static Log LOGGER = LogFactory.getLog(TargetHandler.class);

    @Autowired
    private JmsTemplate jms;

    @HandleBeforeCreate
    public void beforeCreate(Target target) {
        LOGGER.info("Target: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(Target target) {
        LOGGER.info("Target: HandleAfterCreate");
        jms.convertAndSend(TargetEngine.QUEUE_CREATE, target);
    }

    @HandleBeforeSave
    public void beforeSave(Target target) {
        LOGGER.info("Target: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Target target) {
        LOGGER.info("Target: HandleAfterSave");
        jms.convertAndSend(TargetEngine.QUEUE_UPDATE, target);
    }

    @HandleBeforeDelete
    public void beforeDelete(Target target) {
        LOGGER.info("Target: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Target target) {
        LOGGER.info("Target: HandleAfterDelete");
        jms.convertAndSend(TargetEngine.QUEUE_REMOVE, target);
    }

}

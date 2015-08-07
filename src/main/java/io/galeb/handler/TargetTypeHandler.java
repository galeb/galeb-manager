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

import io.galeb.engine.farm.TargetTypeEngine;
import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.TargetType;

@RepositoryEventHandler(TargetType.class)
public class TargetTypeHandler {

    private static Log LOGGER = LogFactory.getLog(TargetTypeHandler.class);

    @Autowired
    private JmsTemplate jms;

    @HandleBeforeCreate
    public void beforeCreate(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeCreate");
        targetType.setStatus(EntityStatus.OK);
    }

    @HandleAfterCreate
    public void afterCreate(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterCreate");
        jms.convertAndSend(TargetTypeEngine.QUEUE_CREATE, targetType);
    }

    @HandleBeforeSave
    public void beforeSave(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterSave");
        jms.convertAndSend(TargetTypeEngine.QUEUE_UPDATE, targetType);
    }

    @HandleBeforeDelete
    public void beforeDelete(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterDelete");
        jms.convertAndSend(TargetTypeEngine.QUEUE_REMOVE, targetType);
    }

}

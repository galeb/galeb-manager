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

import io.galeb.engine.farm.FarmEngine;
import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Farm;

@RepositoryEventHandler(Farm.class)
public class FarmHandler {

    private static Log LOGGER = LogFactory.getLog(FarmHandler.class);

    @Autowired
    private JmsTemplate jms;

    @HandleBeforeCreate
    public void beforeCreate(Farm farm) {
        LOGGER.info("Farm: HandleBeforeCreate");
        farm.setStatus(EntityStatus.OK);
    }

    @HandleAfterCreate
    public void afterCreate(Farm farm) {
        LOGGER.info("Farm: HandleAfterCreate");
        jms.convertAndSend(FarmEngine.QUEUE_CREATE, farm);
    }

    @HandleBeforeSave
    public void beforeSave(Farm farm) {
        LOGGER.info("Farm: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Farm farm) {
        LOGGER.info("Farm: HandleAfterSave");
        jms.convertAndSend(FarmEngine.QUEUE_UPDATE, farm);
    }

    @HandleBeforeDelete
    public void beforeDelete(Farm farm) {
        LOGGER.info("Farm: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Farm farm) {
        LOGGER.info("Farm: HandleAfterDelete");
        jms.convertAndSend(FarmEngine.QUEUE_REMOVE, farm);
    }

}

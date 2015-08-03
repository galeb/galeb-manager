package io.galeb.handler;

import io.galeb.entity.Farm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler(Farm.class)
public class FarmHandler {

    private static Log LOGGER = LogFactory.getLog(FarmHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Farm farm) {
        LOGGER.info("Farm: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(Farm farm) {
        LOGGER.info("Farm: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Farm farm) {
        LOGGER.info("Farm: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Farm farm) {
        LOGGER.info("Farm: HandleAfterSave");
    }

}

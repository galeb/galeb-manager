package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.entity.TargetType;

@RepositoryEventHandler(TargetType.class)
public class TargetTypeHandler {

    private static Log LOGGER = LogFactory.getLog(TargetTypeHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterSave");
    }

}

package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.entity.Target;

@RepositoryEventHandler(Target.class)
public class TargetHandler {

    private static Log LOGGER = LogFactory.getLog(TargetHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Target target) {
        LOGGER.info("Target: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(Target target) {
        LOGGER.info("Target: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Target target) {
        LOGGER.info("Target: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Target target) {
        LOGGER.info("Target: HandleAfterSave");
    }

}

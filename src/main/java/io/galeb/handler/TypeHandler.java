package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.entity.Type;

@RepositoryEventHandler(Type.class)
public class TypeHandler {

    private static Log LOGGER = LogFactory.getLog(TypeHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Type type) {
        LOGGER.info("Type: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(Type type) {
        LOGGER.info("Type: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Type type) {
        LOGGER.info("Type: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Type type) {
        LOGGER.info("Type: HandleAfterSave");
    }

}

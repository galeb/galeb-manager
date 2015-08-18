package io.galeb.manager.handler;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.OK;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.manager.entity.TargetType;

@RepositoryEventHandler(TargetType.class)
public class TargetTypeHandler {

    private static Log LOGGER = LogFactory.getLog(TargetTypeHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeCreate");
        targetType.setStatus(OK);
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

    @HandleBeforeDelete
    public void beforeDelete(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterDelete");
    }

}

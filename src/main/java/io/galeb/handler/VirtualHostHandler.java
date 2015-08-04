package io.galeb.handler;

import io.galeb.entity.VirtualHost;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler(VirtualHost.class)
public class VirtualHostHandler {

    private static Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleAfterSave");
    }

}

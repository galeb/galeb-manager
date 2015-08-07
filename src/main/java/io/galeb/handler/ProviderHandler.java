package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.engine.DriverBuilder;
import io.galeb.engine.ProvisioningBuilder;
import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Provider;

@RepositoryEventHandler(Provider.class)
public class ProviderHandler {

    private static Log LOGGER = LogFactory.getLog(ProviderHandler.class);

    private void checkProvider(final Provider provider) {
        provider.setStatus(EntityStatus.OK);
        provider.setDriver(DriverBuilder.build(provider.getDriver()).toString());
        provider.setProvisioning(ProvisioningBuilder.build(provider.getProvisioning()).toString());
    }

    @HandleBeforeCreate
    public void beforeCreate(Provider provider) {
        LOGGER.info("Provider: HandleBeforeCreate");
        checkProvider(provider);
    }

    @HandleAfterCreate
    public void afterCreate(Provider provider) {
        LOGGER.info("Provider: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Provider provider) {
        LOGGER.info("Provider: HandleBeforeSave");
        checkProvider(provider);
    }

    @HandleAfterSave
    public void afterSave(Provider provider) {
        LOGGER.info("Provider: HandleAfterSave");
    }

    @HandleBeforeDelete
    public void beforeDelete(Provider provider) {
        LOGGER.info("Provider: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Provider provider) {
        LOGGER.info("Provider: HandleAfterDelete");
    }

}

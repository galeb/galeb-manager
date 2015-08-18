/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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

import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.engine.ProvisioningBuilder;
import io.galeb.manager.entity.Provider;

@RepositoryEventHandler(Provider.class)
public class ProviderHandler {

    private static Log LOGGER = LogFactory.getLog(ProviderHandler.class);

    private void checkProvider(final Provider provider) {
        provider.setStatus(OK);
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

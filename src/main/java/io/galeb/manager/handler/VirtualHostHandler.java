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

import java.util.Iterator;

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
import org.springframework.security.core.Authentication;

import io.galeb.manager.engine.farm.VirtualHostEngine;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

@RepositoryEventHandler(VirtualHost.class)
public class VirtualHostHandler extends RoutableToEngine<VirtualHost> {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private VirtualHostRepository virtualHostRepository;

    public VirtualHostHandler() {
        setQueueCreateName(VirtualHostEngine.QUEUE_CREATE);
        setQueueUpdateName(VirtualHostEngine.QUEUE_UPDATE);
        setQueueRemoveName(VirtualHostEngine.QUEUE_REMOVE);
    }

    @Override
    protected void setBestFarm(final VirtualHost virtualhost) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Iterator<Farm> farmIterable = farmRepository.findByEnvironmentAndStatus(virtualhost.getEnvironment(), EntityStatus.OK).iterator();
        final Farm farm = farmIterable.hasNext() ? farmIterable.next() : null;
        SystemUserService.runAs(currentUser);
        if (farm!=null) {
            virtualhost.setFarmId(farm.getId());
        }
    }

    @HandleBeforeCreate
    public void beforeCreate(VirtualHost virtualhost) throws Exception {
        virtualhost.setFarmId(-1L);
        beforeCreate(virtualhost, LOGGER);
    }

    @HandleAfterCreate
    public void afterCreate(VirtualHost virtualhost) throws Exception {
        afterCreate(virtualhost, jms, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(VirtualHost virtualhost) throws Exception {
        beforeSave(virtualhost, virtualHostRepository, LOGGER);
    }

    @HandleAfterSave
    public void afterSave(VirtualHost virtualhost) throws Exception {
        afterSave(virtualhost, jms, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(VirtualHost virtualhost) {
        beforeDelete(virtualhost, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(VirtualHost virtualhost) throws Exception {
        afterDelete(virtualhost, jms, LOGGER);
    }

}

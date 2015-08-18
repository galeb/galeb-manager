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

import io.galeb.manager.engine.farm.TargetEngine;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Target;
import io.galeb.manager.exceptions.BadRequestException;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.TargetRepository;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.OK;

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

@RepositoryEventHandler(Target.class)
public class TargetHandler extends RoutableToEngine<Target> {

    private static Log LOGGER = LogFactory.getLog(TargetHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private FarmRepository farmRepository;

    public TargetHandler() {
        setQueueCreateName(TargetEngine.QUEUE_CREATE);
        setQueueUpdateName(TargetEngine.QUEUE_UPDATE);
        setQueueRemoveName(TargetEngine.QUEUE_REMOVE);
    }

    @Override
    protected void setBestFarm(final Target target) throws Exception {
        long farmId = -1L;
        if (target.getParent() != null) {
            final Target targetParent = target.getParent();
            farmId = targetParent.getFarmId();
        } else {
            final Environment environment = target.getEnvironment();
            if (environment != null) {
                final Farm farm = farmRepository.findByEnvironmentAndStatus(environment, OK)
                        .stream().findFirst().orElse(null);
                if (farm != null) {
                    farmId = farm.getId();
                }
            } else {
                final String errorMgs = "Target.environment and Target.parent are null";
                LOGGER.error(errorMgs);
                throw new BadRequestException(errorMgs);
            }
        }
        target.setFarmId(farmId);
    }

    @HandleBeforeCreate
    public void beforeCreate(Target target) throws Exception {
        target.setFarmId(-1L);
        beforeCreate(target, LOGGER);
        setProject(target);
    }

    @HandleAfterCreate
    public void afterCreate(Target target) throws Exception {
        afterCreate(target, jms, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(Target target) throws Exception {
        beforeSave(target, targetRepository, LOGGER);
    }

    @HandleAfterSave
    public void afterSave(Target target) throws Exception {
        afterSave(target, jms, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(Target target) throws Exception {
        beforeDelete(target, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(Target target) throws Exception {
        afterDelete(target, jms, LOGGER);
    }

    private void setProject(Target target) throws Exception {
        if (target.getParent() != null) {
            if (target.getProject() == null) {
                target.setProject(target.getParent().getProject());
            } else {
                if (!target.getProject().equals(target.getParent().getProject())) {
                    final String errorMsg = "Target Project is not equal of the Parent Project";
                    LOGGER.error(errorMsg);
                    throw new BadRequestException(errorMsg);
                }
            }
        } else {
            if (target.getProject()==null) {
                final String errorMsg = "Target Project and Parent is null";
                LOGGER.error(errorMsg);
                throw new BadRequestException(errorMsg);
            }
        }
    }

}

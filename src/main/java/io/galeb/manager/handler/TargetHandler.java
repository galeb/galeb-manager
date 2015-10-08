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

import io.galeb.manager.entity.*;
import io.galeb.manager.repository.PoolRepository;
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
import org.springframework.security.core.Authentication;

import io.galeb.manager.exceptions.BadRequestException;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

@RepositoryEventHandler(Target.class)
public class TargetHandler extends AbstractHandler<Target> {

    private static Log LOGGER = LogFactory.getLog(TargetHandler.class);

    @Autowired private TargetRepository targetRepository;
    @Autowired private PoolRepository poolRepository;
    @Autowired private FarmRepository farmRepository;

    @Override
    protected void setBestFarm(final Target target) throws Exception {
        long farmId = -1L;
        if (target.getParent() != null && !target.getParent().equals(poolRepository.getNoParent())) {
            farmId = target.getParent().getFarmId();
        } else {
            final Environment environment = target.getEnvironment();
            if (environment != null) {
                Authentication currentUser = CurrentUser.getCurrentAuth();
                SystemUserService.runAs();
                final Iterable<Farm> farmIterable = farmRepository.findByEnvironment(environment);
                final Farm farm = farmIterable.iterator().hasNext() ? farmIterable.iterator().next() : null;
                SystemUserService.runAs(currentUser);
                if (farm != null) {
                    farmId = farm.getId();
                }
            } else {
                final String errorMgs = "Target.environment and Target.pool are null";
                LOGGER.error(errorMgs);
                throw new BadRequestException(errorMgs);
            }
        }
        target.setFarmId(farmId);
    }

    @HandleBeforeCreate
    public void beforeCreate(Target target) throws Exception {
        target.setFarmId(-1L);
        setParentIfNull(target);
        setProject(target);
        setEnvironment(target);
        setGlobalIfNecessary(target);
        beforeCreate(target, LOGGER);
    }

    @HandleAfterCreate
    public void afterCreate(Target target) throws Exception {
        afterCreate(target, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(Target target) throws Exception {
        setParentIfNull(target);
        beforeSave(target, targetRepository, LOGGER);
        setGlobalIfNecessary(target);
    }

    @HandleAfterSave
    public void afterSave(Target target) throws Exception {
        afterSave(target, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(Target target) throws Exception {
        beforeDelete(target, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(Target target) throws Exception {
        afterDelete(target, LOGGER);
    }

    private void setProject(Target target) throws Exception {
        if (target.getParent() != null && !target.getParent().equals(poolRepository.getNoParent())) {
            final Project projectOfParent = target.getParent().getProject();

            if (target.getProject() == null) {
                target.setProject(projectOfParent);
            } else {
                if (!target.getProject().equals(projectOfParent)) {
                    final String errorMsg = "Target.project is not equal of the Pool's Project";
                    LOGGER.error(errorMsg);
                    throw new BadRequestException(errorMsg);
                }
            }
        } else {
            if (target.getProject() == null) {
                final String errorMsg = "Target.project and Target.pool are NULL";
                LOGGER.error(errorMsg);
                throw new BadRequestException(errorMsg);
            }
        }
    }

    private void setEnvironment(Target target) throws Exception {
        if (target.getParent() != null && !target.getParent().equals(poolRepository.getNoParent())) {
            final Environment envOfParent = target.getParent().getEnvironment();
            if (target.getEnvironment() == null) {
                target.setEnvironment(envOfParent);
            } else {
                if (!target.getEnvironment().equals(envOfParent)) {
                    final String errorMsg = "Target's Environment is not equal of the Parent's Environment";
                    LOGGER.error(errorMsg);
                    throw new BadRequestException(errorMsg);
                }
            }
        } else {
            if (target.getEnvironment() == null) {
                final String errorMsg = "Both Target Environment and Parent are null";
                LOGGER.error(errorMsg);
                throw new BadRequestException(errorMsg);
            }
        }
    }

    private void setGlobalIfNecessary(Target target) {
        target.setGlobal(target.getParent() != null && target.getParent().isGlobal());
    }

    private void setParentIfNull(Target target) {
        if (target.getParent() == null) {
            target.setParent(poolRepository.getNoParent());
        }
    }
}

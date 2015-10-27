/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.galeb.manager.handler;

import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.exceptions.BadRequestException;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.PoolRepository;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.Authentication;

@RepositoryEventHandler(Pool.class)
public class PoolHandler extends AbstractHandler<Pool> {

    private static Log LOGGER = LogFactory.getLog(PoolHandler.class);

    @Autowired private PoolRepository poolRepository;
    @Autowired private FarmRepository farmRepository;

    private PageRequest pageable = new PageRequest(0, 99999);

    @Override
    protected void setBestFarm(final Pool pool) throws Exception {
        long farmId = -1L;
        final Environment environment = pool.getEnvironment();
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Iterable<Farm> farmIterable = farmRepository.findByEnvironment(environment);
        final Farm farm = farmIterable.iterator().hasNext() ? farmIterable.iterator().next() : null;
        SystemUserService.runAs(currentUser);
        if (farm != null) {
            farmId = farm.getId();
        }
        pool.setFarmId(farmId);
    }

    @HandleBeforeCreate
    public void beforeCreate(Pool pool) throws Exception {
        pool.setFarmId(-1L);
        beforeCreate(pool, LOGGER);
        setGlobalIfNecessary(pool);
    }

    @HandleAfterCreate
    public void afterCreate(Pool pool) throws Exception {
        afterCreate(pool, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(Pool pool) throws Exception {
        if (pool.getName().equals("NoParent")) {
            LOGGER.info("Pool: HandleBeforeSave");
            throw new BadRequestException();
        }
        beforeSave(pool, poolRepository, LOGGER);
        setGlobalIfNecessary(pool);
    }

    @HandleAfterSave
    public void afterSave(Pool pool) throws Exception {
        afterSave(pool, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(Pool pool) throws Exception {
        if (pool.getName().equals("NoParent")) {
            LOGGER.info("Pool: HandleBeforeDelete");
            throw new BadRequestException();
        }
        beforeDelete(pool, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(Pool pool) throws Exception {
        afterDelete(pool, LOGGER);
    }

    private void setGlobalIfNecessary(Pool pool) {
        pool.setGlobal(pool.getRules().stream().map(Rule::isGlobal).filter(b -> b).count() > 0);
    }

}

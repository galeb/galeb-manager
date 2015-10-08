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

package io.galeb.manager.engine.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.core.model.BackendPool;
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.PoolQueue;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.PoolRepository;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.engine.listeners.services.GenericEntityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PoolEngine extends AbstractEngine<Pool> {

    private static final Log LOGGER = LogFactory.getLog(PoolEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private PoolRepository poolRepository;

    @Autowired
    private GenericEntityService genericEntityService;

    @Autowired
    private PoolQueue poolQueue;

    @Autowired
    private FarmQueue farmQueue;

    @JmsListener(destination = PoolQueue.QUEUE_CREATE)
    public void create(Pool pool) {
        LOGGER.info("Creating " + pool.getClass().getSimpleName() + " " + pool.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(pool).get());
        createTarget(pool, driver);
    }

    private void createTarget(Pool pool, final Driver driver) {
        boolean isOk = false;
        try {
            isOk = driver.create(makeProperties(pool));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            pool.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            poolQueue.sendToQueue(PoolQueue.QUEUE_CALLBK, pool);
        }
    }

    @JmsListener(destination = PoolQueue.QUEUE_UPDATE)
    public void update(Pool pool) {
        LOGGER.info("Updating " + pool.getClass().getSimpleName() + " " + pool.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(pool).get());
        updateTarget(pool, driver);
    }

    private void updateTarget(final Pool pool, final Driver driver) {
        boolean isOk = false;
        try {
            isOk = driver.update(makeProperties(pool));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            pool.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            poolQueue.sendToQueue(PoolQueue.QUEUE_CALLBK, pool);
        }
    }

    @JmsListener(destination = PoolQueue.QUEUE_REMOVE)
    public void remove(Pool pool) {
        LOGGER.info("Removing " + pool.getClass().getSimpleName() + " " + pool.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(pool).get());
        removeTarget(pool, driver);
    }

    private void removeTarget(final Pool pool, final Driver driver) {
        boolean isOk = false;

        try {
            isOk = driver.remove(makeProperties(pool));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            pool.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            poolQueue.sendToQueue(PoolQueue.QUEUE_CALLBK, pool);
        }
    }

    @JmsListener(destination = PoolQueue.QUEUE_CALLBK)
    public void callBack(Pool pool) {
        if (genericEntityService.isNew(pool)) {
            // pool removed?
            return;
        }
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        try {
            poolRepository.save(pool);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            setFarmStatusOnError(pool);
        } finally {
            SystemUserService.runAs(currentUser);
        }
    }

    @Override
    protected FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Override
    protected FarmQueue farmQueue() {
        return farmQueue;
    }

    private Properties makeProperties(Pool pool) {
        String json = "{}";
        try {
            if (pool.getBalancePolicy() != null) {
                pool.getProperties().put(BackendPool.PROP_LOADBALANCE_POLICY, pool.getBalancePolicy().getBalancePolicyType().getName());
                pool.getProperties().putAll(pool.getBalancePolicy().getProperties());
            }
            final JsonMapper jsonMapper = new JsonMapper().makeJson(pool);
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        final Properties properties = fromEntity(pool);
        properties.put("json", json);
        properties.put("path", BackendPool.class.getSimpleName().toLowerCase());
        return properties;
    }
}

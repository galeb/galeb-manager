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
import io.galeb.manager.engine.listeners.services.QueueLocator;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.PoolQueue;
import io.galeb.manager.repository.FarmRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PoolEngine extends AbstractEngine<Pool> {

    private static final Log LOGGER = LogFactory.getLog(PoolEngine.class);

    @Override
    protected Log getLogger() {
        return LOGGER;
    }

    private FarmRepository farmRepository;
    private QueueLocator queueLocator;

    @JmsListener(destination = PoolQueue.QUEUE_CREATE)
    public void create(Pool pool, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Creating " + pool.getClass().getSimpleName() + " " + pool.getName());
        final Driver driver = getDriver(pool);
        try {
            driver.create(makeProperties(pool, jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @JmsListener(destination = PoolQueue.QUEUE_UPDATE)
    public void update(Pool pool, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Updating " + pool.getClass().getSimpleName() + " " + pool.getName());
        final Driver driver = getDriver(pool);
        try {
            driver.update(makeProperties(pool, jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public Farm getFarmById(long id) {
        return getFarmRepository() != null ? getFarmRepository().findOne(id) : null;
    }

    @JmsListener(destination = PoolQueue.QUEUE_REMOVE)
    public void remove(Pool pool, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Removing " + pool.getClass().getSimpleName() + " " + pool.getName());
        final Driver driver = getDriver(pool);

        try {
            driver.remove(makeProperties(pool, jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    protected FarmQueue farmQueue() {
        return (FarmQueue) getQueueLocator().getQueue(Farm.class);
    }

    public Properties makeProperties(Pool pool, Map<String, String> jmsHeaders) {
        String json = "{}";
        try {
            if (pool.getBalancePolicy() != null) {
                pool.getProperties().put(BackendPool.PROP_LOADBALANCE_POLICY, pool.getBalancePolicy().getBalancePolicyType().getName());
                pool.getProperties().putAll(pool.getBalancePolicy().getProperties());
            }
            final JsonMapper jsonMapper = new JsonMapper().makeJson(pool);
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        final Properties properties = fromEntity(pool, jmsHeaders);
        properties.put(JSON_PROP, json);
        properties.put(PATH_PROP, BackendPool.class.getSimpleName().toLowerCase());
        return properties;
    }

    public FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Autowired
    public PoolEngine setFarmRepository(final FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
        return this;
    }

    public QueueLocator getQueueLocator() {
        return queueLocator;
    }

    @Autowired
    public PoolEngine setQueueLocator(final QueueLocator queueLocator) {
        this.queueLocator = queueLocator;
        return this;
    }
}

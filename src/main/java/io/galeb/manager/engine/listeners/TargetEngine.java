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

package io.galeb.manager.engine.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.core.model.Backend;
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.listeners.services.QueueLocator;
import io.galeb.manager.engine.util.CounterDownLatch;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.TargetQueue;
import io.galeb.manager.repository.FarmRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Map;

@Component
public class TargetEngine extends AbstractEngine<Target> {

    private static final Log LOGGER = LogFactory.getLog(TargetEngine.class);

    private CounterDownLatch counterDownLatch;

    @Override
    protected Log getLogger() {
        return LOGGER;
    }

    @PersistenceContext
    private EntityManager em;

    private FarmRepository farmRepository;
    private QueueLocator queueLocator;

    @JmsListener(destination = TargetQueue.QUEUE_CREATE)
    public void create(Target target, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Creating "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = getDriver(target);
        try {
            driver.create(makeProperties(target, target.getParent(), jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @JmsListener(destination = TargetQueue.QUEUE_UPDATE)
    public void update(Target target, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Updating " + target.getClass().getSimpleName() + " " + target.getName());
        final Driver driver = getDriver(target);
        try {
            driver.update(makeProperties(target, target.getParent(), jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public Farm getFarmById(long id) {
        return getFarmRepository() != null ? getFarmRepository().findOne(id) : null;
    }

    @JmsListener(destination = TargetQueue.QUEUE_REMOVE)
    public void remove(Target target, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Removing "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = getDriver(target);

        try {
            driver.remove(makeProperties(target, target.getParent(), jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    protected FarmQueue farmQueue() {
        return (FarmQueue) getQueueLocator().getQueue(Farm.class);
    }

    public Properties makeProperties(Target target, Pool pool, final Map<String, String> jmsHeaders) {
        String json = "{}";
        try {
            final JsonMapper jsonMapper = new JsonMapper().makeJson(target);
            if (pool != null) {
                jsonMapper.putString("parentId", pool.getName());
            }
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        final Properties properties = fromEntity(target, jmsHeaders);
        properties.put(JSON_PROP, json);
        properties.put(PATH_PROP, Backend.class.getSimpleName().toLowerCase());
        return properties;
    }

    public FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Autowired
    public TargetEngine setFarmRepository(final FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
        return this;
    }

    public QueueLocator getQueueLocator() {
        return queueLocator;
    }

    @Autowired
    public TargetEngine setQueueLocator(final QueueLocator queueLocator) {
        this.queueLocator = queueLocator;
        return this;
    }

    public CounterDownLatch counterDownLatch() {
        return counterDownLatch;
    }

    @Autowired
    public void setCounterDownLatch(CounterDownLatch counterDownLatch) {
        this.counterDownLatch = counterDownLatch;
    }
}

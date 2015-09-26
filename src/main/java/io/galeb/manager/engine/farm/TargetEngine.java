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

package io.galeb.manager.engine.farm;

import io.galeb.core.model.Backend;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.jms.FarmQueue;
import io.galeb.manager.jms.TargetQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.entity.Target;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;
import io.galeb.manager.service.GenericEntityService;

@Component
public class TargetEngine extends AbstractEngine<Target> {

    private static final Log LOGGER = LogFactory.getLog(TargetEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private GenericEntityService genericEntityService;

    @Autowired
    private TargetQueue targetQueue;

    @Autowired
    private FarmQueue farmQueue;

    @JmsListener(destination = TargetQueue.QUEUE_CREATE)
    public void create(Target target) {
        LOGGER.info("Creating "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(target).get());
        createTarget(target, target.getParent(), driver);
    }

    private void createTarget(Target target, Pool pool, final Driver driver) {
        boolean isOk = false;
        try {
            isOk = driver.create(makeProperties(target, pool));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            target.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            targetQueue.sendToQueue(TargetQueue.QUEUE_CALLBK, target);
        }
    }

    @JmsListener(destination = TargetQueue.QUEUE_UPDATE)
    public void update(Target target) {
        LOGGER.info("Updating "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(target).get());
        updateTarget(target, target.getParent(), driver);
    }

    private void updateTarget(final Target target, final Pool pool, final Driver driver) {
        boolean isOk = false;
        try {
            isOk = driver.update(makeProperties(target, pool));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            target.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            targetQueue.sendToQueue(TargetQueue.QUEUE_CALLBK, target);
        }
    }

    @JmsListener(destination = TargetQueue.QUEUE_REMOVE)
    public void remove(Target target) {
        LOGGER.info("Removing "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(target).get());
        removeTarget(target, target.getParent(), driver);
    }

    private void removeTarget(final Target target, final Pool pool, final Driver driver) {
        boolean isOk = false;

        try {
            isOk = driver.remove(makeProperties(target, pool));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            target.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            targetQueue.sendToQueue(TargetQueue.QUEUE_CALLBK, target);
        }
    }

    @JmsListener(destination = TargetQueue.QUEUE_CALLBK)
    public void callBack(Target target) {
        if (genericEntityService.isNew(target)) {
            // target removed?
            return;
        }
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        target.setSaveOnly(true);
        targetRepository.save(target);
        setFarmStatusOnError(target);
        SystemUserService.runAs(currentUser);
        target.setSaveOnly(false);
    }

    @Override
    protected FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Override
    protected FarmQueue farmQueue() {
        return farmQueue;
    }

    private Properties makeProperties(Target target, Pool pool) {
        String json = "{}";
        try {
            final JsonMapper jsonMapper = new JsonMapper().makeJson(target);
            if (pool != null) {
                jsonMapper.putString("parentId", pool.getName());
            }
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        final Properties properties = fromEntity(target);
        properties.put("json", json);
        properties.put("path", Backend.class.getSimpleName().toLowerCase());
        return properties;
    }
}

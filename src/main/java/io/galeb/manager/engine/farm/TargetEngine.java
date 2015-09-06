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

import io.galeb.core.model.BackendPool;
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class TargetEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-target-create";
    public static final String QUEUE_UPDATE = "queue-target-update";
    public static final String QUEUE_REMOVE = "queue-target-remove";
    public static final String QUEUE_CALLBK = "queue-target-callback";

    private static final Log LOGGER = LogFactory.getLog(TargetEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    JmsTemplate jms;

    @Autowired
    private TargetRepository targetRepository;

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Target target) {
        LOGGER.info("Creating "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(target).get());
        boolean isOk = false;
        try {
            isOk = driver.create(makeProperties(target));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            target.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, target);
        }
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Target target) {
        LOGGER.info("Updating "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(target).get());
        boolean isOk = false;
        try {
            isOk = driver.update(makeProperties(target));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            target.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, target);
        }
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Target target) {
        LOGGER.info("Removing "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(target).get());
        boolean isOk = false;
        try {
            isOk = driver.remove(makeProperties(target));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            target.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, target);
        }
    }

    @JmsListener(destination = QUEUE_CALLBK)
    public void callBack(Target target) {
        if (targetRepository.findOne(target.getId()) == null) {
            // target removed?
            return;
        }
        target.setSaveOnly(true);
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
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
    protected JmsTemplate getJmsTemplate() {
        return jms;
    }

    private Properties makeProperties(Target target) {
        String json = "{}";
        try {
            if (target.getBalancePolicy() != null) {
                target.getProperties().put(BackendPool.PROP_LOADBALANCE_POLICY, target.getBalancePolicy().getBalancePolicyType().getName());
                target.getProperties().putAll(target.getBalancePolicy().getProperties());
            }
            final JsonMapper jsonMapper = new JsonMapper().makeJson(target);
            if (target.getParent() != null) {
                jsonMapper.putString("parentId", target.getParent().getName());
            }
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.equals(e.getMessage());
        }
        final Properties properties = fromEntity(target);
        properties.put("json", json);
        properties.put("path", target.getTargetType().getName().toLowerCase());
        return properties;
    }
}

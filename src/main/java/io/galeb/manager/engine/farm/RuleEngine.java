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

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
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
public class RuleEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-rule-create";
    public static final String QUEUE_UPDATE = "queue-rule-update";
    public static final String QUEUE_REMOVE = "queue-rule-remove";
    public static final String QUEUE_CALLBK = "queue-rule-callback";

    private static final Log LOGGER = LogFactory.getLog(RuleEngine.class);

    @Autowired
    FarmRepository farmRepository;

    @Autowired
    JmsTemplate jms;

    @Autowired
    private RuleRepository ruleRepository;

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Rule rule) {
        LOGGER.info("Creating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        boolean isOk = false;
        try {
            isOk = driver.create(makeProperties(rule));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            rule.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, rule);
        }
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Rule rule) {
        LOGGER.info("Updating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        boolean isOk = false;
        try {
            isOk = driver.update(makeProperties(rule));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            rule.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, rule);
        }
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Rule rule) {
        LOGGER.info("Removing "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        boolean isOk = false;
        try {
            isOk = driver.remove(makeProperties(rule));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            rule.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, rule);
        }
    }

    @JmsListener(destination = QUEUE_CALLBK)
    public void callBack(Rule rule) {
        rule.setSaveOnly(true);
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        ruleRepository.save(rule);
        setFarmStatusOnError(rule);
        SystemUserService.runAs(currentUser);
        rule.setSaveOnly(false);
    }

    @Override
    protected FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Override
    protected JmsTemplate getJmsTemplate() {
        return jms;
    }

    private Properties makeProperties(Rule rule) {
        String json = "{}";
        try {
            final JsonMapper jsonMapper = new JsonMapper().makeJson(rule);
            jsonMapper.putString("parentId", rule.getParent().getName());
            jsonMapper.addToNode("properties", "ruleType", rule.getRuleType().getName());
            jsonMapper.addToNode("properties", "targetType", rule.getTarget().getTargetType().getName());
            jsonMapper.addToNode("properties", "targetId", rule.getTarget().getName());
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.equals(e.getMessage());
        }
        final Properties properties = fromEntity(rule);
        properties.put("json", json);
        properties.put("path", "rule");
        return properties;
    }
}

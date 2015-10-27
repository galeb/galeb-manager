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

import io.galeb.core.model.BackendPool;
import io.galeb.manager.engine.util.VirtualHostAliasBuilder;
import io.galeb.manager.entity.*;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.RuleQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.engine.listeners.services.GenericEntityService;

import java.util.*;

@Component
public class RuleEngine extends AbstractEngine<Rule> {

    private static final Log LOGGER = LogFactory.getLog(RuleEngine.class);

    @Autowired private FarmRepository farmRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private RuleQueue ruleQueue;
    @Autowired private FarmQueue farmQueue;
    @Autowired private GenericEntityService genericEntityService;
    @Autowired private VirtualHostAliasBuilder virtualHostAliasBuilder;

    @JmsListener(destination = RuleQueue.QUEUE_CREATE)
    public void create(Rule rule) {
        LOGGER.info("Creating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        rule.getParents().stream().forEach(virtualhost -> {
            updateRuleSpecialProperties(rule, virtualhost);
            boolean isOk = false;
            try {
                isOk = driver.create(makeProperties(rule, virtualhost));
                virtualhost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias =  virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualhost);
                    driver.create(makeProperties(rule, virtualHostAlias));
                });
            } catch (Exception e) {
                LOGGER.error(e);
            } finally {
                rule.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
                ruleQueue.sendToQueue(RuleQueue.QUEUE_CALLBK, rule);
            }
        });
    }

    @JmsListener(destination = RuleQueue.QUEUE_UPDATE)
    public void update(Rule rule) {
        LOGGER.info("Updating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        rule.getParents().stream().forEach(virtualhost -> {
            updateRuleSpecialProperties(rule, virtualhost);
            boolean isOk = false;

            try {
                if (!driver.exist(makeProperties(rule, virtualhost))) {
                    ruleQueue.sendToQueue(RuleQueue.QUEUE_CREATE, rule);
                    return;
                }
                isOk = driver.update(makeProperties(rule, virtualhost));
                virtualhost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias =  virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualhost);
                    driver.update(makeProperties(rule, virtualHostAlias));
                });
            } catch (Exception e) {
                LOGGER.error(e);
            } finally {
                rule.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
                ruleQueue.sendToQueue(RuleQueue.QUEUE_CALLBK, rule);
            }
        });
    }

    @JmsListener(destination = RuleQueue.QUEUE_REMOVE)
    public void remove(Rule rule) {
        LOGGER.info("Removing " + rule.getClass().getSimpleName() + " " + rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        rule.getParents().stream().forEach(virtualhost -> {
            boolean isOk = false;

            try {
                isOk = driver.remove(makeProperties(rule, virtualhost));
            } catch (Exception e) {
                LOGGER.error(e);
            } finally {
                rule.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
                ruleQueue.sendToQueue(RuleQueue.QUEUE_CALLBK, rule);
            }
        });
    }

    @JmsListener(destination = RuleQueue.QUEUE_CALLBK)
    public void callBack(Rule rule) {
        if (genericEntityService.isNew(rule)) {
            // rule removed?
            return;
        }
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        try {
            ruleRepository.save(rule);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            setFarmStatusOnError(rule);
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

    private void updateRuleSpecialProperties(final Rule rule, final VirtualHost virtualhost) {
        Optional<Integer> ruleOrder = virtualhost.getRulesOrdered().stream()
                .filter(r -> r.getRuleId() == rule.getId()).map(RuleOrder::getRuleOrder).findAny();
        rule.setRuleOrder(ruleOrder.orElse(Integer.MAX_VALUE));
        Rule ruleDefault = virtualhost.getRuleDefault();
        if (ruleDefault != null) {
            rule.setRuleDefault(ruleDefault.getId() == rule.getId());
        }
    }

    private Properties makeProperties(Rule rule, VirtualHost virtualHost) {
        String json = "{}";
        try {
            final JsonMapper jsonMapper = new JsonMapper().makeJson(rule);
            jsonMapper.putString("parentId", virtualHost.getName());
            jsonMapper.addToNode("properties", "ruleType", rule.getRuleType().getName());
            jsonMapper.addToNode("properties", "targetType", BackendPool.class.getSimpleName());
            jsonMapper.addToNode("properties", "targetId", rule.getPool().getName());
            jsonMapper.addToNode("properties", "orderNum", String.valueOf(rule.getRuleOrder()));
            jsonMapper.addToNode("properties", "default", String.valueOf(rule.isRuleDefault()));
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        final Properties properties = fromEntity(rule);
        properties.put("json", json);
        properties.put("path", "rule");
        return properties;
    }
}

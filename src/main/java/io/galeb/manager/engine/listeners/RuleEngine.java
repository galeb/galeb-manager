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
import io.galeb.manager.engine.listeners.services.QueueLocator;
import io.galeb.manager.engine.util.VirtualHostAliasBuilder;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.RuleOrder;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.queue.AbstractEnqueuer;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.RuleQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.repository.FarmRepository;

import java.util.Map;
import java.util.Optional;

@Component
public class RuleEngine extends AbstractEngine<Rule> {

    private static final Log LOGGER = LogFactory.getLog(RuleEngine.class);

    @Override
    protected Log getLogger() {
        return LOGGER;
    }

    @Autowired private FarmRepository farmRepository;
    @Autowired private QueueLocator queueLocator;
    @Autowired private VirtualHostAliasBuilder virtualHostAliasBuilder;

    @JmsListener(destination = RuleQueue.QUEUE_CREATE)
    public void create(Rule rule, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Creating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        rule.getParents().stream().forEach(virtualhost -> {
            updateRuleSpecialProperties(rule, virtualhost);
            try {
                driver.create(makeProperties(rule, virtualhost, jmsHeaders));
                virtualhost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias = virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualhost);
                    driver.create(makeProperties(rule, virtualHostAlias, jmsHeaders));
                });
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    @JmsListener(destination = RuleQueue.QUEUE_UPDATE)
    public void update(Rule rule, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Updating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        rule.getParents().stream().forEach(virtualhost -> {
            updateRuleSpecialProperties(rule, virtualhost);
            try {
                if (!driver.exist(makeProperties(rule, virtualhost, jmsHeaders))) {
                    ruleQueue().sendToQueue(RuleQueue.QUEUE_CREATE, rule);
                    return;
                }
                driver.update(makeProperties(rule, virtualhost, jmsHeaders));
                virtualhost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias = virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualhost);
                    driver.update(makeProperties(rule, virtualHostAlias, jmsHeaders));
                });
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    @JmsListener(destination = RuleQueue.QUEUE_REMOVE)
    public void remove(Rule rule, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Removing " + rule.getClass().getSimpleName() + " " + rule.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(rule).get());
        rule.getParents().stream().forEach(virtualhost -> {
            try {
                driver.remove(makeProperties(rule, virtualhost, jmsHeaders));
                virtualhost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias = virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualhost);
                    driver.remove(makeProperties(rule, virtualHostAlias, jmsHeaders));
                });
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    @Override
    protected FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Override
    protected FarmQueue farmQueue() {
        return (FarmQueue)queueLocator.getQueue(Farm.class);
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

    public Properties makeProperties(Rule rule, VirtualHost virtualHost, final Map<String, String> jmsHeaders) {
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
        final Properties properties = fromEntity(rule, jmsHeaders);
        properties.put("json", json);
        properties.put("path", "rule");
        return properties;
    }

    private AbstractEnqueuer<Rule> ruleQueue() {
        return (RuleQueue)queueLocator.getQueue(Rule.class);
    }

}

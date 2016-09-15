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

import io.galeb.manager.engine.listeners.services.QueueLocator;
import io.galeb.manager.engine.util.VirtualHostAliasBuilder;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.RuleOrder;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.queue.AbstractEnqueuer;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.RuleQueue;
import io.galeb.manager.queue.VirtualHostQueue;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VirtualHostEngine extends AbstractEngine<VirtualHost> {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostEngine.class);

    @Override
    protected Log getLogger() {
        return LOGGER;
    }

    @Autowired private FarmRepository farmRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private QueueLocator queueLocator;
    @Autowired private VirtualHostAliasBuilder virtualHostAliasBuilder;

    @JmsListener(destination = VirtualHostQueue.QUEUE_CREATE)
    public void create(VirtualHost virtualHost, @Headers final Map<String, String> jmsHeaders) {

        LOGGER.info("Creating " + virtualHost.getClass().getSimpleName() + " " + virtualHost.getName());
        final Driver driver = getDriver(virtualHost);
        try {
            driver.create(makeProperties(virtualHost, jmsHeaders));
            virtualHost.getAliases().forEach(virtualHostName -> {
                VirtualHost virtualHostAlias = virtualHostAliasBuilder
                        .buildVirtualHostAlias(virtualHostName, virtualHost);
                create(virtualHostAlias, jmsHeaders);
            });
            createRules(virtualHost, jmsHeaders);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @JmsListener(destination = VirtualHostQueue.QUEUE_UPDATE)
    public void update(VirtualHost virtualHost, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Updating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        final Driver driver = getDriver(virtualHost);
        try {
            driver.update(makeProperties(virtualHost, jmsHeaders));
            virtualHost.getAliases().forEach(virtualHostName -> {
                VirtualHost virtualHostAlias = virtualHostAliasBuilder
                        .buildVirtualHostAlias(virtualHostName, virtualHost);
                if (!driver.exist(makeProperties(virtualHostAlias, jmsHeaders))) {
                    create(virtualHostAlias, jmsHeaders);
                } else {
                    update(virtualHostAlias, jmsHeaders);
                }
            });
            updateRules(virtualHost, jmsHeaders);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @JmsListener(destination = VirtualHostQueue.QUEUE_REMOVE)
    public void remove(VirtualHost virtualHost, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Removing " + virtualHost.getClass().getSimpleName() + " " + virtualHost.getName());
        final Driver driver = getDriver(virtualHost);
        try {
            driver.remove(makeProperties(virtualHost, jmsHeaders));
            virtualHost.getAliases().forEach(virtualHostName -> {
                VirtualHost virtualHostAlias = virtualHostAliasBuilder
                        .buildVirtualHostAlias(virtualHostName, virtualHost);
                remove(virtualHostAlias, jmsHeaders);
            });
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    protected FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Override
    protected FarmQueue farmQueue() {
        return (FarmQueue)queueLocator.getQueue(Farm.class);
    }

    public Properties makeProperties(VirtualHost virtualHost, final Map<String, String> jmsHeaderProperties) {
        String json = "{}";
        try {
            JsonMapper jsonMapper = new JsonMapper().makeJson(virtualHost);
            json = jsonMapper.toString();
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        final Properties properties = fromEntity(virtualHost, jmsHeaderProperties);
        properties.put("json", json);
        properties.put("path", "virtualhost");
        return properties;
    }

    private void updateRules(VirtualHost virtualHost, Map<String, String> jmsHeaders) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Set<Long> ruleOrderedIds = virtualHost.getRulesOrdered().stream()
                .map(RuleOrder::getRuleId)
                .collect(Collectors.toSet());
        final Set<Rule> rulesNotOrdered = ruleRepository.findAll().stream()
                .filter(r -> r.getParents().contains(virtualHost) && !ruleOrderedIds.contains(r.getId()))
                .collect(Collectors.toSet());
        ruleOrderedIds.stream()
                .map(id -> ruleRepository.findOne(id))
                .filter(Objects::nonNull)
                .forEach(rule -> ruleQueue().sendToQueue(RuleQueue.QUEUE_UPDATE, rule, jmsHeaders));
        rulesNotOrdered.forEach(rule -> ruleQueue().sendToQueue(RuleQueue.QUEUE_UPDATE, rule, jmsHeaders));
        SystemUserService.runAs(currentUser);
    }

    private void createRules(VirtualHost virtualHost, Map<String, String> jmsHeaders) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Set<Long> ruleOrderedIds = virtualHost.getRulesOrdered().stream()
                .map(RuleOrder::getRuleId)
                .collect(Collectors.toSet());
        final Set<Rule> rulesNotOrdered = ruleRepository.findAll().stream()
                .filter(r -> r.getParents().contains(virtualHost) && !ruleOrderedIds.contains(r.getId()))
                .collect(Collectors.toSet());
        ruleOrderedIds.stream()
                .map(id -> ruleRepository.findOne(id))
                .filter(Objects::nonNull)
                .forEach(rule -> ruleQueue().sendToQueue(RuleQueue.QUEUE_CREATE, rule, jmsHeaders));
        rulesNotOrdered.forEach(rule -> ruleQueue().sendToQueue(RuleQueue.QUEUE_UPDATE, rule, jmsHeaders));
        SystemUserService.runAs(currentUser);
    }

    private AbstractEnqueuer<Rule> ruleQueue() {
        return (RuleQueue)queueLocator.getQueue(Rule.class);
    }


    private Driver getDriver(VirtualHost virtualHost) {
        if (driver == null) {
            driver = DriverBuilder.getDriver(findFarm(virtualHost).get());
        }
        return driver;
    }

    public VirtualHostEngine setVirtualHostAliasBuilder(VirtualHostAliasBuilder aVirtualHostAliasBuilder) {
        virtualHostAliasBuilder = aVirtualHostAliasBuilder;
        return this;
    }

}

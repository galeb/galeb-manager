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
import io.galeb.manager.repository.VirtualHostRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class VirtualHostEngine extends AbstractEngine<VirtualHost> {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostEngine.class);

    @Override
    protected Log getLogger() {
        return LOGGER;
    }

    private FarmRepository farmRepository;
    private RuleRepository ruleRepository;
    private VirtualHostRepository virtualHostRepository;
    private QueueLocator queueLocator;
    private VirtualHostAliasBuilder virtualHostAliasBuilder;

    @JmsListener(destination = VirtualHostQueue.QUEUE_CREATE)
    public void create(VirtualHost virtualHost, @Headers final Map<String, String> jmsHeaders) {

        LOGGER.info("Creating " + virtualHost.getClass().getSimpleName() + " " + virtualHost.getName());
        final Driver driver = getDriver(virtualHost);
        try {
            driver.create(makeProperties(virtualHost, jmsHeaders));
            virtualHost.getAliases().forEach(virtualHostName -> {
                VirtualHost virtualHostAlias = getVirtualHostAliasBuilder()
                        .buildVirtualHostAlias(virtualHostName, virtualHost);
                create(virtualHostAlias, jmsHeaders);
            });
            createRules(virtualHost, jmsHeaders);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @JmsListener(destination = VirtualHostQueue.QUEUE_UPDATE)
    public void update(VirtualHost virtualHost, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Updating " + virtualHost.getClass().getSimpleName() + " " + virtualHost.getName());
        final Driver driver = getDriver(virtualHost);
        try {
            driver.update(makeProperties(virtualHost, jmsHeaders));
            virtualHost.getAliases().forEach(virtualHostName -> {
                VirtualHost virtualHostAlias = getVirtualHostAliasBuilder()
                        .buildVirtualHostAlias(virtualHostName, virtualHost);
                if (!driver.exist(makeProperties(virtualHostAlias, jmsHeaders))) {
                    create(virtualHostAlias, jmsHeaders);
                } else {
                    update(virtualHostAlias, jmsHeaders);
                }
            });
            updateRules(virtualHost, jmsHeaders);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public Farm getFarmById(long id) {
        return getFarmRepository() != null ? getFarmRepository().findOne(id) : null;
    }

    @JmsListener(destination = VirtualHostQueue.QUEUE_REMOVE)
    public void remove(VirtualHost virtualHost, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Removing " + virtualHost.getClass().getSimpleName() + " " + virtualHost.getName());
        final Driver driver = getDriver(virtualHost);
        try {
            driver.remove(makeProperties(virtualHost, jmsHeaders));
            virtualHost.getAliases().forEach(virtualHostName -> {
                VirtualHost virtualHostAlias = getVirtualHostAliasBuilder()
                        .buildVirtualHostAlias(virtualHostName, virtualHost);
                remove(virtualHostAlias, jmsHeaders);
            });
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    protected FarmQueue farmQueue() {
        return (FarmQueue) getQueueLocator().getQueue(Farm.class);
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
        properties.put(JSON_PROP, json);
        properties.put(PATH_PROP, "virtualhost");
        return properties;
    }

    private void updateRules(VirtualHost virtualHost, Map<String, String> jmsHeaders) {
        String ruleQueue = RuleQueue.QUEUE_UPDATE;
        sendRuleToQueue(virtualHost, jmsHeaders, ruleQueue);
    }

    private void createRules(VirtualHost virtualHost, Map<String, String> jmsHeaders) {
        String ruleQueue = RuleQueue.QUEUE_CREATE;
        sendRuleToQueue(virtualHost, jmsHeaders, ruleQueue);
    }

    private AbstractEnqueuer<Rule> ruleQueue() {
        return (RuleQueue) getQueueLocator().getQueue(Rule.class);
    }

    private void sendRuleToQueue(VirtualHost virtualHost, final Map<String, String> jmsHeaders, String ruleQueue) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Map<String, String> newJmsHeaders = new HashMap<>(jmsHeaders);
        newJmsHeaders.remove(PARENTID_PROP);
        final Set<Long> ruleOrderedIds = virtualHost.getRulesOrdered().stream()
                .map(RuleOrder::getRuleId)
                .collect(Collectors.toSet());
        final Set<Rule> rulesNotOrdered = getVirtualHostRepository() != null ? getVirtualHostRepository().getRulesFromVirtualHostName(virtualHost.getName()).stream()
                .filter(r -> !ruleOrderedIds.contains(r.getId()))
                .collect(Collectors.toSet()) : Collections.emptySet();
        ruleOrderedIds.stream()
                .map(id -> getRuleRepository() != null ? getRuleRepository().findOne(id) : null)
                .filter(Objects::nonNull)
                .forEach(rule -> ruleQueue().sendToQueue(ruleQueue, rule, newJmsHeaders));
        rulesNotOrdered.forEach(rule -> ruleQueue().sendToQueue(ruleQueue, rule, newJmsHeaders));
        SystemUserService.runAs(currentUser);
    }

    public FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Autowired
    public VirtualHostEngine setFarmRepository(final FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
        return this;
    }

    public RuleRepository getRuleRepository() {
        return ruleRepository;
    }

    @Autowired
    public VirtualHostEngine setRuleRepository(final RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
        return this;
    }

    public VirtualHostRepository getVirtualHostRepository() {
        return virtualHostRepository;
    }

    @Autowired
    public VirtualHostEngine setVirtualHostRepository(final VirtualHostRepository virtualHostRepository) {
        this.virtualHostRepository = virtualHostRepository;
        return this;
    }

    public QueueLocator getQueueLocator() {
        return queueLocator;
    }

    @Autowired
    public VirtualHostEngine setQueueLocator(final QueueLocator queueLocator) {
        this.queueLocator = queueLocator;
        return this;
    }

    public VirtualHostAliasBuilder getVirtualHostAliasBuilder() {
        return virtualHostAliasBuilder;
    }

    @Autowired
    public VirtualHostEngine setVirtualHostAliasBuilder(final VirtualHostAliasBuilder virtualHostAliasBuilder) {
        this.virtualHostAliasBuilder = virtualHostAliasBuilder;
        return this;
    }
}

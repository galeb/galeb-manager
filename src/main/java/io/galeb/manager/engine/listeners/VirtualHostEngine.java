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

import io.galeb.core.jcache.CacheFactory;
import io.galeb.core.jcache.IgniteCacheFactory;
import io.galeb.manager.engine.util.VirtualHostAliasBuilder;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.RuleQueue;
import io.galeb.manager.queue.VirtualHostQueue;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.engine.listeners.services.GenericEntityService;

@Component
public class VirtualHostEngine extends AbstractEngine<VirtualHost> {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostEngine.class);

    @Autowired private FarmRepository farmRepository;
    @Autowired private VirtualHostRepository virtualHostRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private GenericEntityService genericEntityService;
    @Autowired private VirtualHostQueue virtualHostQueue;
    @Autowired private RuleQueue ruleQueue;
    @Autowired private FarmQueue farmQueue;
    @Autowired private VirtualHostAliasBuilder virtualHostAliasBuilder;

    @JmsListener(destination = VirtualHostQueue.QUEUE_CREATE)
    public void create(VirtualHost virtualHost) {
        LOGGER.info("Creating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(virtualHost).get());
        boolean isOk = false;
        try {
            isOk = driver.create(makeProperties(virtualHost));
            if (isOk) {
                virtualHost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias = virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualHost);
                    create(virtualHostAlias);
                });
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            releaseLocks(virtualHost, "");
            if (virtualHost.getStatus() != EntityStatus.DISABLED) {
                virtualHost.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
                virtualHostQueue.sendToQueue(VirtualHostQueue.QUEUE_CALLBK, virtualHost);
            }
        }
    }

    @JmsListener(destination = VirtualHostQueue.QUEUE_UPDATE)
    public void update(VirtualHost virtualHost) {
        LOGGER.info("Updating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(virtualHost).get());
        boolean isOk = false;
        try {
            isOk = driver.update(makeProperties(virtualHost));
            if (isOk) {
                virtualHost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias = virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualHost);
                    if (!driver.exist(makeProperties(virtualHostAlias))) {
                        create(virtualHostAlias);
                        createRules(virtualHostAlias);
                    } else {
                        update(virtualHostAlias);
                    }
                });
                updateRules(virtualHost);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            releaseLocks(virtualHost, "");
            if (virtualHost.getStatus() != EntityStatus.DISABLED) {
                virtualHost.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
                virtualHostQueue.sendToQueue(VirtualHostQueue.QUEUE_CALLBK, virtualHost);
            }
        }
    }

    @JmsListener(destination = VirtualHostQueue.QUEUE_REMOVE)
    public void remove(VirtualHost virtualHost) {
        LOGGER.info("Removing " + virtualHost.getClass().getSimpleName() + " " + virtualHost.getName());
        final Driver driver = DriverBuilder.getDriver(findFarm(virtualHost).get());
        boolean isOk = false;
        try {
            isOk = driver.remove(makeProperties(virtualHost));
            if (isOk) {
                virtualHost.getAliases().forEach(virtualHostName -> {
                    VirtualHost virtualHostAlias = virtualHostAliasBuilder
                            .buildVirtualHostAlias(virtualHostName, virtualHost);
                    remove(virtualHostAlias);
                });
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            releaseLocks(virtualHost, "");
            virtualHost.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            virtualHostQueue.sendToQueue(VirtualHostQueue.QUEUE_CALLBK, virtualHost);
        }
    }

    @JmsListener(destination = VirtualHostQueue.QUEUE_CALLBK)
    public void callBack(VirtualHost virtualHost) {
        if (genericEntityService.isNew(virtualHost)) {
            // virtualHost removed?
            return;
        }
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        try {
            virtualHostRepository.save(virtualHost);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage());
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

    private Properties makeProperties(VirtualHost virtualHost) {
        String json = "{}";
        try {
            JsonMapper jsonMapper = new JsonMapper().makeJson(virtualHost);
            json = jsonMapper.toString();
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        Properties properties = fromEntity(virtualHost);
        properties.put("json", json);
        properties.put("path", "virtualhost");
        return properties;
    }

    private void updateRules(VirtualHost virtualHost) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        virtualHost.getRulesOrdered().stream()
                .map(ruleOrder -> ruleRepository.findOne(ruleOrder.getRuleId()))
                .filter(rule -> rule != null)
                .forEach(rule -> ruleQueue.sendToQueue(RuleQueue.QUEUE_UPDATE, rule));
        SystemUserService.runAs(currentUser);
    }

    private void createRules(VirtualHost virtualHost) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        virtualHost.getRulesOrdered().stream()
                .map(ruleOrder -> ruleRepository.findOne(ruleOrder.getRuleId()))
                .filter(rule -> rule != null)
                .forEach(rule -> ruleQueue.sendToQueue(RuleQueue.QUEUE_CREATE, rule));
        SystemUserService.runAs(currentUser);
    }

}

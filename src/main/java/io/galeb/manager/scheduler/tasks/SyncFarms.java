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

package io.galeb.manager.scheduler.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.manager.common.Properties;
import io.galeb.manager.entity.*;
import io.galeb.manager.jms.FarmQueue;
import io.galeb.manager.redis.DistributedLocker;
import io.galeb.manager.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.jms.JmsConfiguration;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

import static java.util.AbstractMap.*;

@Component
public class SyncFarms {

    private static final Log    LOGGER        = LogFactory.getLog(SyncFarms.class);
    private static final String TASK_LOCKNAME = "SyncFarms.task";
    private static final long   INTERVAL      = 10000;

    @Autowired private FarmRepository        farmRepository;
    @Autowired private VirtualHostRepository virtualHostRepository;
    @Autowired private RuleRepository        ruleRepository;
    @Autowired private TargetRepository      targetRepository;
    @Autowired private PoolRepository        poolRepository;
    @Autowired private FarmQueue             farmQueue;
    @Autowired private DistributedLocker     distributedLocker;

    private final ObjectMapper mapper   = new ObjectMapper();
    private final Pageable     pageable = new PageRequest(0, 99999);

    private boolean disableJms = Boolean.getBoolean(System.getProperty(
                                    JmsConfiguration.DISABLE_JMS, Boolean.toString(false)));

    public boolean registerLock(String key, long ttl) {
        if (distributedLocker == null) {
            LOGGER.warn(DistributedLocker.class.getSimpleName() + " is NULL");
            return false;
        }
        if (!distributedLocker.getLock(key, ttl)) {
            LOGGER.warn(key + " is locked by other process. Aborting task");
            return false;
        }

        LOGGER.debug(key + " locked by me (" + this + ")");
        return true;
    }

    private Stream<Target> getTargets(Farm farm) {
        return StreamSupport.stream(
                targetRepository.findByFarmId(farm.getId(), pageable).spliterator(), false);
    }

    private Stream<Rule> getRules(Farm farm) {
        return StreamSupport.stream(
                ruleRepository.findByFarmId(farm.getId(), pageable).spliterator(), false)
                .filter(rule -> !rule.getParents().isEmpty());
    }

    private Stream<VirtualHost> getVirtualhosts(Farm farm) {
        return StreamSupport.stream(
                virtualHostRepository.findByFarmId(farm.getId(), pageable).spliterator(), false);
    }

    private Stream<Pool> getPools(Farm farm) {
        return StreamSupport.stream(
                poolRepository.findByFarmId(farm.getId(), pageable).spliterator(), false);
    }

    @Scheduled(fixedRate = INTERVAL)
    private void task() {

        if (!registerLock(TASK_LOCKNAME, INTERVAL / 1000)) {
            return;
        }

        try {
            Authentication currentUser = CurrentUser.getCurrentAuth();
            SystemUserService.runAs();

            StreamSupport.stream(farmRepository.findAll().spliterator(), false)
                    .filter(farm -> !farm.getStatus().equals(EntityStatus.DISABLED))
                    .forEach(farm ->
                    {
                        try {
                            syncFarm(farm);
                        } catch (Exception e) {
                            LOGGER.error(e);
                        }
                    });

            SystemUserService.runAs(currentUser);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            distributedLocker.release(TASK_LOCKNAME);
            LOGGER.debug("TASK checkFarm finished");
        }
    }

    private void syncFarm(Farm farm) throws JsonProcessingException {
        final Driver driver = DriverBuilder.getDriver(farm);
        final Properties properties = getPropertiesWithEntities(farm);

        Map<String, Map<String, String>> diff = driver.diff(properties);
        if (diff.isEmpty()) {
            LOGGER.info("FARM STATUS OK: " + farm.getName() + " [" + farm.getApi() + "]");
            farm.setStatus(EntityStatus.OK);
            farmQueue.sendToQueue(FarmQueue.QUEUE_CALLBK, farm);

        } else {
            String json = mapper.writeValueAsString(diff);
            LOGGER.warn("FARM " + farm.getName() + " INCONSISTENT: \n" + json);
            farm.setStatus(EntityStatus.ERROR);
            farmQueue.sendToQueue(FarmQueue.QUEUE_CALLBK, farm);

            if (farm.isAutoReload() && !disableJms) {
                Entry<Farm, Map<String, Object>> entrySet = new SimpleImmutableEntry(farm, diff);
                farmQueue.sendToQueue(FarmQueue.QUEUE_SYNC, entrySet);
            } else {
                LOGGER.warn("FARM STATUS FAIL (But AutoSync is disabled): " + farm.getName() + " [" + farm.getApi() + "]");
            }
        }
    }

    private Properties getPropertiesWithEntities(Farm farm) {
        final Map<String, Set<AbstractEntity<?>>> entitiesMap = getEntitiesMap(farm);
        final Properties properties = new Properties();
        properties.put("api", farm.getApi());
        properties.put("entitiesMap", entitiesMap);
        return properties;
    }

    private Map<String, Set<AbstractEntity<?>>> getEntitiesMap(Farm farm) {
        final Map<String, Set<AbstractEntity<?>>> entitiesMap = new HashMap<>();
        entitiesMap.put(VirtualHost.class.getSimpleName().toLowerCase(), getVirtualhosts(farm)
                .collect(Collectors.toSet()));
        entitiesMap.put(BackendPool.class.getSimpleName().toLowerCase(), getPools(farm)
                .collect(Collectors.toSet()));
        entitiesMap.put(Backend.class.getSimpleName().toLowerCase(), getTargets(farm)
                .collect(Collectors.toSet()));
        entitiesMap.put(Rule.class.getSimpleName(), getRules(farm)
                .collect(Collectors.toSet()));
        return entitiesMap;
    }

}

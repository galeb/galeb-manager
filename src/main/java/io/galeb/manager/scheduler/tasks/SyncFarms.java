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
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.manager.common.Properties;
import io.galeb.manager.entity.*;
import io.galeb.manager.queue.*;
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

import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

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
    private final Pageable     pageable = new PageRequest(0, Integer.MAX_VALUE);

    private boolean disableQueue = Boolean.getBoolean(System.getProperty(
                                    JmsConfiguration.DISABLE_QUEUE, Boolean.toString(false)));


    @Scheduled(fixedRate = INTERVAL)
    private void task() {

        if (!distributedLocker.lock(TASK_LOCKNAME, INTERVAL / 1000)) {
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

            if (farm.isAutoReload() && !disableQueue) {
                Entry<Farm, Map<String, Object>> entrySet = new SimpleImmutableEntry(farm, diff);
                farmQueue.sendToQueue(FarmQueue.QUEUE_SYNC, entrySet);
            } else {
                LOGGER.warn("FARM STATUS FAIL (But AutoSync is disabled): " + farm.getName() + " [" + farm.getApi() + "]");
            }
        }
    }

    private Properties getPropertiesWithEntities(Farm farm) {
        final Map<String, List<?>> entitiesMap = getEntitiesMap(farm);
        final Properties properties = new Properties();
        properties.put("api", farm.getApi());
        properties.put("entitiesMap", entitiesMap);
        return properties;
    }

    private Map<String, List<?>> getEntitiesMap(Farm farm) {
        final Map<String, List<?>> entitiesMap = new HashMap<>();
        entitiesMap.put(VirtualHost.class.getSimpleName().toLowerCase(),
                virtualHostRepository.findByFarmId(farm.getId(), pageable).getContent());
        entitiesMap.put(BackendPool.class.getSimpleName().toLowerCase(),
                poolRepository.findByFarmId(farm.getId(), pageable).getContent());
        entitiesMap.put(Backend.class.getSimpleName().toLowerCase(),
                targetRepository.findByFarmId(farm.getId(), pageable).getContent());
        entitiesMap.put(Rule.class.getSimpleName().toLowerCase(),
                ruleRepository.findByFarmId(farm.getId(), pageable).getContent());
        return entitiesMap;
    }

}

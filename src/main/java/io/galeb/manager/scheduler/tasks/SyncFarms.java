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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.core.jcache.CacheFactory;
import io.galeb.core.jcache.IgniteCacheFactory;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.manager.common.Properties;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.JmsConfiguration;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.PoolRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.scheduler.SchedulerConfiguration;
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

import static java.lang.System.getenv;
import static java.lang.System.getProperty;
import static java.lang.System.currentTimeMillis;
import static java.util.AbstractMap.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;

@Component
public class SyncFarms {

    private static final Log    LOGGER        = LogFactory.getLog(SyncFarms.class);
    private static final String TASK_LOCKNAME = "SyncFarms.task";
    private static final long   INTERVAL      = 10000; // msec

    public static final int    LOCK_TTL      = 120; // seconds

    @Autowired private FarmRepository        farmRepository;
    @Autowired private VirtualHostRepository virtualHostRepository;
    @Autowired private RuleRepository        ruleRepository;
    @Autowired private TargetRepository      targetRepository;
    @Autowired private PoolRepository        poolRepository;
    @Autowired private FarmQueue             farmQueue;

    CacheFactory cacheFactory = IgniteCacheFactory.INSTANCE;

    private final ObjectMapper mapper   = new ObjectMapper();
    private final Pageable     pageable = new PageRequest(0, Integer.MAX_VALUE);

    private boolean disableQueue = Boolean.getBoolean(
            getProperty(JmsConfiguration.DISABLE_QUEUE,
                    Boolean.toString(false)));

    private boolean disableSched = Boolean.getBoolean(
            getProperty(SchedulerConfiguration.GALEB_DISABLE_SCHED,
                    getenv(SchedulerConfiguration.GALEB_DISABLE_SCHED)));

    @Scheduled(fixedRate = INTERVAL)
    private void task() {
        if (disableSched) {
            LOGGER.debug(SyncFarms.class.getSimpleName() + " aborted (GALEB_DISABLE_SCHED is TRUE)");
            return;
        }
        if (!cacheFactory.lock(TASK_LOCKNAME)) {
            LOGGER.warn("syncFarm LOCKED (" + TASK_LOCKNAME + "). Waiting for release lock");
            return;
        }

        try {
            Authentication currentUser = CurrentUser.getCurrentAuth();
            SystemUserService.runAs();

            farmRepository.findAll().stream()
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
            cacheFactory.release(TASK_LOCKNAME);
            LOGGER.info("Lock " + TASK_LOCKNAME + " released");
            LOGGER.debug("TASK checkFarm finished");
        }
    }

    private void syncFarm(Farm farm) throws JsonProcessingException {
        String farmLock = farm.getName() + ".lock";
        if (!cacheFactory.lock(farmLock)) {
            LOGGER.warn("syncFarm LOCKED (" + farm.getName() + "). Waiting for release lock");
            return;
        }

        final Driver driver = DriverBuilder.getDriver(farm);
        final Properties properties = getPropertiesWithEntities(farm);

        long diffStart = currentTimeMillis();
        LOGGER.info("FARM STATUS - Getting diff from " + farm.getName() + " [" + farm.getApi() + "]");
        Map<String, Map<String, Object>> diff = driver.diff(properties);
        LOGGER.info("FARM STATUS - diff from " + farm.getName() + " [" + farm.getApi() + "] finished ("
                + (currentTimeMillis() - diffStart) + " ms)");

        if (diff.isEmpty()) {
            cacheFactory.release(farmLock);
            LOGGER.info("Lock " + farmLock + " released");
            LOGGER.info("FARM STATUS OK: " + farm.getName() + " [" + farm.getApi() + "]");
            farm.setStatus(EntityStatus.OK);
            farmQueue.sendToQueue(FarmQueue.QUEUE_CALLBK, farm);

        } else {
            //String json = mapper.writeValueAsString(diff);
            LOGGER.warn("FARM " + farm.getName() + " INCONSISTENT: " + diff.size() + " fix(es)");
            farm.setStatus(EntityStatus.ERROR);
            farmQueue.sendToQueue(FarmQueue.QUEUE_CALLBK, farm);

            if (farm.isAutoReload() && !disableQueue) {
                @SuppressWarnings("unchecked")
                Entry<Farm, Map<String, Object>> entrySet = new SimpleImmutableEntry(farm, diff);
                farmQueue.sendToQueue(FarmQueue.QUEUE_SYNC, entrySet);
            } else {
                cacheFactory.release(farmLock);
                LOGGER.info("Lock " + farmLock + " released");
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

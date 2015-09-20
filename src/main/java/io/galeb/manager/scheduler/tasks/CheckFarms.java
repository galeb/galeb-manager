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

import static io.galeb.manager.scheduler.SchedulerConfiguration.GALEB_DISABLE_SCHED;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.galeb.manager.redis.DistributedLocker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.galeb.manager.common.EmptyStream;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.Driver.StatusFarm;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.engine.farm.FarmEngine;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.jms.JmsConfiguration;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

@Component
public class CheckFarms {

    private static final Log LOGGER = LogFactory.getLog(CheckFarms.class);

    private static final String CHECK_FARMS_RUN_LOCKNAME = "CheckFarms.run";

    private static final String CHECK_FARMS_DIFF_LOCKNAME = "CheckFarms.newReload";

    private static final long INTERVAL = 10000;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private VirtualHostRepository virtualHostRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private DistributedLocker distributedLocker;

    private final ObjectMapper mapper = new ObjectMapper();

    private boolean disableJms = Boolean.getBoolean(System.getProperty(
                                    JmsConfiguration.DISABLE_JMS, Boolean.toString(false)));

    private Properties getProperties(final Farm farm,
                                     final AbstractEntity<?> entity,
                                     String path,
                                     long numElements) {
        return getProperties(farm,
                             entity,
                             path,
                             numElements,
                             EmptyStream.get());
    }

    private Properties getProperties(final Farm farm,
                                     final AbstractEntity<?> entity,
                                     String path,
                                     long numElements,
                                     final Stream<? extends AbstractEntity<?>> parents) {
        Properties properties = new Properties();
        properties.put("api", farm.getApi());
        properties.put("name", entity.getName());
        properties.put("path", path);
        properties.put("id", entity.getId());
        properties.put("numElements", numElements);
        properties.put("parents", parents);
        return properties;
    }

    public boolean getLock(String key, long ttl) {
        if (distributedLocker == null) {
            LOGGER.warn(DistributedLocker.class.getSimpleName() + " is NULL");
            return false;
        } else {
            if (!distributedLocker.getLock(key, ttl)) {
                LOGGER.warn(key + " is locked by other process. Aborting task");
                return false;
            }
        }
        LOGGER.debug(key + " locked by me (" + this + ")");
        return true;
    }

    @Scheduled(fixedRate = INTERVAL)
    private void run() {

        String disableSchedulerStr = System.getenv(GALEB_DISABLE_SCHED);
        if (disableSchedulerStr != null && Boolean.parseBoolean(disableSchedulerStr)) {
            LOGGER.warn(GALEB_DISABLE_SCHED + " is true");
            return;
        }

        if (!getLock(CHECK_FARMS_RUN_LOCKNAME, INTERVAL/1000)) {
            return;
        }

        LOGGER.debug("TASK checkFarm executing");

        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        try {
            StreamSupport.stream(farmRepository.findAll().spliterator(), false)
                                    .filter(farm -> !farm.getStatus().equals(EntityStatus.DISABLED))
                                    .forEach(farm -> {

                final Driver driver = DriverBuilder.getDriver(farm);
                AtomicBoolean isOk = new AtomicBoolean(true);

                if (!farm.getStatus().equals(EntityStatus.ERROR)) {

                   final long virtualhostCount = getVirtualhosts(farm).count();

                   getVirtualhosts(farm).forEach(virtualhost -> {
                        Properties properties = getProperties(farm,
                                                              virtualhost,
                                                              "virtualhost",
                                                              virtualhostCount);
                        boolean lastStatus = isOk.get();
                        isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                    });

                    final long ruleCount = getRules(farm).count();

                    getRules(farm).forEach(rule -> {
                        Stream<VirtualHost> virtualhosts =  StreamSupport.stream(rule.getParents().spliterator(), false);
                        Properties properties = getProperties(farm,
                                                              rule,
                                                              "rule",
                                                              ruleCount,
                                                              virtualhosts);
                        boolean lastStatus = isOk.get();
                        isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                    });

                    Map<String, Long> targetsCountMap = getTargets(farm).parallel().collect(
                                    Collectors.groupingBy(target -> target.getTargetType().getName(),
                                                          Collectors.counting()));

                    getTargets(farm).forEach(target -> {
                        String targetTypeName = target.getTargetType().getName();
                        Properties properties = getProperties(farm,
                                                              target,
                                                              target.getTargetType().getName().toLowerCase(),
                                                              targetsCountMap.get(targetTypeName));
                        boolean lastStatus = isOk.get();
                        isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                    });

                } else {
                    isOk.set(false);
                }

                farm.setStatus(isOk.get() ? EntityStatus.OK : EntityStatus.ERROR);
                if (!isOk.get()) {
                    if (farm.isAutoReload() && !disableJms) {
                        jms.convertAndSend(FarmEngine.QUEUE_RELOAD, farm);
                    } else {
                        LOGGER.warn("FARM STATUS FAIL (But AutoReload disabled): " + farm.getName() + " [" + farm.getApi() + "]");
                    }
                } else {
                    LOGGER.info("FARM STATUS OK: " + farm.getName() + " [" + farm.getApi() + "]");
                }
            });
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            distributedLocker.release(CHECK_FARMS_RUN_LOCKNAME);
            SystemUserService.runAs(currentUser);
            LOGGER.debug("TASK checkFarm finished");
        }

    }

    private Stream<Target> getTargets(Farm farm) {
        return StreamSupport.stream(
                targetRepository.findByFarmId(farm.getId()).spliterator(), false);
    }

    private Stream<Rule> getRules(Farm farm) {
        return StreamSupport.stream(
                ruleRepository.findByFarmId(farm.getId()).spliterator(), false)
                .filter(rule -> !rule.getParents().isEmpty());
    }

    private Stream<VirtualHost> getVirtualhosts(Farm farm) {
        return StreamSupport.stream(
                virtualHostRepository.findByFarmId(farm.getId()).spliterator(), false);
    }

    @Scheduled(fixedRate = INTERVAL)
    private void diff() {

        if (!getLock(CHECK_FARMS_DIFF_LOCKNAME, INTERVAL/1000)) {
            return;
        }

        try {
            Authentication currentUser = CurrentUser.getCurrentAuth();
            SystemUserService.runAs();

            StreamSupport.stream(farmRepository.findAll().spliterator(), false)
                    .filter(farm -> !farm.getStatus().equals(EntityStatus.DISABLED))
                    .forEach(farm ->
                    {
                        final Driver driver = DriverBuilder.getDriver(farm);
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("api", farm.getApi());
                        properties.put("virtualhosts", getVirtualhosts(farm).collect(Collectors.toSet()));
                        properties.put("backendpools", getTargets(farm)
                                .filter(target -> target.getTargetType().getName().equals("BackendPool"))
                                .collect(Collectors.toSet()));
                        properties.put("backends", getTargets(farm)
                                .filter(target -> target.getTargetType().getName().equals("Backend"))
                                .collect(Collectors.toSet()));
                        properties.put("rules", getRules(farm).collect(Collectors.toSet()));

                        try {
                            String json = mapper.writeValueAsString(driver.diff(properties));
                            LOGGER.warn("----------------------");
                            LOGGER.warn(json);
                            LOGGER.warn("----------------------");
                        } catch (Exception e) {
                            LOGGER.error(e);
                            e.printStackTrace();
                        }

                    });

            SystemUserService.runAs(currentUser);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            distributedLocker.release(CHECK_FARMS_DIFF_LOCKNAME);
        }
    }

}

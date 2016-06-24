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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.core.cluster.ClusterLocker;
import io.galeb.core.cluster.ignite.IgniteClusterLocker;
import io.galeb.manager.engine.util.CounterDownLatch;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.JmsConfiguration;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.PoolRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.scheduler.SchedulerConfiguration;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.getenv;
import static java.lang.System.getProperty;
import static java.lang.System.currentTimeMillis;

@Component
public class SyncFarms {

    private static final Log   LOGGER         = LogFactory.getLog(SyncFarms.class);
    private static final long  SCHED_INTERVAL = 5000; //sec

    @Autowired private FarmRepository        farmRepository;
    @Autowired private VirtualHostRepository virtualHostRepository;
    @Autowired private RuleRepository        ruleRepository;
    @Autowired private TargetRepository      targetRepository;
    @Autowired private PoolRepository        poolRepository;
    @Autowired private FarmQueue             farmQueue;

    private ClusterLocker locker = IgniteClusterLocker.getInstance().start();

    private boolean disableQueue = Boolean.valueOf(
            getProperty(JmsConfiguration.DISABLE_QUEUE,
                    Boolean.toString(false)));

    private boolean disableSched = Boolean.valueOf(
            getProperty(SchedulerConfiguration.GALEB_DISABLE_SCHED,
                    getenv(SchedulerConfiguration.GALEB_DISABLE_SCHED)));

    @Scheduled(fixedRate = SCHED_INTERVAL)
    private void task() {
        long start = currentTimeMillis();
        LOGGER.debug("Executing " + this.getClass().getSimpleName() + ".task");
        if (disableSched) {
            LOGGER.warn(SyncFarms.class.getSimpleName() + " aborted (GALEB_DISABLE_SCHED is TRUE)");
            return;
        }

        try {
            Authentication currentUser = CurrentUser.getCurrentAuth();
            SystemUserService.runAs();

            farmRepository.findAll().stream()
                    .filter(farm -> !farm.getStatus().equals(EntityStatus.DISABLED))
                    .parallel()
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
            LOGGER.debug("Finished " + this.getClass().getSimpleName() + ".task (" + (currentTimeMillis() - start) + " ms)");
        }
    }

    private void syncFarm(Farm farm) throws JsonProcessingException {
        String farmStatusMsgPrefix = "FARM STATUS - ";

        String[] apis = farm.getApi().split(",");
        final AtomicBoolean needRelease = new AtomicBoolean(true);
        final AtomicBoolean countDownZeroed = new AtomicBoolean(true);
        Arrays.stream(apis).forEach(api -> {
            final Integer latchCount = CounterDownLatch.refreshAndGet(api);
            countDownZeroed.set(countDownZeroed.get() && (latchCount != null && latchCount == 0));
            needRelease.set(needRelease.get() && CounterDownLatch.refreshAndCheckContainsKey(api));
        });

        if (countDownZeroed.get()) {
            if (needRelease.get()) {
                releaseLock(farm.idName(), apis);
                LOGGER.info(farmStatusMsgPrefix + "Releasing lock: Farm " + farm.getName());
            } else {
                Arrays.stream(apis).forEach(api -> {
                    final Integer latchCount = CounterDownLatch.refreshAndGet(api);
                    String farmFull = farm.getName() + " [ " + api + " ] ";
                    LOGGER.warn(farmStatusMsgPrefix + "Still synchronizing Farm " + farmFull + " (remains " + latchCount + " tasks)");
                });
            }
        } else {
            if (farm.isAutoReload() && !disableQueue) {
                farmQueue.sendToQueue(FarmQueue.QUEUE_SYNC, farm);
            } else {
                LOGGER.warn(farmStatusMsgPrefix + "Check & Sync DISABLED (QUEUE_SYNC or Auto Reload is FALSE): " + farm.getName());
            }
        }
    }

    private void releaseLock(String lockId, final String[] apis) {
        locker.release(lockId);
        Arrays.stream(apis).forEach(CounterDownLatch::remove);
    }
}

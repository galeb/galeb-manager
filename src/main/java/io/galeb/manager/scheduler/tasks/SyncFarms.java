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
import io.galeb.manager.common.CommandCountDown;
import io.galeb.manager.common.StatusDistributed;
import io.galeb.manager.engine.service.LockerManager;
import io.galeb.manager.engine.util.CounterDownLatch;
import io.galeb.manager.entity.Farm;

import io.galeb.manager.entity.LockStatus;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.JmsConfiguration;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.scheduler.SchedulerConfiguration;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.getenv;
import static java.lang.System.getProperty;
import static java.lang.System.currentTimeMillis;

@Component
public class SyncFarms {

    private static final Log   LOGGER         = LogFactory.getLog(SyncFarms.class);
    private static final long  SCHED_INTERVAL = 5000; //sec

    private final FarmRepository        farmRepository;
    private final FarmQueue             farmQueue;
    private final CounterDownLatch      counterDownLatch;
    private final LockerManager lockerManager;
    private final StatusDistributed statusDist;

    private boolean disableQueue = Boolean.valueOf(
            getProperty(JmsConfiguration.DISABLE_QUEUE,
                    Boolean.toString(false)));

    private boolean disableSched = Boolean.valueOf(
            getProperty(SchedulerConfiguration.GALEB_DISABLE_SCHED,
                    getenv(SchedulerConfiguration.GALEB_DISABLE_SCHED)));

    private static Map<String, String[]> apisToSync = new ConcurrentHashMap<>();
    private static Map<String, Long> counterWithoutCommandCounterDown = new ConcurrentHashMap<>();
    private static int tryiesTimeoutSyncFarm = 0;
    private static int timeToAvoidReleaseToTest = 0;

    @Autowired
    public SyncFarms(FarmRepository farmRepository,
                     FarmQueue farmQueue,
                     CounterDownLatch counterDownLatch,
                     StatusDistributed statusDist) {
        this.farmRepository = farmRepository;
        this.farmQueue = farmQueue;
        this.counterDownLatch = counterDownLatch;
        this.lockerManager = new LockerManager().setCounterDownLatch(counterDownLatch);
        this.statusDist = statusDist;
    }

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
                    .parallel()
                    .forEach(farm ->
                    {
                        try {
                            syncFarm(farm);
                        } catch (Exception e) {
                            LOGGER.error(ExceptionUtils.getStackTrace(e));
                        }
                    });

            SystemUserService.runAs(currentUser);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            LOGGER.debug("Finished " + this.getClass().getSimpleName() + ".task (" + (currentTimeMillis() - start) + " ms)");
        }
    }

    private void syncFarm(Farm farm) throws JsonProcessingException {

        String farmStatusMsgPrefix = "FARM STATUS - " + farm.idName() + " - ";
        String[] apis = apisToSync.getOrDefault(farm.idName(), farm.getApi().split(","));

        if (checkFarmWithSlowOp(apis, farm.idName())) {
            LOGGER.error(farmStatusMsgPrefix + " POSSIBLE CLUSTER LOCK! RELEASING THE LOCK...");
            release(farm, apis);
            return;
        }

        Boolean containsLock = lockerManager.contains(farm.idName());
        if (containsLock != null && !containsLock) {
            LOGGER.info(farmStatusMsgPrefix + "Updated apis to sync because the lock is released for this instance.");
            apis = farm.getApi().split(",");
            apisToSync.put(farm.idName(), apis);
        }

        CommandCountDown comm = CommandCountDown.getCommandApplied(apis, counterDownLatch);
        switch (comm) {
            case SEND_TO_QUEUE:
                if (farm.isAutoReload() && !disableQueue) {
                    farmQueue.sendToQueue(FarmQueue.QUEUE_SYNC, farm, "ID:farm-" + farm.getId() + "-" + System.currentTimeMillis());
                } else {
                    LOGGER.warn(farmStatusMsgPrefix + "Check & Sync DISABLED (QUEUE_SYNC or Auto Reload is FALSE): " + farm.getName());
                }
                releaseCounterWithoutCommand(farm);
                break;
            case RELEASE:
                release(farm, apis);
                releaseCounterWithoutCommand(farm);
                break;
            case STILL_SYNCHRONIZING:
                releaseCounterWithoutCommand(farm);
                Arrays.stream(apis).forEach(api -> {
                    String farmFull = farm.getName() + " [ " + api + " ] ";
                    final Integer latchCount = counterDownLatch.get(api);
                    LOGGER.warn(farmStatusMsgPrefix + "Still synchronizing Farm " + farmFull + " (remains " + latchCount + " tasks)");
                });
                break;
            default:
                verifyCounterWithoutCommand(farm, apis);
        }
        updateStatusDistributed(farm, apis);
    }

    private void release(Farm farm, String[] apis) {
        lockerManager.release(farm.idName(), apis);
        statusDist.updateNewStatus(farm.idName(), false);
    }

    private boolean checkFarmWithSlowOp(String[] apis, String idFarm) {
        final AtomicBoolean applied = new AtomicBoolean(false);
        if (apis != null) {
            Arrays.stream(apis).forEach(api -> {
                applied.set(applied.get() || (checkLockFarmWithoutCounter(api, idFarm)));
            });
        }
        return applied.get();
    }

    private boolean checkLockFarmWithoutCounter(String api, String idFarm) {
        Optional<LockStatus> lock = statusDist.getLockStatusLocal(idFarm);
        boolean isLock = lock.isPresent() && lock.get().isHasLock();
        return !counterDownLatch.checkContainsKey(api) && isLock;
    }

    private void verifyCounterWithoutCommand(Farm farm, String[] apis) {
        Arrays.stream(apis).forEach(api -> {
            final Integer latchCount = counterDownLatch.get(api);
            String farmFull = farm.getName() + " [ " + api + " ] ";
            LOGGER.warn("Without command to execute. Skip the sync farm " + farmFull + " (remains " + latchCount + " tasks)");
        });
        Long count = counterWithoutCommandCounterDown.getOrDefault(farm.idName(), 0L);
        if (count == 6) {
            LOGGER.warn("Force releasing lock: Farm " + farm.getName() + " and removing CountDownLatch of " + Arrays.toString(ArrayUtils.toArray(apis)));
            lockerManager.release(farm.idName(), apis);
            releaseCounterWithoutCommand(farm);
        } else {
            ++count;
            counterWithoutCommandCounterDown.put(farm.idName(), count);
        }
    }

    private void releaseCounterWithoutCommand(Farm farm) {
        counterWithoutCommandCounterDown.remove(farm.idName());
    }

    private void updateStatusDistributed(Farm farm, String[] apis) {
        final Map<String, Integer> countDownLatchOfApis = new HashMap<>();
        Arrays.stream(apis).forEach(api -> {
            final Integer latchCount = counterDownLatch.get(api);
            if (latchCount != null) {
                countDownLatchOfApis.put(api,latchCount);
            }
        });
        statusDist.updateCountDownLatch(farm.idName(), countDownLatchOfApis);
    }
}

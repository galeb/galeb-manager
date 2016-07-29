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
import io.galeb.manager.engine.service.LockerManager;
import io.galeb.manager.engine.util.CounterDownLatch;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.JmsConfiguration;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.scheduler.SchedulerConfiguration;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.getenv;
import static java.lang.System.getProperty;
import static java.lang.System.currentTimeMillis;

@Component
public class SyncFarms {

    private static final Log   LOGGER         = LogFactory.getLog(SyncFarms.class);
    private static final long  SCHED_INTERVAL = 5000; //sec

    @Autowired private FarmRepository        farmRepository;
    @Autowired private FarmQueue             farmQueue;

    private final LockerManager lockerManager = new LockerManager();

    private boolean disableQueue = Boolean.valueOf(
            getProperty(JmsConfiguration.DISABLE_QUEUE,
                    Boolean.toString(false)));

    private boolean disableSched = Boolean.valueOf(
            getProperty(SchedulerConfiguration.GALEB_DISABLE_SCHED,
                    getenv(SchedulerConfiguration.GALEB_DISABLE_SCHED)));

    private static Map<String, String[]> apisToSync = new ConcurrentHashMap<>();
    private static Map<String, Long> counterWithoutCommandCounterDown = new ConcurrentHashMap<>();

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
        String farmStatusMsgPrefix = "FARM STATUS - " + farm.idName() + " - ";

        Boolean containsLock = lockerManager.contains(farm.idName());
        String[] apis = apisToSync.getOrDefault(farm.idName(), farm.getApi().split(","));
        if (containsLock != null && !containsLock) {
            LOGGER.info(farmStatusMsgPrefix + "Updated apis to sync because the lock is released for this instance.");
            apis = farm.getApi().split(",");
            apisToSync.put(farm.idName(), apis);
        }

        CommandCountDown comm = CommandCountDown.getCommandApplied(apis);
        switch (comm) {
            case SEND_TO_QUEUE:
                if (farm.isAutoReload() && !disableQueue) {
                    farmQueue.sendToQueue(FarmQueue.QUEUE_SYNC, farm);
                } else {
                    LOGGER.warn(farmStatusMsgPrefix + "Check & Sync DISABLED (QUEUE_SYNC or Auto Reload is FALSE): " + farm.getName());
                }
                releaseCounterWithoutCommand(farm);
                break;
            case RELEASE:
                lockerManager.release(farm.idName(), apis);
                releaseCounterWithoutCommand(farm);
                break;
            case STILL_SYNCHRONIZING:
                logStillSynchronizing(farm, farmStatusMsgPrefix, apis);
                releaseCounterWithoutCommand(farm);
                break;
            default:
                verifyCounterWithoutCommand(farm, apis);
        }
    }

    private void verifyCounterWithoutCommand(Farm farm, String[] apis) {
        Arrays.stream(apis).forEach(api -> {
            final Integer latchCount = CounterDownLatch.refreshAndGet(api);
            String farmFull = farm.getName() + " [ " + api + " ] ";
            LOGGER.warn("Without command to execute. Skip the sync farm " + farmFull + " (remains " + latchCount + " tasks)");
        });
        Long count = counterWithoutCommandCounterDown.getOrDefault(farm.idName(), Long.valueOf(0));
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

    private void logStillSynchronizing(Farm farm, String farmStatusMsgPrefix, String[] apis) {
        Arrays.stream(apis).forEach(api -> {
            final Integer latchCount = CounterDownLatch.refreshAndGet(api);
            String farmFull = farm.getName() + " [ " + api + " ] ";
            LOGGER.warn(farmStatusMsgPrefix + "Still synchronizing Farm " + farmFull + " (remains " + latchCount + " tasks)");
        });
    }
}

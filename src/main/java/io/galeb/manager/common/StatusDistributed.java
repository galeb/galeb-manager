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

package io.galeb.manager.common;

import io.galeb.core.json.JsonObject;
import io.galeb.manager.cache.DistMap;
import io.galeb.manager.engine.service.LockerManager;
import io.galeb.manager.entity.LockStatus;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import java.io.Serializable;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public final class StatusDistributed implements Serializable {

    private static final DistMap DIST_MAP = DistMap.getInstance();

    private static final LockerManager lockerManager = new LockerManager();

    public List<LockStatus> getLockStatus(String farmIdName) {
        String cacheName = LockStatus.class.getSimpleName() + farmIdName;
        List<LockStatus> values = new ArrayList<>();
        Cache<String, String> cacheValues = DIST_MAP.getAll(cacheName);
        StreamSupport.stream(cacheValues.spliterator(), true).forEach(entry -> {
            values.add((LockStatus) JsonObject.fromJson(entry.getValue(), LockStatus.class));
        });
        return values;
    }

    public Optional<LockStatus> getLockStatusLocal(String farmIdName) {
        List<LockStatus> allList = getLockStatus(farmIdName);
        return allList.stream().filter(lockStatus -> lockStatus.getName().contains(lockerManager.name()))
                               .findAny();
    }

    public void updateNewStatus(String farmIdName, boolean hasLock) {
        String cacheName = LockStatus.class.getSimpleName() + farmIdName;
        String value = JsonObject.toJsonString(LockStatus.create(lockerManager.name(), new Date(), hasLock));
        DIST_MAP.put(cacheName, lockerManager.name(), value);
    }

    public void updateCountDownLatch(String farmIdName, Map<String, Integer> countDownLatchOfApis) {
        String cacheName = LockStatus.class.getSimpleName() + farmIdName;
        String cacheValue = DIST_MAP.get(cacheName, lockerManager.name());
        if (cacheValue != null && !countDownLatchOfApis.isEmpty()) {
            LockStatus lockOfDistMap = (LockStatus) JsonObject.fromJson(cacheValue, LockStatus.class);
            lockOfDistMap.setCounterDownLatch(countDownLatchOfApis);
            DIST_MAP.put(cacheName, lockerManager.name(), JsonObject.toJsonString(lockOfDistMap));
        }
    }
}

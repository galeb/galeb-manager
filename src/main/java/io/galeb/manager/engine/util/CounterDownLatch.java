/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
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

package io.galeb.manager.engine.util;

import io.galeb.manager.scheduler.SchedulerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static java.lang.System.getProperty;

@Service
public class CounterDownLatch {

    private final long timeoutSyncFarm = Long.parseLong(getProperty(SchedulerConfiguration.GALEB_TIMEOUT_SYNC_FARM, String.valueOf(Integer.MAX_VALUE)));

    private final StringRedisTemplate template;

    @Autowired
    public CounterDownLatch(StringRedisTemplate template) {
        this.template = template;
    }

    public synchronized int decrementDiffCounter(String key) {
        int oldValue = -1;
        if (checkContainsKey(key)) {
            String value = template.opsForValue().get(key);
            if (value != null) {
                oldValue = Integer.parseInt(value);
                if (oldValue > 0) {
                    template.opsForValue().increment(key, -1L);
                } else {
                    put(key, 0);
                }
            }
        }
        return oldValue;
    }

    public synchronized void refresh(String key) {
        template.expire(key, timeoutSyncFarm, TimeUnit.MILLISECONDS);
    }

    public synchronized Integer get(String key) {
        String value = template.opsForValue().get(key);
        if (value != null) {
            return Integer.valueOf(value);
        }
        return null;
    }

    public synchronized void put(String key, Integer value) {
        template.opsForValue().set(key, String.valueOf(value), timeoutSyncFarm, TimeUnit.MILLISECONDS);
    }

    public synchronized void remove(String key) {
        template.delete(key);
    }

    public synchronized boolean checkContainsKey(String key) {
        return template.hasKey(key);
    }

    public synchronized int reset(String key) {
        put(key, 0);
        return 0;
    }
}

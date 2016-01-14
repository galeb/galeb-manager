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

package io.galeb.manager.redis;

import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.galeb.manager.scheduler.tasks.SyncFarms.LOCK_TTL;

@Component
@Scope("prototype")
public class DistributedLocker {

    public static final String LOCK_PREFIX = "lock_";

    public static final List<Class<? extends Entity>> FARM_ENTITIES_LIST =
            Arrays.asList(VirtualHost.class, Rule.class, BackendPool.class, Backend.class);

    private static final Map<String, Class<? extends Entity>> ENTITY_MAP = new HashMap<>();
    static {
        ENTITY_MAP.put(VirtualHost.class.getSimpleName().toLowerCase(), VirtualHost.class);
        ENTITY_MAP.put(BackendPool.class.getSimpleName().toLowerCase(), BackendPool.class);
        ENTITY_MAP.put(Rule.class.getSimpleName().toLowerCase(), Rule.class);
        ENTITY_MAP.put(Backend.class.getSimpleName().toLowerCase(), Backend.class);
    }

    private static final Log LOGGER = LogFactory.getLog(DistributedLocker.class);

    private JedisConnectionFactory jedisConnectionFactory = null;

    private JedisConnection redis = null;

    @PostConstruct
    public void init() {
        redis = jedisConnectionFactory.getConnection();
    }

    @Autowired
    private DistributedLocker(JedisConnectionFactory jedisConnectionFactory) {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }

    private synchronized boolean getLock(String key, long ttl) {
        try {
            if (redisSetNxPx(key, ttl)) {
                redis.setEx(key.getBytes(), ttl, "lock".getBytes());
                return redis.get(key.getBytes()) != null;
            }
        } catch (Exception e) {
            closeOnError();
            LOGGER.error(e);
        }
        return false;
    }

    public synchronized boolean containsLockWithPrefix(String prefix) {
        boolean hasOthers = false;
        try {
            Set<byte[]> keys = redis.keys((prefix + "*").getBytes());
            hasOthers = !keys.isEmpty();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return hasOthers;
    }

    public synchronized DistributedLocker release(String key) {
        try {
            LOGGER.info("Releasing lock " + key);
            redis.del(key.getBytes());
        } catch (Exception e) {
            closeOnError();
            LOGGER.error(e);
        }
        return this;
    }

    public boolean refresh(String key, long ttl) {
        boolean result = false;
        LOGGER.debug("Refreshing TTL (" + key + ").");
        try {
            result = redis.pExpire(key.getBytes(), ttl);
        } catch (Exception e) {
            closeOnError();
            LOGGER.error(e);
        }
        return result;
    }

    private Long remainTTL(String key) {
        Long remain = 0L;
        try {
            remain = redis.pTtl(key.getBytes());
        } catch (Exception e) {
            closeOnError();
            LOGGER.error(e);
        }
        return remain != null ? remain : 0L;
    }

    private boolean redisSetNxPx(String key, long ttl) {
        ByteArrayOutputStream out = null;
        try {
            Object resultObj = redis.execute(
                    "SET",
                    key.getBytes(),
                    "lock".getBytes(),
                    "NX".getBytes(),
                    "PX".getBytes(),
                    Long.valueOf(ttl).toString().getBytes());
            out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(resultObj);
            os.flush();
            os.close();
        } catch (Exception e) {
            closeOnError();
            LOGGER.error(e);
        }
        return out != null && out.size() > 5;
    }

    public synchronized boolean lock(String key, long ttl) {
        if (!getLock(key, ttl)) {
            LOGGER.warn("Locked by other process (" + key + "). Aborting task");
            return false;
        }

        LOGGER.debug(key + " locked by me (" + this + ")");
        return true;
    }

    public synchronized boolean lock(String key, String entityType, String id, long ttl) {
        Class<?> clazz = ENTITY_MAP.get(entityType);
        String lockName = key + "." + clazz.getSimpleName();
        return lock(lockName + "__" + id, ttl);
    }

    private void closeOnError() {
        try {
            redis.close();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void refreshAllLock(String lockName) {
        if ("UNDEF".equals(lockName)) {
            LOGGER.warn("lockName is " + lockName);
            return;
        }
        FARM_ENTITIES_LIST.forEach(clazz -> {
            refresh(lockName + "." +clazz.getSimpleName(), LOCK_TTL);
        });
    }

    public void releaseAllLocks(String lockName, Set<String> entityTypes) {
        FARM_ENTITIES_LIST.stream().forEach(clazz -> {
            if (entityTypes == null || !entityTypes.contains(clazz.getSimpleName().toLowerCase())) {
                release(lockName + "." + clazz.getSimpleName());
            }
        });
    }
}

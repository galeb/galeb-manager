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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

@Component
public class DistributedLocker {

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
            LOGGER.error(e);
        }
        return false;
    }

    public synchronized DistributedLocker release(String key) {
        try {
            LOGGER.debug("Releasing locker with key " + key);
            redis.del(key.getBytes());
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return this;
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
            LOGGER.error(e);
        }
        return out != null && out.size() > 5;
    }

    public synchronized boolean lock(String key, long ttl) {
        if (!getLock(key, ttl)) {
            LOGGER.warn("Locked by other process (" + key + ". Aborting task");
            return false;
        }

        LOGGER.debug(key + " locked by me (" + this + ")");
        return true;
    }

}

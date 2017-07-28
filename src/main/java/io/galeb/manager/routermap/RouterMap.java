/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2017 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.routermap;

import io.galeb.manager.common.ErrorLogger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RouterMap {

    private static final RouterMap INSTANCE = new RouterMap();

    private static final String ROUTER_PREFIX = "routers:";
    private static final int    REGISTER_TTL  = 30000; // ms
    public static final String KEY_HASH_ETAG = "etag";
    public static final String KEY_HASH_TIMESTAMP= "timeetag";
    public RouterState routerState;

    private StringRedisTemplate redisTemplate;

    public static RouterMap getInstance() {
        return INSTANCE;
    }

    private RouterMap() {
        //
    }

    public RouterMap setTemplate(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        return this;
    }

    public RouterMap setRouterState(final RouterState routerState) {
        this.routerState = routerState;
        return this;
    }

    public void put(String groupId, String localIp, String etag, String envname, String timestamp) {
        String keyPrefixEnv = ROUTER_PREFIX + envname;
        String keyFull = keyPrefixEnv + ":" + groupId + ":" + localIp;
        try {
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
            Map<String, String> mapValues = new HashMap<String, String>() {{
                put(KEY_HASH_ETAG, etag);
                put(KEY_HASH_TIMESTAMP, timestamp);
            }};
            redisTemplate.opsForHash().putAll(keyFull, mapValues);
            redisTemplate.expire(keyFull, REGISTER_TTL, TimeUnit.MILLISECONDS);
            routerState.updateRouterState(keyPrefixEnv, envname);
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
    }

    public Set<JsonSchema.Env> get() {
        return get(null);
    }

    public Set<JsonSchema.Env> get(String environmentName) {
        try {
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

            final Map<String, Set<JsonSchema.Router>> routerMap = new HashMap<>();
            final Set<JsonSchema.Env> envs = new HashSet<>();
            final Set<JsonSchema.GroupID> groupIDs = new HashSet<>();
            String key_envname = environmentName == null ? "*" : environmentName;
            redisTemplate.keys(ROUTER_PREFIX + key_envname + ":*").forEach(key -> {
                String etag = (String)redisTemplate.opsForHash().get(key, KEY_HASH_ETAG);
                long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                String envGroupIdWithLocalIp = key.replaceAll(ROUTER_PREFIX, "");
                String[] key_splited = envGroupIdWithLocalIp.split(":");
                String env = key_splited[0];
                String groupId = key_splited[1];
                String localIp = key_splited[2];

                Set<JsonSchema.Router> map = routerMap.computeIfAbsent(groupId, s -> new HashSet<>());
                map.add(new JsonSchema.Router(localIp, etag, expire));
                routerMap.put(groupId, map);

                groupIDs.add(new JsonSchema.GroupID(groupId, map));

                envs.add(new JsonSchema.Env(env, groupIDs));
            });
            return envs;
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
        return Collections.emptySet();
    }

}

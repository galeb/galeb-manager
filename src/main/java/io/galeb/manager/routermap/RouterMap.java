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

    public static final String ROUTER_PREFIX = "routers:";
    public static final long    REGISTER_TTL  = 30000; // ms
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

    public void put(String groupId, String localIp, String etag, String envname) {
        String keyPrefixEnv = ROUTER_PREFIX + envname;
        String keyFull = keyPrefixEnv + ":" + groupId + ":" + localIp;
        try {
            Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
            if (!redisTemplate.hasKey(keyFull)) {
                routerState.incrementVersion(envname);
            }
            redisTemplate.opsForValue().set(keyFull, etag, REGISTER_TTL, TimeUnit.MILLISECONDS);
            routerState.updateRouterState(envname);
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

            final Set<JsonSchema.Env> envs = new HashSet<>();
            String key_envname = environmentName == null ? "*" : environmentName;
            redisTemplate.keys(ROUTER_PREFIX + key_envname + ":*").forEach(key -> {
                String etag = redisTemplate.opsForValue().get(key);
                long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                String envGroupIdWithLocalIp = key.replaceAll(ROUTER_PREFIX, "");
                String[] keySplited = envGroupIdWithLocalIp.split(":");
                String env = keySplited[0];
                String groupId = keySplited[1];
                String localIp = keySplited[2];

                JsonSchema.Env envSchema = envs.stream()
                                                .filter(e -> e.getEnvName().equals(env))
                                                .findAny()
                                                .orElseGet(() -> new JsonSchema.Env(env, new HashSet<>()));
                JsonSchema.GroupID groupIDSchema = envSchema.getGroupIDs().stream()
                                                                            .filter(g -> g.getGroupID().equals(groupId))
                                                                            .findAny()
                                                                            .orElseGet(() -> new JsonSchema.GroupID(groupId, new HashSet<>()));
                groupIDSchema.getRouters().add(new JsonSchema.Router(localIp, etag, expire));
                envSchema.getGroupIDs().add(groupIDSchema);
                envs.add(envSchema);
            });
            return envs;
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
        return Collections.emptySet();
    }

}

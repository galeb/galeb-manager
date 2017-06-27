/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.routermap;

import com.google.gson.annotations.SerializedName;
import io.galeb.manager.common.ErrorLogger;
import io.galeb.manager.entity.AbstractEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class RouterMapConfiguration {

    @Bean
    public RouterMap routerMap(StringRedisTemplate redisTemplate) {
        return new RouterMap(redisTemplate);
    }

    public class RouterMap {

        private static final String ROUTER_PREFIX = "routers:";
        private static final String KEY_ROUTER_SYNC = "sync_etag:";
        private static final int    REGISTER_TTL  = 30000; // ms

        private final StringRedisTemplate redisTemplate;

        public RouterMap(final StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
            AbstractEntity.routerMap = this;
        }

        public void put(String groupId, String localIp, String etag, String envname) {
            String key_prefix_env = ROUTER_PREFIX + envname;
            String key_full = key_prefix_env + ":" + groupId + ":" + localIp;
            try {
                redisTemplate.opsForValue().set(key_full, etag, REGISTER_TTL, TimeUnit.MILLISECONDS);
                Set<String> keysGroupId = redisTemplate.keys(key_prefix_env + ":*");
                List<String> allValues = redisTemplate.opsForValue().multiGet(keysGroupId);
                boolean allEqual = allValues.stream().distinct().limit(2).count() <= 1;
                redisTemplate.opsForValue().set(KEY_ROUTER_SYNC + envname,String.valueOf(allEqual), REGISTER_TTL, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
            }
        }

        public Set<Env> get() {
            return get(null);
        }

        public Set<Env> get(String envname) {
            try {
                final Map<String, Set<Router>> routerMap = new HashMap<>();
                final Set<Env> envs = new HashSet<>();
                final Set<GroupID> groupIDs = new HashSet<>();
                String key_envname = envname == null ? "*" : envname;
                redisTemplate.keys(ROUTER_PREFIX + key_envname + ":*").forEach(key -> {
                    String etag = redisTemplate.opsForValue().get(key);
                    long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                    String envGroupIdWithLocalIp = key.replaceAll(ROUTER_PREFIX, "");
                    String[] key_splited = envGroupIdWithLocalIp.split(":");
                    String env = key_splited[0];
                    String groupId = key_splited[1];
                    String localIp = key_splited[2];

                    Set<Router> map = routerMap.computeIfAbsent(groupId, s -> new HashSet<>());
                    map.add(new Router(localIp, etag, expire));
                    routerMap.put(groupId, map);

                    groupIDs.add(new GroupID(groupId, map));

                    envs.add(new Env(env, groupIDs));
                });
                return envs;
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
            }
            return Collections.emptySet();
        }

        public Optional<Boolean> isAllRoutersSyncByEnv(String env) {
            String value = redisTemplate.opsForValue().get(KEY_ROUTER_SYNC + env);
            return Optional.ofNullable(value == null ? null : Boolean.valueOf(value));
        }

    }


    @SuppressWarnings({"unused", "WeakerAccess"})
    public class Env {
        private final String envName;
        private final Set<GroupID> groupIDs;

        public Env(String envName, Set<GroupID> groupIDs) {
            this.envName = envName;
            this.groupIDs = groupIDs;
        }

        @SerializedName("name")
        public String getEnvName() {
            return envName;
        }

        public Set<GroupID> getGroupIDs() {
            return groupIDs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Env env = (Env) o;
            return Objects.equals(envName, env.envName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(envName);
        }
    }

    public class GroupID {
        private final String groupID;
        private final Set<Router> routers;

        public GroupID(String groupID, Set<Router> routers) {
            this.routers = routers;
            this.groupID = groupID;
        }

        public Set<Router> getRouters() { return routers; }
        public String getGroupID() { return groupID; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            GroupID groupObj = (GroupID) o;
            return Objects.equals(getGroupID(), groupObj.getGroupID());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getGroupID());
        }
    }

    @SuppressWarnings("unused")
    public class Router {
        private final String localIp;
        private final String etag;
        private final long expire;

        public Router(String localIp, String etag, long expire) {
            this.localIp = localIp;
            this.etag = etag;
            this.expire = expire;
        }

        public String getLocalIp() {
            return localIp;
        }

        public String getEtag() {
            return etag;
        }

        public long getExpire() {
            return expire;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Router router = (Router) o;
            return Objects.equals(localIp, router.localIp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(localIp);
        }
    }
}

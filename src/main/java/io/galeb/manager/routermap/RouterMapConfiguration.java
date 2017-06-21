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
        private static final int    REGISTER_TTL  = 30000; // ms

        private final StringRedisTemplate redisTemplate;

        public RouterMap(final StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        public int put(String groupId, String localIp, String etag) {
            String key = ROUTER_PREFIX + groupId + ":" + localIp;
            try {
                redisTemplate.opsForValue().set(key, etag, REGISTER_TTL, TimeUnit.MILLISECONDS);
                return redisTemplate.keys(ROUTER_PREFIX + groupId + "*").size();
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
            }
            return 1;
        }

        public Set<Env> get() {
            try {
                final Map<String, Set<Router>> routerMap = new HashMap<>();
                final Set<Env> envs = new HashSet<>();
                redisTemplate.keys(ROUTER_PREFIX + "*").forEach(key -> {
                    String etag = redisTemplate.opsForValue().get(key);
                    long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
                    String groupIdWithLocalIp = key.replaceFirst(ROUTER_PREFIX, "");
                    int groupIdIndex = groupIdWithLocalIp.indexOf(":");
                    String groupId = groupIdWithLocalIp.substring(0, groupIdIndex);
                    String localIp = groupIdWithLocalIp.substring(groupIdIndex + 1, groupIdWithLocalIp.length());
                    routerMap.computeIfAbsent(groupId, s -> new HashSet<>()).add(new Router(localIp, etag, expire));
                });
                routerMap.forEach((env, routers) -> envs.add(new Env(env, routers)));
                return envs;
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
            }
            return Collections.emptySet();
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public class Env {
        private final String envName;
        private final Set<Router> routers;

        public Env(String envName, Set<Router> routers) {
            this.envName = envName;
            this.routers = routers;
        }

        @SerializedName("name")
        public String getEnvName() {
            return envName;
        }

        public Set<Router> getRouters() {
            return routers;
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

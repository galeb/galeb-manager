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

        public synchronized int put(String groupId, String localIp) {
            String key = ROUTER_PREFIX + groupId + ":" + localIp;
            try {
                redisTemplate.opsForValue().set(key, groupId, REGISTER_TTL, TimeUnit.MILLISECONDS);
                return redisTemplate.keys(ROUTER_PREFIX + groupId + "*").size();
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
            }
            return 1;
        }

        public synchronized Map<String, Set<String>> get() {
            try {
                final Map<String, Set<String>> routers = new HashMap<>();
                redisTemplate.keys(ROUTER_PREFIX + "*").forEach(key -> {
                    String groupIdWithLocalIp = key.replaceFirst(ROUTER_PREFIX, "");
                    int groupIdIndex = groupIdWithLocalIp.indexOf(":");
                    String groupId = groupIdWithLocalIp.substring(0, groupIdIndex);
                    String localIp = groupIdWithLocalIp.substring(groupIdIndex + 1, groupIdWithLocalIp.length());
                    routers.computeIfAbsent(groupId, s -> new HashSet<>()).add(localIp);
                });
                return routers;
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
            }
            return Collections.emptyMap();
        }
    }
}

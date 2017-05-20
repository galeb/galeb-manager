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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Configuration
public class RouterMapConfiguration {

    @Bean
    public RouterMap routerMap() {
        return new RouterMap();
    }

    public class RouterMap {

        public static final int REGISTER_TTL = 120000; // ms

        private final ConcurrentHashMap<String, Set<RegisterExpirable>> routers = new ConcurrentHashMap<>();

        public synchronized void put(String groupId, String localIp) {
            routers.computeIfAbsent(groupId, routers -> new HashSet<>()).add(new RegisterExpirable(localIp, System.currentTimeMillis()));
        }

        public Map<String, Set<String>> get() {
            return routers.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(RegisterExpirable::getLocalIp).collect(Collectors.toSet())));
        }

        public int count(String groupId) {
            return routers.computeIfAbsent(groupId, routers -> new HashSet<>()).size();
        }

        @Scheduled(fixedDelay = REGISTER_TTL)
        public void gc() {
            synchronized (routers) {
                for (Map.Entry<String, Set<RegisterExpirable>> e : routers.entrySet()) {
                    Set<RegisterExpirable> expiredList = e.getValue().stream()
                            .filter(r -> r.getTimestamp() < System.currentTimeMillis() - REGISTER_TTL)
                            .collect(Collectors.toSet());
                    routers.get(e.getKey()).removeAll(expiredList);
                    if (routers.get(e.getKey()).isEmpty()) {
                        routers.remove(e.getKey());
                    }
                }
            }
        }
    }

    private class RegisterExpirable {
        private final String localIp;
        private final long timestamp;

        RegisterExpirable(String localIp, long timestamp) {
            this.localIp = localIp;
            this.timestamp = timestamp;
        }

        String getLocalIp() {
            return localIp;
        }

        long getTimestamp() {
            return timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegisterExpirable that = (RegisterExpirable) o;
            return Objects.equals(localIp, that.localIp);
        }

        @Override
        public int hashCode() {
            return localIp.hashCode();
        }
    }

}

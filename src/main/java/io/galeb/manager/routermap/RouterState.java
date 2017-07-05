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

import io.galeb.manager.entity.service.EtagService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RouterState {

    public enum State {
        EMPTY,
        SYNC,
        NOSYNC
    }

    public static final RouterState INSTANCE = new RouterState();

    private static final String KEY_ROUTER_SYNC = "sync_etag:";
    private static final int    REGISTER_TTL    = 30000; // ms

    private ConcurrentHashMap<String, Integer> routerCount = new ConcurrentHashMap<>();

    private StringRedisTemplate redisTemplate;
    private EtagService etagService;

    public RouterState setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        return this;
    }

    public RouterState setEtagService(EtagService etagService) {
        this.etagService = etagService;
        return this;
    }

    public void updateRouterState(String keyPrefixEnv, String envname) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

        Set<String> keysGroupId = redisTemplate.keys(keyPrefixEnv + ":*");
        List<String> allValues = redisTemplate.opsForValue().multiGet(keysGroupId);
        routerCount.put(envname, Math.max(allValues.size(), 1));
        boolean allEqual = allValues.stream().distinct().limit(2).count() <= 1;
        redisTemplate.opsForValue().set(KEY_ROUTER_SYNC + envname,String.valueOf(allEqual), REGISTER_TTL, TimeUnit.MILLISECONDS);
    }

    public State state(String environmentName) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
        String etagFromRouter = "NEED GET ETAG FROM ROUTERMAP"; // TODO
        String etag = getEtag(environmentName);
        String stateStrFromRedis = redisTemplate.opsForValue().get(KEY_ROUTER_SYNC + environmentName);
        return stateStrFromRedis == null ? State.EMPTY : Boolean.valueOf(stateStrFromRedis) && etag.equals(etagFromRouter) ? State.SYNC : State.NOSYNC;
    }

    private String getEtag(String environmentName) {
        // TODO: NumRouters Problem. Get from Environment.properties.fullhash best approach??
        Integer numRouters = routerCount.get(environmentName);
        return etagService.etag(environmentName, numRouters, true);
    }

    public void releaseSync(String environmentName) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

        redisTemplate.opsForValue().set(KEY_ROUTER_SYNC + environmentName, Boolean.FALSE.toString(), REGISTER_TTL, TimeUnit.MILLISECONDS);
    }

}

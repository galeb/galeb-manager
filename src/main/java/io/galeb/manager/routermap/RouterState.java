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

import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.service.EtagService;
import io.galeb.manager.repository.EnvironmentRepository;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.galeb.manager.entity.AbstractEntitySyncronizable.PROP_FULLHASH;

public class RouterState {

    public enum State {
        EMPTY,
        SYNC,
        NOSYNC
    }

    public static final RouterState INSTANCE = new RouterState();

    private static final String KEY_ROUTER_SYNC = "sync_etag:";
    private static final int    REGISTER_TTL    = 30000; // ms

    private StringRedisTemplate redisTemplate;
    private EnvironmentRepository environmentRepository;

    public RouterState setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        return this;
    }

    public RouterState setEnviromentRepository(EnvironmentRepository enviromentRepository) {
        this.environmentRepository = enviromentRepository;
        return this;
    }

    public void updateRouterState(String keyPrefixEnv, String envname) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

        Set<String> keysGroupId = redisTemplate.keys(keyPrefixEnv + ":*");
        List<String> allValues = redisTemplate.opsForValue().multiGet(keysGroupId);
        boolean allEqual = allValues.stream().distinct().limit(2).count() <= 1;
        if (allEqual && allValues != null && allValues.size() > 0) {
            String etag = getEtagByEnvironment(envname);
            boolean allEqualAndEtagMatch = allValues.get(0).equals(etag);
            redisTemplate.opsForValue().set(KEY_ROUTER_SYNC + envname,String.valueOf(allEqualAndEtagMatch), REGISTER_TTL, TimeUnit.MILLISECONDS);
        }
    }

    private String getEtagByEnvironment(String envname) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Page<Environment> envPage = environmentRepository.findByName(envname, new PageRequest(0, 1));
        Environment environment = null;
        if (envPage.hasContent()) {
            environment = envPage.iterator().next();
        }
        String etag = environment == null ? "" :  environment.getProperties().getOrDefault(PROP_FULLHASH, "");
        SystemUserService.runAs(currentUser);
        return etag;

    }

    public State state(String environmentName) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
        String stateStrFromRedis = redisTemplate.opsForValue().get(KEY_ROUTER_SYNC + environmentName);
        return stateStrFromRedis == null ? State.EMPTY : Boolean.valueOf(stateStrFromRedis) ? State.SYNC : State.NOSYNC;
    }

    public void releaseSync(String environmentName) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");

        redisTemplate.opsForValue().set(KEY_ROUTER_SYNC + environmentName, Boolean.FALSE.toString(), REGISTER_TTL, TimeUnit.MILLISECONDS);
    }

}

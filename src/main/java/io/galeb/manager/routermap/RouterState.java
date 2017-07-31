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

import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.repository.EnvironmentRepository;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.galeb.manager.entity.AbstractEntitySyncronizable.PREFIX_HAS_CHANGE;
import static io.galeb.manager.entity.AbstractEntitySyncronizable.PROP_FULLHASH;

public class RouterState {

    public enum State {
        EMPTY,
        SYNC,
        NOSYNC
    }

    public static final RouterState INSTANCE = new RouterState();

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
        Set<Long> timestampsRouters = new HashSet<>();
        redisTemplate.keys(keyPrefixEnv + ":*").stream().forEach(key -> {
            timestampsRouters.add(Long.valueOf((String)redisTemplate.opsForHash().get(key, RouterMap.KEY_HASH_TIMESTAMP)));
        });
        redisTemplate.keys("sync:" + envname + ":*").forEach(key -> {
            redisTemplate.opsForSet().members(key).forEach(t -> {
                if (timestampsRouters.stream().filter(tr -> Long.valueOf(t) < tr).count() > 0) {
                    redisTemplate.opsForSet().remove(key, t);
                }
            });
        });
    }

    public boolean isEmpty(String env) {
        return redisTemplate.keys(RouterMap.ROUTER_PREFIX + env + ":*").isEmpty();
    }

    public State state(AbstractEntity entity) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
        if (isEmpty(entity.getEnvName())) return State.EMPTY;
        boolean hasChange;
        if (entity instanceof Farm) {
            hasChange = !changes(entity.getEnvName()).isEmpty();
        } else {
            hasChange = !changes(entity).isEmpty();
        }
        if (hasChange) {
            return State.NOSYNC;
        }
        if (existsKeysSync(entity)) {
            return State.NOSYNC;
        }
        return State.SYNC;
    }

    public void registerChanges(AbstractEntity entity) {
        String env = entity.getEnvName();
        String suffix = entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId() + ":" + entity.getLastModifiedAt().getTime();
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.setIfAbsent(PREFIX_HAS_CHANGE + ":" + env + ":" + suffix, env);
    }

    public Set<String> changes(String envname) {
        final Set<String> result = redisTemplate.keys(PREFIX_HAS_CHANGE + ":" + envname + ":*");
        return (result != null) ? result : Collections.emptySet();
    }

    public Set<String> changes(AbstractEntity entity) {
        final Set<String> result = redisTemplate.keys(PREFIX_HAS_CHANGE + ":" + entity.getEnvName() + ":" + entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId() + ":*");
        return (result != null) ? result : Collections.emptySet();
    }

    public boolean existsKeysSync(AbstractEntity entity) {
        String keySuffix = entity.getEnvName() + ":" +  entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId();
        boolean existsKey = redisTemplate.keys("sync:" + keySuffix)
                .stream().count() > 0;
        return existsKey;
    }

    public void addKeysSync(Set<String> changes) {
        changes.stream().forEach(change -> {
            String[] changeSplited = change.split(":");
            String envname = changeSplited[1];
            String objname = changeSplited[2];
            String objid = changeSplited[3];
            redisTemplate.opsForSet().add("sync:" + envname + ":" + objname + ":" + objid, String.valueOf(System.currentTimeMillis()));
        });
    }

}

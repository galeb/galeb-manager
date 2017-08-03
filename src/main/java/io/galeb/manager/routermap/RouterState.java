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
import io.galeb.manager.entity.Farm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

import static io.galeb.manager.entity.AbstractEntitySyncronizable.PREFIX_HAS_CHANGE;
import static io.galeb.manager.routermap.RouterMap.ROUTER_PREFIX;

public class RouterState {

    public enum State {
        EMPTY,
        SYNC,
        NOSYNC
    }

    public static final RouterState INSTANCE = new RouterState();

    private StringRedisTemplate redisTemplate;

    public RouterState setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        return this;
    }

    public void updateRouterState(String envname) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
        Set<Long> versionRouters = new HashSet<>();
        Set<String> eTagRouters = new HashSet<>();
        redisTemplate.keys(ROUTER_PREFIX + envname + ":*").stream().forEach(key -> {
            String[] value = redisTemplate.opsForValue().get(key).split(":");
            eTagRouters.add(value[0]);
            versionRouters.add(Long.valueOf(value[1]));
        });
        if (eTagRouters.isEmpty()) return;
        Long versionRouter = versionRouters.stream().mapToLong(i -> i).min().getAsLong();
        redisTemplate.keys(PREFIX_HAS_CHANGE + envname + ":*").forEach(key -> {
            String versionKey = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(versionKey) && versionRouter >= Long.valueOf(versionKey)) {
                redisTemplate.delete(key);
            }
        });
    }

    public boolean isEmpty(String env) {
        return redisTemplate.keys(ROUTER_PREFIX + env + ":*").isEmpty();
    }

    public State state(AbstractEntity entity) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
        if (isEmpty(entity.getEnvName())) return State.EMPTY;
        String key;
        if (entity instanceof Farm) {
            key = PREFIX_HAS_CHANGE + entity.getEnvName() + ":*";
        } else {
            String suffix = entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId();
            key = PREFIX_HAS_CHANGE + entity.getEnvName() + ":" + suffix + ":*";
        }
        if (hasChanges(key)) {
            return State.NOSYNC;
        }
        return State.SYNC;
    }

    public boolean hasChanges(String key) {
        final Set<String> result = redisTemplate.keys(key);
        return result != null && !result.isEmpty();
    }

    public <T extends AbstractEntity<?>> void registerChanges(T entity) {
        String env = entity.getEnvName();
        String suffix = entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId() + ":" + entity.getLastModifiedAt().getTime();
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.setIfAbsent(PREFIX_HAS_CHANGE + env + ":" + suffix, "");
    }


}

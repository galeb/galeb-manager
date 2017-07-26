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
import io.galeb.manager.repository.EnvironmentRepository;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    public State state(AbstractEntity entity) {
        Assert.notNull(redisTemplate, StringRedisTemplate.class.getSimpleName() + " IS NULL");
        String keySuffix = entity == null ? "*" : entity.getEnvName() + ":" +  entity.getClass().getSimpleName().toLowerCase().concat(String.valueOf(entity.getId()));
        boolean existsKey = redisTemplate.keys("sync:" + keySuffix)
                                         .stream().count() > 0;
        return existsKey ? State.NOSYNC : State.SYNC;
    }

}

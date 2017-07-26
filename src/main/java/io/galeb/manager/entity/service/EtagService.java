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

package io.galeb.manager.entity.service;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.manager.common.ErrorLogger;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.repository.EnvironmentRepository;
import io.galeb.manager.routermap.RouterMap;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.hash.Hashing.sha256;
import static io.galeb.manager.entity.AbstractEntitySyncronizable.*;

@Service
public class EtagService {

    private static final Log LOGGER = LogFactory.getLog(EtagService.class);

    private static final String CACHE_TTL = System.getProperty(EtagService.class.getName().toLowerCase(), "10"); // minutes

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").serializeNulls().create();
    private final EnvironmentRepository environmentRepository;
    private final StringRedisTemplate template;
    private final CopyService copyService;
    private final RouterMap routerMap;

    @Autowired
    public EtagService(final EnvironmentRepository environmentRepository,
                       final StringRedisTemplate template,
                       final CopyService copyService,
                       final RouterMap routerMap) {
        this.environmentRepository = environmentRepository;
        this.template = template;
        this.copyService = copyService;
        this.routerMap = routerMap;
    }

    public String responseBody(String envname, String groupId, String routerEtag) throws Exception {
        int numRouters = getNumRouters(envname, groupId);
        Set<String> changes = changes(envname);
        String etag;
        if (!changes.isEmpty()) {
            LOGGER.info("MUDANÇAS ACONTECERAM...");
            expireLastEtag(envname);
            expireChanges(changes);
            expireCache(envname);
            etag = etag(envname, numRouters, false);
        } else {
            LOGGER.info("SEM MUDANÇAS...");
            etag = etag(envname, numRouters, true);
        }
        if ("".equals(etag)) {
            return HttpStatus.NOT_FOUND.name();
        }
        if (routerEtag.equals(etag)) {
            return HttpStatus.NOT_MODIFIED.name();
        }
        String body = getBodyCached(etag, envname);
        if ("".equals(body)) {
            return HttpStatus.NOT_FOUND.name();
        }
        return body;
    }

    public void registerChanges(AbstractEntity entity) {
        String env = entity.getEnvName();
        String suffix = entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId() + ":" + entity.getLastModifiedAt().getTime();
        final ValueOperations<String, String> valueOperations = template.opsForValue();
        valueOperations.setIfAbsent(PREFIX_HAS_CHANGE + ":" + env + ":" + suffix, env);
        LOGGER.info("Registrando mudanças no Redis com chave " + PREFIX_HAS_CHANGE + ":" + env + ":" + suffix + " e valor " + env);
    }

    private String etag(String envname, int numRouters, boolean cache) {
        String etag = "";
        if (cache) etag = getLastEtag(envname);
        if ("".equals(etag)) {
            LOGGER.info("GERANDO NOVA TAG");
            etag = newEtag(envname, numRouters);
            LOGGER.info("NEW TAG -> " + etag);
        }
        return etag;
    }

    private Set<String> changes(String envname) {
        final Set<String> result = template.keys(PREFIX_HAS_CHANGE + ":" + envname + ":*");
        return (result != null) ? result : Collections.emptySet();
    }

    private void expireChanges(Set<String> changes) {
        changes.forEach(this::expire);
    }

    private void expire(String key) {
        if (template.hasKey(key)) {
//            template.expire(key, 100, TimeUnit.MILLISECONDS);
            template.delete(key);
        }
    }

    private void expireLastEtag(String envname) {
        template.keys(getLastEtagKey(envname, true)).forEach(this::expire);
    }

    private void expireCache(String envname) {
        template.keys(getEtagKey("*", envname)).forEach(this::expire);
    }

    private void persistToRedis(String etag, String envname, String body) {
        template.opsForValue().set(getLastEtagKey(envname, false), etag, Integer.parseInt(CACHE_TTL), TimeUnit.MINUTES);
        template.opsForValue().set(getEtagKey(etag, envname), body, Integer.parseInt(CACHE_TTL), TimeUnit.MINUTES);
    }

    private String getLastEtag(String envname) {
        Set<String> lastKey = template.keys(getLastEtagKey(envname, true));
        if (lastKey != null && lastKey.size() > 0) {
            String lastEtagKey = lastKey.stream().findAny().orElse("");
            if (!"".equals(lastEtagKey)) {
                return Optional.ofNullable(template.opsForValue().get(lastEtagKey)).orElse("");
            }
        }
        return "";
    }

    private String getLastEtagKey(String envname, boolean all) {
        return PREFIX_LAST_ETAG + ":" + envname + ":" + (all ? "*" : System.currentTimeMillis());
    }

    private String getBodyCached(String etag, String envname) {
        if (template.hasKey(getEtagKey(etag, envname))) {
            return Optional.ofNullable(template.opsForValue().get(getEtagKey(etag, envname))).orElse("");
        }
        return "";
    }

    private String getEtagKey(String etag, String envname) {
        return PREFIX_ETAG + ":" + envname + ":" + etag;
    }

    private String newEtag(String envname, int numRouters) {
        try {
            expireLastEtag(envname);
            expireCache(envname);

            List<VirtualHost> virtualHosts = copyService.getVirtualHosts(envname, numRouters);
            if (virtualHosts == null || virtualHosts.isEmpty()) {
                return "";
            }
            String key = virtualHosts.stream().map(this::getFullHash)
                    .sorted()
                    .distinct()
                    .collect(Collectors.joining());
            String etag = key == null || "".equals(key) ? "" : sha256().hashString(key, Charsets.UTF_8).toString();
            virtualHosts = virtualHosts.stream()
                    .map(v -> {
                        v.getEnvironment().getProperties().put(PROP_FULLHASH, etag);
                        return v;
                    })
                    .collect(Collectors.toList());
            persistToDb(envname, etag);
            persistToRedis(etag, envname, gson.toJson(new Virtualhosts(virtualHosts.toArray(new VirtualHost[]{})), Virtualhosts.class));
            return etag;
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
        return "";
    }

    @Transactional
    private void persistToDb(String envname, String etag) throws Exception {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        updateEtag(envFindByName(envname), etag);
        SystemUserService.runAs(currentUser);
    }

    public String getFullHash(VirtualHost v) {
        return Optional.ofNullable(v.getProperties().get(PROP_FULLHASH)).orElse("");
    }

    public void updateEtag(final Environment environment, String etag) throws Exception {
        environment.getProperties().put(PROP_FULLHASH, etag);
        try {
            environmentRepository.saveAndFlush(environment);
            LOGGER.warn("Environment " + environment.getName() + ": updated fullhash to " + etag);
        } catch (Exception e) {
            LOGGER.error("Environment " + environment.getName() + ": FAIL to update fullhash to " + etag);
            throw e;
        } finally {
            environmentRepository.flush();
        }
    }

    public Environment envFindByName(String envname) {
        final Page<Environment> envPage = environmentRepository.findByName(envname, new PageRequest(0, 1));
        Environment environment = null;
        if (envPage.hasContent()) {
            environment = envPage.iterator().next();
        }
        return environment;
    }

    private class Virtualhosts implements Serializable {
        private static final long serialVersionUID = 1L;
        public final VirtualHost[] virtualhosts;

        Virtualhosts(final VirtualHost[] virtualhosts) {
            this.virtualhosts = virtualhosts;
        }
    }

    private int getNumRouters(final String envname, final String routerGroupIp) {
        int numRouters;
        numRouters = routerMap.get(envname)
                .stream()
                .mapToInt(e -> e.getGroupIDs()
                        .stream()
                        .filter(g -> g.getGroupID().equals(routerGroupIp))
                        .mapToInt(r -> r.getRouters().size())
                        .sum())
                .sum();
        return numRouters;
    }
}

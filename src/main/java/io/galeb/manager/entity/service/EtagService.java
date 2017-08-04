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
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.routermap.RouterMap;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.hash.Hashing.sha256;
import static io.galeb.manager.entity.AbstractEntitySyncronizable.*;

@Service
public class EtagService {

    private static final Log LOGGER = LogFactory.getLog(EtagService.class);

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").serializeNulls().create();
    private final StringRedisTemplate template;
    private final CopyService copyService;
    private final RouterMap routerMap;
    private static final String EMPTY = "EMPTY";

    @Autowired
    public EtagService(final StringRedisTemplate template,
                       final CopyService copyService,
                       final RouterMap routerMap) {
        this.template = template;
        this.copyService = copyService;
        this.routerMap = routerMap;
    }

    public String responseBody(String envname, String groupId, String routerEtag) throws Exception {
        int numRouters = getNumRouters(envname, groupId);
        Set<String> changes = changes(envname);
        String lastETag = getLastEtag(envname);
        String routerEtagParsed = routerEtag.split(":")[0];
        String eTagChanges = getEtagChanges(changes, numRouters);

        if (isAllEtagEquals(eTagChanges, lastETag, routerEtagParsed)) {
            return HttpStatus.NOT_MODIFIED.name();
        }

        String body;
        Set<String> changesFiltered = emptyChanges(changes);
        boolean fromCache = eTagChanges.equals(lastETag) && changesFiltered.isEmpty();
        if (fromCache) {
            body = getBodyCached(envname);
        } else {
            body = getBody(envname, eTagChanges, changesFiltered, numRouters);
        }
        return "".equals(body) ? HttpStatus.NOT_FOUND.name() : body;
    }

    private String getEtagChanges(Set<String> changes, int numRouters) {
        String key = changes.stream().sorted().collect(Collectors.joining()).concat(String.valueOf(numRouters));
        return sha256().hashString(key, Charsets.UTF_8).toString();
    }

    private String getBody(String envname, String newEtag, Set<String> changesFiltered, int numRouters) throws Exception {
        String version = updateVersion(envname, changesFiltered);
        updateLastEtag(envname, newEtag);
        String json = getBodyJson(envname, numRouters, newEtag, version);
        persistToRedis(newEtag, envname, json);
        LOGGER.info("New version created: " + version + " with new etag: " + newEtag);
        return json;
    }

    private String updateVersion(String envname, Set<String> changesFiltered) {
        String newVersion = String.valueOf(template.opsForHash().increment(getInfoKey(envname), FIELD_INFO_VERSION, 1));
        changesFiltered.stream().forEach(ch -> {
            template.opsForValue().set(ch, newVersion);
        });
        return newVersion;
    }

    private Set<String> emptyChanges(Set<String> changes) {
        Set<String> changesFiltered = new HashSet<>();
        changes.stream().forEach(ch -> {
            if ("".equals(template.opsForValue().get(ch))) {
                changesFiltered.add(ch);
            }
        });
        return changesFiltered;
    }

    private void updateLastEtag(String envname, String etagChanges) {
        template.opsForHash().put(getInfoKey(envname), FIELD_INFO_ETAG, etagChanges);
    }

    private boolean isAllEtagEquals(String etagChanges, String lastETag, String routerEtag) {
        return etagChanges.equals(lastETag) && etagChanges.equals(routerEtag);
    }

    private Set<String> changes(String envname) {
        final Set<String> result = template.keys(PREFIX_HAS_CHANGE + envname + ":*");
        return (result != null) ? result : Collections.emptySet();
    }

    private void persistToRedis(String etag, String envname, String body) {
        Map<String, String> mapValues = new HashMap<String, String>() {{
            put(FIELD_INFO_CACHE, body);
            put(FIELD_INFO_ETAG, etag);
        }};
        template.opsForHash().putAll(getInfoKey(envname), mapValues);
    }

    private String getLastEtag(String envname) {
        String valueETag = (String)template.opsForHash().get(getInfoKey(envname), FIELD_INFO_ETAG);
        return valueETag != null ? valueETag : EMPTY;
    }

    private String getBodyCached(String envname) {
        String cache = (String)template.opsForHash().get(getInfoKey(envname), FIELD_INFO_CACHE);
        return Optional.ofNullable(cache).orElse("");
    }

    private String getInfoKey(String envname) {
        return PREFIX_INFO + envname;
    }

    public String getBodyJson(String envname, int numRouters, String etag, String version) throws Exception {
        String fullEtag = etag + ":" + version;
        List<VirtualHost> virtualHosts = copyService.getVirtualHosts(envname, numRouters, fullEtag);
        if (virtualHosts == null || virtualHosts.isEmpty()) {
            return "";
        }
        return gson.toJson(new Virtualhosts(virtualHosts.toArray(new VirtualHost[]{})), Virtualhosts.class);
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

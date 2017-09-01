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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.routermap.RouterMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.galeb.manager.entity.AbstractEntitySyncronizable.*;

@Service
public class EtagService {

    private static final Log LOGGER = LogFactory.getLog(EtagService.class);

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").serializeNulls().create();
    private final StringRedisTemplate template;
    private final CopyService copyService;
    private final RouterMap routerMap;

    @Autowired
    public EtagService(final StringRedisTemplate template,
                       final CopyService copyService,
                       final RouterMap routerMap) {
        this.template = template;
        this.copyService = copyService;
        this.routerMap = routerMap;
    }

    public String responseBody(String envname, String groupId, String routerEtag) throws Exception {
        String actualVersion = getActualVersion(envname);
        if (actualVersion.equals(routerEtag)) return HttpStatus.NOT_MODIFIED.name();

        int numRouters = getNumRouters(envname, groupId);
        String body = getBody(envname, actualVersion, numRouters);

        return "".equals(body) ? HttpStatus.NOT_FOUND.name() : body;
    }

    private String getBody(String envname, String actualVersion, int numRouters) throws Exception {
        String body = template.opsForValue().get(getCacheKeyWithVersion(envname, actualVersion));
        if (body == null) {
            body = getBodyJson(envname, numRouters, actualVersion);
            template.opsForValue().set(getCacheKeyWithVersion(envname, actualVersion), body, 5, TimeUnit.MINUTES);
            LOGGER.info("Created cache body for " + envname + " with version " + actualVersion);
        }
        return body;
    }

    private String getCacheKeyWithVersion(String envname, String actualVersion) {
        return PREFIX_CACHE + envname + ":" + actualVersion;
    }

    public String getBodyJson(String envname, int numRouters, String etag) throws Exception {
        List<VirtualHost> virtualHosts = copyService.getVirtualHosts(envname, numRouters, etag);
        if (virtualHosts == null || virtualHosts.isEmpty()) {
            return "";
        }
        return gson.toJson(new Virtualhosts(virtualHosts.toArray(new VirtualHost[]{})), Virtualhosts.class);
    }

    public String getActualVersion(String env) {
        String version = template.opsForValue().get(PREFIX_VERSION + env);
        if (version == null) {
            version = String.valueOf(template.opsForValue().increment(PREFIX_VERSION + env, 1));
        }
        return version;
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

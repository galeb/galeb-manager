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
import io.galeb.manager.common.ErrorLogger;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.repository.EnvironmentRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.hash.Hashing.sha256;

@Service
public class EtagService {

    private static final Log LOGGER = LogFactory.getLog(EtagService.class);

    public static final String PROP_FULLHASH = "fullhash";

    private final EnvironmentRepository environmentRepository;

    @Autowired
    public EtagService(final EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    public String buildFullHash(final VirtualHost virtualHost, int numRouters) {
        final List<String> keys = new ArrayList<>();
        keys.add(virtualHost.getLastModifiedAt().toString());
        Rule ruleDefault = virtualHost.getRuleDefault();
        if (ruleDefault != null) {
            keys.add(ruleDefault.getLastModifiedAt().toString());
            keys.add(ruleDefault.getPool().getLastModifiedAt().toString());
        }
        virtualHost.getRules().stream().sorted(Comparator.comparing(AbstractEntity::getName)).forEach(rule -> {
            keys.add(rule.getLastModifiedAt().toString());
            keys.add(rule.getPool().getLastModifiedAt().toString());
            rule.getPool().getTargets().stream().sorted(Comparator.comparing(AbstractEntity::getName))
                    .forEach(target -> keys.add(target.getLastModifiedAt().toString()));
        });
        String key = String.join("", keys) + numRouters;
        return sha256().hashString(key, Charsets.UTF_8).toString();
    }

    public String newEtag(List<VirtualHost> virtualHosts) {
        String key = virtualHosts.stream().map(this::getFullHash)
                .sorted()
                .distinct()
                .collect(Collectors.joining());
        return key == null || "".equals(key) ? "" : sha256().hashString(key, Charsets.UTF_8).toString();
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

    private Environment envFindByName(String envname) {
        final Page<Environment> envPage = environmentRepository.findByName(envname, new PageRequest(0, 1));
        Environment environment = null;
        if (envPage.hasContent()) {
            environment = envPage.iterator().next();
        }
        return environment;
    }

    public boolean routerUpdateIsNecessary(String envname, String routerEtag, final List<VirtualHost> virtualHosts) {
        final Environment environment = envFindByName(envname);
        String newEtag = newEtag(virtualHosts);
        String persistedEtag = environment.getProperties().get(EtagService.PROP_FULLHASH);
        if (!newEtag.equals(persistedEtag)) {
            try {
                updateEtag(environment, newEtag);
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
                return false;
            }
        }
        return Objects.isNull(routerEtag) || !newEtag.equals(routerEtag);
    }
}

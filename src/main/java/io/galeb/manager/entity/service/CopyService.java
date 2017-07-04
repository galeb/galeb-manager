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
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.BalancePolicy;
import io.galeb.manager.entity.BalancePolicyType;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Project;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.RuleOrder;
import io.galeb.manager.entity.RuleType;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.routermap.RouterMap;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.hash.Hashing.sha256;
import static io.galeb.manager.entity.AbstractEntitySyncronizable.PROP_FULLHASH;

@Service
public class CopyService {

    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";
    private static final String PROP_HEALTHY  = "healthy";
    private final RouterMap routerMap;
    private final VirtualHostRepository virtualHostRepository;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setExclusionStrategies(new GsonIgnoreExclusionStrategy())
            .disableInnerClassSerialization().create();

    @Autowired
    public CopyService(final RouterMap routerMap,
                       final VirtualHostRepository virtualHostRepository) {
        this.routerMap = routerMap;
        this.virtualHostRepository = virtualHostRepository;
    }

    @SuppressWarnings("WeakerAccess")
    @Transactional
    public List<VirtualHost> getVirtualHosts(String envname, String routerGroupId) throws Exception {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Stream<VirtualHost> virtualHostStream = virtualHostRepository.findByEnvironmentName(envname).stream();
        int numRouters = getNumRouters(envname, routerGroupId);
        List<VirtualHost> virtualhosts = virtualHostStream.map(virtualHost -> copyVirtualHost(virtualHost, numRouters))
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());
        SystemUserService.runAs(currentUser);
        return virtualhosts;
    }

    @Cacheable("virtualhosts")
    public VirtualHost copyVirtualHost(final VirtualHost virtualHost, int numRouters) {
        final Environment enviroment = getEnvironment(virtualHost);
        if (enviroment == null) return null;
        final Project project = getProject(virtualHost);
        final Rule virtualHostRuleDefault = virtualHost.getRuleDefault();
        final VirtualHost virtualHostCopy = gson.fromJson(gson.toJson(virtualHost), VirtualHost.class);
        virtualHostCopy.setEnvironment(enviroment);
        virtualHostCopy.setProject(project);
        if (virtualHostRuleDefault != null) {
            final Pool poolDefault = copyPool(virtualHostRuleDefault.getPool(), numRouters);
            final Rule ruleDefault = copyRule(virtualHostRuleDefault, poolDefault, virtualHost);
            virtualHostCopy.setRuleDefault(ruleDefault);
        }
        virtualHostCopy.setId(virtualHost.getId());
        virtualHostCopy.setEnvironment(enviroment);
        virtualHostCopy.setProject(project);
        virtualHostCopy.setProperties(virtualHost.getProperties());
        virtualHostCopy.setAliases(virtualHost.getAliases());
        virtualHostCopy.setRulesOrdered(virtualHost.getRulesOrdered());

        final Set<Rule> rules = copyRules(virtualHost, numRouters);
        virtualHostCopy.setRules(rules);
        virtualHostCopy.getProperties().put(PROP_FULLHASH, buildFullHash(virtualHostCopy, numRouters));
        return virtualHostCopy;
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("project")
    public Project getProject(final VirtualHost virtualHost) {
        return gson.fromJson(gson.toJson(virtualHost.getProject()), Project.class);
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("environment")
    public Environment getEnvironment(final VirtualHost virtualHost) {
        final Environment environment = gson.fromJson(gson.toJson(virtualHost.getEnvironment()), Environment.class);
        environment.getProperties().remove(PROP_FULLHASH);
        return environment;
    }

    private Rule copyRule(final Rule rule, final Pool pool, final VirtualHost virtualhost) {
        final RuleType ruleType = gson.fromJson(gson.toJson(rule.getRuleType()), RuleType.class);
        final Rule ruleCopy = gson.fromJson(gson.toJson(rule), Rule.class);
        ruleCopy.setRuleType(ruleType);
        ruleCopy.setPool(pool);
        ruleCopy.setId(rule.getId());
        ruleCopy.setProperties(rule.getProperties());
        Integer ruleOrder = virtualhost.getRulesOrdered().stream()
                .filter(r -> r.getRuleId() == rule.getId()).map(RuleOrder::getRuleOrder).findAny().orElse(Integer.MAX_VALUE);
        ruleCopy.getProperties().put("order", String.valueOf(ruleOrder));
        return ruleCopy;
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("rules")
    public Set<Rule> copyRules(final VirtualHost virtualHost, int numRouters) {
        return virtualHost.getRules().stream()
                .map(rule -> copyRule(rule, copyPool(rule.getPool(), numRouters), virtualHost))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("targets")
    public Set<Target> copyTargets(final Pool pool) {
        // Send only Targets OK (property "healthy":"OK" or status OK or status PENDING)
        return pool.getTargets().stream().filter(target -> {
            final String targetHealthy = target.getProperties().get(PROP_HEALTHY);
            final AbstractEntity.EntityStatus targetStatus = target.getStatus();
            final AbstractEntity.EntityStatus[] validStatus = {AbstractEntity.EntityStatus.OK, AbstractEntity.EntityStatus.PENDING};
            return "OK".equals(targetHealthy) || Arrays.stream(validStatus).anyMatch(e -> e == targetStatus);
        }).map(target -> {
            Target targetCopy = gson.fromJson(gson.toJson(target), Target.class);
            targetCopy.setId(target.getId());
            targetCopy.setProperties(target.getProperties());
            return targetCopy;
        }).collect(Collectors.toSet());
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("pool")
    public Pool copyPool(final Pool pool, int numRouters) {
        final Pool poolCopy = gson.fromJson(gson.toJson(pool), Pool.class);
        final BalancePolicy poolBalancePolicy = pool.getBalancePolicy();
        if (poolBalancePolicy != null) {
            final BalancePolicyType balancePolicyTypeOriginal = poolBalancePolicy.getBalancePolicyType();
            if (balancePolicyTypeOriginal != null) {
                final BalancePolicyType balancePolicyType = gson.fromJson(gson.toJson(balancePolicyTypeOriginal), BalancePolicyType.class);
                BalancePolicy balancePolicy = gson.fromJson(gson.toJson(poolBalancePolicy), BalancePolicy.class);
                balancePolicy.setBalancePolicyType(balancePolicyType);
                poolCopy.setBalancePolicy(balancePolicy);
            }
        }
        poolCopy.setProperties(pool.getProperties());
        poolCopy.getProperties().put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));
        poolCopy.setTargets(copyTargets(pool));
        return poolCopy;
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

    private String buildFullHash(final VirtualHost virtualHost, int numRouters) {
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

    private class GsonIgnoreExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(GsonIgnore.class) != null ||
                   f.getAnnotation(ManyToOne.class)  != null ||
                   f.getAnnotation(OneToMany.class)  != null ||
                   f.getAnnotation(ManyToMany.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.getAnnotation(GsonIgnore.class) != null ||
                   clazz.getAnnotation(ManyToOne.class)  != null ||
                   clazz.getAnnotation(OneToMany.class)  != null ||
                   clazz.getAnnotation(ManyToMany.class) != null;
        }
    }

}

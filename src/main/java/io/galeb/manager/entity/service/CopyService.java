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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.galeb.manager.entity.service.EtagService.PROP_FULLHASH;

@Service
public class CopyService {

    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";
    private static final String PROP_HEALTHY  = "healthy";
    private final EtagService etagService;
    private final RouterMap routerMap;
    private final VirtualHostRepository virtualHostRepository;

    @Autowired
    public CopyService(final EtagService etagService,
                       final RouterMap routerMap,
                       final VirtualHostRepository virtualHostRepository) {
        this.etagService = etagService;
        this.routerMap = routerMap;
        this.virtualHostRepository = virtualHostRepository;
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("virtualhosts")
    @Transactional
    public List<VirtualHost> getVirtualHosts(String envname, String routerGroupId) throws Exception {
        final Stream<VirtualHost> virtualHostStream = virtualHostRepository.findByEnvironmentName(envname).stream();
        return virtualHostStream.map(virtualHost -> copyVirtualHost(virtualHost, routerGroupId))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    public VirtualHost copyVirtualHost(final VirtualHost virtualHost, String routerGroupId) {
        final Environment enviroment = getEnvironment(virtualHost);
        if (enviroment == null) return null;
        final Project project = getProject(virtualHost);
        final Rule virtualHostRuleDefault = virtualHost.getRuleDefault();
        final VirtualHost virtualHostCopy = new VirtualHost(virtualHost.getName(), enviroment, project) {
            @Override
            public Date getCreatedAt() {
                return virtualHost.getCreatedAt();
            }

            @Override
            public Date getLastModifiedAt() {
                return virtualHost.getLastModifiedAt();
            }

            @Override
            public EntityStatus getStatus() {
                return virtualHost.getStatus();
            }

            @Override
            public Long getVersion() {
                return virtualHost.getVersion();
            }

            @Override
            public int getHash() {
                return virtualHost.getHash();
            }
        };
        if (virtualHostRuleDefault != null) {
            final Pool poolDefault = copyPool(virtualHostRuleDefault.getPool(), enviroment.getName(), routerGroupId);
            final Rule ruleDefault = copyRule(virtualHostRuleDefault, poolDefault, virtualHost);
            virtualHostCopy.setRuleDefault(ruleDefault);
        }
        virtualHostCopy.setId(virtualHost.getId());
        virtualHostCopy.setEnvironment(enviroment);
        virtualHostCopy.setProject(project);
        virtualHostCopy.setProperties(virtualHost.getProperties());
        virtualHostCopy.setAliases(virtualHost.getAliases());
        virtualHostCopy.setRulesOrdered(virtualHost.getRulesOrdered());

        final Set<Rule> rules = copyRules(virtualHost, routerGroupId);
        virtualHostCopy.setRules(rules);
        virtualHostCopy.getProperties().put(PROP_FULLHASH, etagService.buildFullHash(virtualHostCopy, getNumRouters(enviroment.getName(), routerGroupId)));
        return virtualHostCopy;
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("project")
    public Project getProject(VirtualHost virtualHost) {
        return new Project(virtualHost.getProject().getName()) {
            @Override
            public long getId() {
                return virtualHost.getProject().getId();
            }

            @Override
            public Date getCreatedAt() {
                return virtualHost.getProject().getCreatedAt();
            }

            @Override
            public Date getLastModifiedAt() {
                return virtualHost.getProject().getLastModifiedAt();
            }

            @Override
            public EntityStatus getStatus() {
                return virtualHost.getProject().getStatus();
            }

            @Override
            public Long getVersion() {
                return virtualHost.getProject().getVersion();
            }

            @Override
            public int getHash() {
                return virtualHost.getProject().getHash();
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("environment")
    public Environment getEnvironment(VirtualHost virtualHost) {
        return new Environment(virtualHost.getEnvironment().getName()) {
            @Override
            public long getId() {
                return virtualHost.getEnvironment().getId();
            }

            @Override
            public Date getCreatedAt() {
                return virtualHost.getEnvironment().getCreatedAt();
            }

            @Override
            public Date getLastModifiedAt() {
                return virtualHost.getEnvironment().getLastModifiedAt();
            }

            @Override
            public EntityStatus getStatus() {
                return virtualHost.getEnvironment().getStatus();
            }

            @Override
            public Long getVersion() {
                return virtualHost.getEnvironment().getVersion();
            }

            @Override
            public int getHash() {
                return virtualHost.getEnvironment().getHash();
            }
        };
    }

    private Rule copyRule(final Rule rule, final Pool pool, final VirtualHost virtualhost) {
        final RuleType ruleType = new RuleType(rule.getRuleType().getName()){
            @Override
            public long getId() {
                return rule.getRuleType().getId();
            }

            @Override
            public Date getCreatedAt() {
                return rule.getRuleType().getCreatedAt();
            }

            @Override
            public Date getLastModifiedAt() {
                return rule.getRuleType().getLastModifiedAt();
            }

            @Override
            public EntityStatus getStatus() {
                return rule.getRuleType().getStatus();
            }

            @Override
            public Long getVersion() {
                return rule.getRuleType().getVersion();
            }

            @Override
            public int getHash() {
                return rule.getRuleType().getHash();
            }
        };
        final Rule ruleCopy = new Rule(rule.getName(), ruleType, pool) {
            @Override
            public Date getCreatedAt() {
                return rule.getCreatedAt();
            }

            @Override
            public Date getLastModifiedAt() {
                return rule.getLastModifiedAt();
            }

            @Override
            public EntityStatus getStatus() {
                return rule.getStatus();
            }

            @Override
            public Long getVersion() {
                return rule.getVersion();
            }

            @Override
            public int getHash() {
                return rule.getHash();
            }
        };
        ruleCopy.setId(rule.getId());
        ruleCopy.setProperties(rule.getProperties());
        Integer ruleOrder = virtualhost.getRulesOrdered().stream()
                .filter(r -> r.getRuleId() == rule.getId()).map(RuleOrder::getRuleOrder).findAny().orElse(Integer.MAX_VALUE);
        ruleCopy.getProperties().put("order", String.valueOf(ruleOrder));
        return ruleCopy;
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("rules")
    public Set<Rule> copyRules(final VirtualHost virtualHost, String routerGroupId) {
        return virtualHost.getRules().stream()
                .map(rule -> copyRule(rule, copyPool(rule.getPool(), virtualHost.getEnvironment().getName(), routerGroupId), virtualHost))
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
            Target targetCopy = new Target(target.getName()) {
                @Override
                public Date getCreatedAt() {
                    return target.getCreatedAt();
                }

                @Override
                public Date getLastModifiedAt() {
                    return target.getLastModifiedAt();
                }

                @Override
                public EntityStatus getStatus() {
                    return target.getStatus();
                }

                @Override
                public Long getVersion() {
                    return target.getVersion();
                }

                @Override
                public int getHash() {
                    return target.getHash();
                }
            };
            targetCopy.setId(target.getId());
            targetCopy.setProperties(target.getProperties());
            return targetCopy;
        }).collect(Collectors.toSet());
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("pool")
    public Pool copyPool(final Pool pool, String envname, String routerGroupId) {
        final Pool poolCopy = new Pool(pool.getName()) {
            @Override
            public long getId() {
                return pool.getId();
            }

            @Override
            public Date getCreatedAt() {
                return pool.getCreatedAt();
            }

            @Override
            public Date getLastModifiedAt() {
                return pool.getLastModifiedAt();
            }

            @Override
            public EntityStatus getStatus() {
                return pool.getStatus();
            }

            @Override
            public Long getVersion() {
                return pool.getVersion();
            }

            @Override
            public int getHash() {
                return pool.getHash();
            }
        };
        final BalancePolicy poolBalancePolicy = pool.getBalancePolicy();
        if (poolBalancePolicy != null) {
            final BalancePolicyType balancePolicyTypeOriginal = poolBalancePolicy.getBalancePolicyType();
            if (balancePolicyTypeOriginal != null) {
                final BalancePolicyType balancePolicyType = new BalancePolicyType(balancePolicyTypeOriginal.getName()) {
                    @Override
                    public long getId() {
                        return balancePolicyTypeOriginal.getId();
                    }

                    @Override
                    public Date getCreatedAt() {
                        return balancePolicyTypeOriginal.getCreatedAt();
                    }

                    @Override
                    public Date getLastModifiedAt() {
                        return balancePolicyTypeOriginal.getLastModifiedAt();
                    }

                    @Override
                    public EntityStatus getStatus() {
                        return balancePolicyTypeOriginal.getStatus();
                    }

                    @Override
                    public Long getVersion() {
                        return balancePolicyTypeOriginal.getVersion();
                    }

                    @Override
                    public int getHash() {
                        return balancePolicyTypeOriginal.getHash();
                    }
                };
                BalancePolicy balancePolicy = new BalancePolicy(poolBalancePolicy.getName(), balancePolicyType) {
                    private final BalancePolicy balancePolicyOriginal = poolBalancePolicy;

                    @Override
                    public Date getCreatedAt() {
                        return balancePolicyOriginal.getCreatedAt();
                    }

                    @Override
                    public Date getLastModifiedAt() {
                        return balancePolicyOriginal.getLastModifiedAt();
                    }

                    @Override
                    public EntityStatus getStatus() {
                        return balancePolicyOriginal.getStatus();
                    }

                    @Override
                    public Long getVersion() {
                        return balancePolicyOriginal.getVersion();
                    }

                    @Override
                    public int getHash() {
                        return balancePolicyOriginal.getHash();
                    }
                };
                poolCopy.setBalancePolicy(balancePolicy);
            }
        }
        int numRouters = getNumRouters(envname, routerGroupId);
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

}

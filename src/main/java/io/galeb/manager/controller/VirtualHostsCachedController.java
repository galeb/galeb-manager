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

package io.galeb.manager.controller;

import io.galeb.core.util.consistenthash.HashAlgorithm;
import io.galeb.manager.common.ErrorLogger;
import io.galeb.manager.entity.*;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.EnvironmentRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.routermap.RouterMap;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value="/virtualhostscached", produces = MediaType.APPLICATION_JSON_VALUE)
public class VirtualHostsCachedController {

    private static final String PROP_HEALTHY  = "healthy";
    private static final String PROP_FULLHASH = "fullhash";

    private static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";

    private static final Log LOGGER = LogFactory.getLog(VirtualHostsCachedController.class);

    private final HashAlgorithm sha256 = new HashAlgorithm(HashAlgorithm.HashType.SHA256);

    private final VirtualHostRepository virtualHostRepository;
    private final EnvironmentRepository environmentRepository;
    private final RouterMap routerMap;

    @Autowired
    public VirtualHostsCachedController(VirtualHostRepository virtualHostRepository,
                                        EnvironmentRepository environmentRepository,
                                        RouterMap routerMap) {
        this.virtualHostRepository = virtualHostRepository;
        this.environmentRepository = environmentRepository;
        this.routerMap = routerMap;
    }

    @RequestMapping(value="/{envname:.+}", method = RequestMethod.GET)
    public synchronized ResponseEntity showall(@PathVariable String envname,
                                  @RequestHeader(value = "If-None-Match", required = false) String routerEtag,
                                  @RequestHeader(value = "X-Galeb-GroupID", required = false) String routerGroupId) throws Exception {
        return buildResponse(envname, routerGroupId, routerEtag);
    }

    @Transactional
    private ResponseEntity buildResponse(String envname, String routerGroupId, String routerEtag) throws Exception {
        int numRouters = 1;
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final List<VirtualHost> virtualHosts = new ArrayList<>();
        try {
            if (routerEtag != null && routerGroupId != null) {
                numRouters = routerMap.get(envname)
                                      .stream()
                                      .mapToInt(e -> e.getGroupIDs()
                                                      .stream()
                                                      .filter(g -> g.getGroupID().equals(routerGroupId))
                                                      .mapToInt(r -> r.getRouters().size())
                                                      .sum())
                                      .sum();
            }
            final Stream<VirtualHost> virtualHostStream = virtualHostRepository.findByEnvironmentName(envname).stream();
            virtualHosts.addAll(getVirtualHosts(virtualHostStream, numRouters));
            if (!routerUpdateIsNecessary(envname, routerEtag, virtualHosts)) {
                LOGGER.warn("If-None-Match header matchs with internal etag, then ignoring request");
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            SystemUserService.runAs(currentUser);
        }
        if (virtualHosts.isEmpty()) {
            throw new VirtualHostsEmptyException();
        }
        return new ResponseEntity<>(new Resources<>(virtualHosts), OK);
    }

    private Environment envFindByName(String envname) {
        final Page<Environment> envPage = environmentRepository.findByName(envname, new PageRequest(0, 1));
        Environment environment = null;
        if (envPage.hasContent()) {
            environment = envPage.iterator().next();
        }
        return environment;
    }

    private boolean routerUpdateIsNecessary(String envname, String routerEtag, final List<VirtualHost> virtualHosts) {
        final Environment environment = envFindByName(envname);
        String newEtag = newEtag(virtualHosts);
        String persistedEtag = environment.getProperties().get(PROP_FULLHASH);
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

    private void updateEtag(final Environment environment, String etag) throws Exception {
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

    private String newEtag(List<VirtualHost> virtualHosts) {
        String key = virtualHosts.stream().map(this::getFullHash)
                                 .sorted()
                                 .distinct()
                                 .collect(Collectors.joining());
        return key == null || "".equals(key) ? "" : sha256.hash(key).asString();
    }

    private String getFullHash(VirtualHost v) {
        return Optional.ofNullable(v.getProperties().get(PROP_FULLHASH)).orElse("");
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable("virtualhosts")
    public List<VirtualHost> getVirtualHosts(final Stream<VirtualHost> virtualHostStream, final int numRouters) throws Exception {
        return virtualHostStream.map(virtualHost -> copyVirtualHost(virtualHost, numRouters)).collect(Collectors.toList());
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
        return sha256.hash(key).asString();
    }

    private VirtualHost copyVirtualHost(final VirtualHost virtualHost, int numRouters) {
        final Environment enviroment = getEnvironment(virtualHost);
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
            final EntityStatus targetStatus = target.getStatus();
            final EntityStatus[] validStatus = {EntityStatus.OK, EntityStatus.PENDING};
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
    public Pool copyPool(final Pool pool, int numRouters) {
        //        newDiscoveredMembersSize = Math.max(externalDataService.members().size(), 1);
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
        poolCopy.setProperties(pool.getProperties());
        poolCopy.getProperties().put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(numRouters));
        poolCopy.setTargets(copyTargets(pool));
        return poolCopy;
    }

    @ResponseStatus(value= HttpStatus.NOT_FOUND, reason = "Virtualhosts empty in this environment")
    private static class VirtualHostsEmptyException extends Exception
    {
        private static final long serialVersionUID = 1L;
    }
}

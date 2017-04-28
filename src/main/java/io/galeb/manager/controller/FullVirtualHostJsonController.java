package io.galeb.manager.controller;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/fullvhjson")
public class FullVirtualHostJsonController {

    private static final String QUERY_VIRTUALHOST = "SELECT v FROM VirtualHost v WHERE v.name = :name AND v.environment.name = :envname";
    private static final String QUERY_TARGET      = "SELECT t FROM Target t WHERE t.parent.name = :name";
    private static final String QUERY_RULE        = "SELECT r FROM Rule r";

    private static final Log LOGGER = LogFactory.getLog(FullVirtualHostJsonController.class);

    @PersistenceContext
    private EntityManager em;

    @RequestMapping(value="/{envname:.+}/{hostname:.+}", method = RequestMethod.GET)
    public VirtualHost showall(@PathVariable String envname, @PathVariable String hostname) throws VirtualHostNotFoundException {
        final List<VirtualHost> virtualHosts = em.createQuery(QUERY_VIRTUALHOST, VirtualHost.class)
                .setParameter("name", hostname).setParameter("envname", envname).getResultList();
        if (!virtualHosts.isEmpty()) {
            return copyVirtualHost(virtualHosts.iterator().next());
        } else {
            LOGGER.error(hostname + " NOT FOUND");
            throw new VirtualHostNotFoundException();
        }
    }

    private VirtualHost copyVirtualHost(final VirtualHost virtualHost) {
        final Environment enviroment = getEnvironment(virtualHost);
        final Project project = getProject(virtualHost);
        final Pool poolDefault = copyPool(virtualHost.getRuleDefault().getPool());
        final Rule ruleDefault = copyRule(virtualHost.getRuleDefault(), poolDefault, virtualHost);

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
        virtualHostCopy.setId(virtualHost.getId());
        virtualHostCopy.setEnvironment(enviroment);
        virtualHostCopy.setProject(project);
        virtualHostCopy.setRuleDefault(ruleDefault);
        virtualHostCopy.setProperties(virtualHost.getProperties());
        virtualHostCopy.setAliases(virtualHost.getAliases());
        virtualHostCopy.setRulesOrdered(virtualHost.getRulesOrdered());

        final Set<Rule> rules = copyRules(virtualHost);
        virtualHostCopy.setRules(rules);
        return virtualHostCopy;
    }

    private Project getProject(VirtualHost virtualHost) {
        return new Project(virtualHost.getProject().getName()) {
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

    private Environment getEnvironment(VirtualHost virtualHost) {
        return new Environment(virtualHost.getEnvironment().getName()) {
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
        ruleCopy.setProperties(rule.getProperties());

        Optional<Integer> ruleOrder = virtualhost.getRulesOrdered().stream()
                .filter(r -> r.getRuleId() == rule.getId()).map(RuleOrder::getRuleOrder).findAny();
        ruleCopy.setRuleOrder(ruleOrder.orElse(Integer.MAX_VALUE));
        return ruleCopy;
    }
    
    private Set<Rule> copyRules(final VirtualHost virtualHost) {
        return em.createQuery(QUERY_RULE, Rule.class).getResultList().stream()
                .filter(rule -> rule.getParents().contains(virtualHost))
                .map(rule -> copyRule(rule, copyPool(rule.getPool()), virtualHost))
                .collect(Collectors.toSet());
    }

    private void copyTargets(final Pool pool) {
        final List<Target> targetsOriginal = em.createQuery(QUERY_TARGET, Target.class)
                .setParameter("name", pool.getName()).getResultList();
        if (!targetsOriginal.isEmpty()) {
            Set<Target> targets = targetsOriginal.stream().map(target -> {
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
                targetCopy.setProperties(target.getProperties());
                return targetCopy;
            }).collect(Collectors.toSet());
            pool.setTargets(targets);
        }
    }

    private Pool copyPool(final Pool pool) {
        final Pool poolCopy = new Pool(pool.getName()) {
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
        final BalancePolicyType balancePolicyType = new BalancePolicyType(pool.getBalancePolicy().getBalancePolicyType().getName()){
            private final BalancePolicyType balancePolicyTypeOriginal = pool.getBalancePolicy().getBalancePolicyType();

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
        BalancePolicy balancePolicy = new BalancePolicy(pool.getBalancePolicy().getName(), balancePolicyType){
            private final BalancePolicy balancePolicyOriginal = pool.getBalancePolicy();
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
        poolCopy.setProperties(pool.getProperties());
        copyTargets(poolCopy);
        return poolCopy;
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason = "Virtualhost not found in this environment")
    private class VirtualHostNotFoundException extends Exception
    {
        private static final long serialVersionUID = 1L;
    }
}

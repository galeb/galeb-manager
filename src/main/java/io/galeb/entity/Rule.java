package io.galeb.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Rule extends AbstractEntity<Rule> {

    private static final long serialVersionUID = 5596582746795373020L;

    @OneToOne
    @JoinColumn(nullable = false)
    private RuleType ruleType;

    @ManyToOne(fetch = FetchType.EAGER)
    private Environment environment;

    @JsonIgnore
    private long farmId;

    @ManyToOne(fetch = FetchType.EAGER)
    private VirtualHost parent;

    public Rule(String name, RuleType ruleType, Environment environment) {
        Assert.notNull(ruleType);
        setName(name);
        this.ruleType = ruleType;
    }

    protected Rule() {
        //
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public Rule setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
        return this;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Rule setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public long getFarmId() {
        return farmId;
    }

    public Rule setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    public VirtualHost getParent() {
        return parent;
    }

    public Rule setParent(VirtualHost parent) {
        Assert.notNull(parent);
        this.parent = parent;
        return this;
    }

}

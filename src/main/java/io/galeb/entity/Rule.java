package io.galeb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

@Entity
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(nullable = false)
    private RuleType ruleType;

    @OneToOne
    @JoinColumn(nullable = true)
    private Rule nextRule;

    @OneToOne
    @JoinColumn(nullable = true)
    private Target target;

    public Rule(String name, RuleType ruleType) {
        Assert.hasText(name);
        Assert.notNull(ruleType);
        this.name = name;
        this.ruleType = ruleType;
    }

    protected Rule() {
        //
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Rule setName(String name) {
        this.name = name;
        return this;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public Rule setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
        return this;
    }

    public Rule getNextRule() {
        return nextRule;
    }

    public Rule setNextRule(Rule nextRule) {
        this.nextRule = nextRule;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Rule setTarget(Target target) {
        this.target = target;
        return this;
    }
}

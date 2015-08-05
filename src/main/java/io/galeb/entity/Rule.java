package io.galeb.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

@Entity
public class Rule extends AbstractEntity<Rule> {

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

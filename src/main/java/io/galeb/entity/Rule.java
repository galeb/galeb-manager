package io.galeb.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

@Entity
public class Rule extends EntityAffiliable<Rule> {

    private static final long serialVersionUID = 5596582746795373020L;

    @OneToOne
    @JoinColumn(nullable = false)
    private RuleType ruleType;

    public Rule(String name, RuleType ruleType, String parent) {
        Assert.notNull(ruleType);
        setName(name);
        setParent(parent);
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

}

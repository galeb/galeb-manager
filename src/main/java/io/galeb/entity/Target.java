package io.galeb.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

@Entity
public class Target extends AbstractEntity<Target> {

    private static final long serialVersionUID = 5596582746795373012L;

    @OneToOne
    @JoinColumn(nullable = false)
    private TargetType targetType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Rule parent;

    public Target(String name, TargetType targetType, Rule parent) {
        Assert.notNull(targetType);
        Assert.notNull(parent);
        setName(name);
        this.parent = parent;
        this.targetType = targetType;
    }

    protected Target() {
        //
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public Target setTargetType(TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    public Rule getParent() {
        return parent;
    }

    public Target setParent(Rule parent) {
        Assert.notNull(parent);
        this.parent = parent;
        return this;
    }

}

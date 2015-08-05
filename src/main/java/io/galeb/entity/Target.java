package io.galeb.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

@Entity
public class Target extends AbstractEntity<Target> {

    @OneToOne
    @JoinColumn(nullable = false)
    private TargetType targetType;

    @OneToOne
    @JoinColumn(nullable = true)
    private Target nextTarget;

    public Target(String name, TargetType targetType) {
        Assert.notNull(targetType);
        setName(name);
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

    public Target getNextTarget() {
        return nextTarget;
    }

    public Target setNextTarget(Target nextTarget) {
        this.nextTarget = nextTarget;
        return this;
    }
}

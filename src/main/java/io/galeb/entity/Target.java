package io.galeb.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

@Entity
public class Target extends EntityAffiliable<Target> {

    @OneToOne
    @JoinColumn(nullable = false)
    private TargetType targetType;

    public Target(String name, TargetType targetType, String parent) {
        Assert.notNull(targetType);
        setName(name);
        setParent(parent);
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

}

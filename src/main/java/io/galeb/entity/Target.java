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
public class Target {

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
    private TargetType targetType;

    @OneToOne
    @JoinColumn(nullable = true)
    private Target nextTarget;

    public Target(String name, TargetType targetType) {
        Assert.hasText(name);
        Assert.notNull(targetType);
        this.name = name;
        this.targetType = targetType;
    }

    protected Target() {
        //
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Target setName(String name) {
        this.name = name;
        return this;
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

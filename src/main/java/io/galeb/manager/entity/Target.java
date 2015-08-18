package io.galeb.manager.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Target extends AbstractEntity<Target> {

    private static final long serialVersionUID = 5596582746795373012L;

    @OneToOne
    @JoinColumn(nullable = false)
    private TargetType targetType;

    @ManyToOne(fetch = FetchType.EAGER)
    private Environment environment;

    @JsonIgnore
    private long farmId;

    @ManyToOne(fetch = FetchType.EAGER)
    private Target parent;

    @JsonIgnore
    @OneToMany(mappedBy = "target", fetch = FetchType.EAGER)
    private final Set<Rule> rules = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private Project project;

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

    public Environment getEnvironment() {
        return environment;
    }

    public Target setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public long getFarmId() {
        return farmId;
    }

    public Target setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    public Target getParent() {
        return parent;
    }

    public Target setParent(Target parent) {
        this.parent = parent;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public Target setProject(Project project) {
        this.project = project;
        return this;
    }

}

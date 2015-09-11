/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.entity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@NamedQuery(name = "Target.findAll", query =
"SELECT ta FROM Target ta "
        + "INNER JOIN ta.project.teams t "
        + "INNER JOIN t.accounts a "
        + "WHERE 1 = :hasRoleAdmin OR "
        + "ta.global = TRUE OR "
        + "a.name = :principalName")
@Entity
@JsonInclude(NON_NULL)
public class Target extends AbstractEntity<Target> implements WithFarmID<Target> {

    private static final long serialVersionUID = 5596582746795373012L;

    @ManyToOne
    @JoinColumn(name = "targettype_id", nullable = false, foreignKey = @ForeignKey(name="FK_target_targettype"))
    @JsonProperty(required = true)
    private TargetType targetType;

    @ManyToOne
    @JoinColumn(name = "environment_id", foreignKey = @ForeignKey(name="FK_target_environment"))
    private Environment environment;

    @JsonIgnore
    private long farmId;

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "target_id", nullable = true, foreignKey = @ForeignKey(name="FK_parent_target")),
    inverseJoinColumns=@JoinColumn(name = "parent_id", nullable = true, foreignKey = @ForeignKey(name="FK_target_parent")))
    private Set<Target> parents = new HashSet<>();

    @ManyToMany(mappedBy="parents")
    private Set<Target> children = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "target", fetch = FetchType.EAGER)
    private final Set<Rule> rules = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name="FK_target_project"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "balancepolicy_id", foreignKey = @ForeignKey(name="FK_target_balancepolicy"))
    private BalancePolicy balancePolicy;

    @JsonIgnore
    private Boolean global = false;

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

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Target setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    public Set<Target> getParents() {
        return parents;
    }

    public Target setParents(Set<Target> parents) {
        if (parents != null) {
            this.parents.clear();
            this.parents.addAll(parents);
        }
        return this;
    }

    public Set<Target> getChildren() {
        return children;
    }

    public Target setChildren(Set<Target> children) {
        if (children != null) {
            this.children.clear();
            this.children.addAll(children);
        }
        return this;
    }

    public Project getProject() {
        return project;
    }

    public Target setProject(Project project) {
        this.project = project;
        return this;
    }

    public BalancePolicy getBalancePolicy() {
        return balancePolicy;
    }

    public Target setBalancePolicy(BalancePolicy balancePolicy) {
        this.balancePolicy = balancePolicy;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public Target setGlobal(Boolean global) {
        if (global != null) {
            this.global = global;
        }
        return this;
    }

}

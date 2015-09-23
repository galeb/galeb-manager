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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.galeb.manager.repository.custom.TargetRepositoryImpl;

@NamedQuery(name = "Target.findAll", query = TargetRepositoryImpl.FIND_ALL)
@Entity
@JsonInclude(NON_NULL)
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_parent_id_target", columnNames = { "name", "parent_id" }) })
public class Target extends AbstractEntity<Target> implements WithFarmID<Target>, WithParent<Target> {

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

    @ManyToOne
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name="FK_target_parent"))
    private Target parent;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parent")
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

    @Column(insertable = false, updatable = false, nullable = false)
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

    @Override
    public Target getParent() {
        return parent;
    }

    public Target setParent(Target parent) {
        this.parent = parent;
        return this;
    }

    @Override
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

    @JsonProperty("_global")
    public boolean isGlobal() {
        return global;
    }

    @JsonIgnore
    public Target setGlobal(Boolean global) {
        if (global != null) {
            this.global = global;
        }
        return this;
    }

}

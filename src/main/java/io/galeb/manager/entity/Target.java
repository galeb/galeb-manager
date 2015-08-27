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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@NamedQuery(name="Target.findAll", query =
"SELECT ta FROM Target ta "
        + "INNER JOIN ta.project.teams t "
        + "INNER JOIN t.accounts a "
        + "WHERE 1 = :hasRoleAdmin OR "
        + "a.name = :principalName")
@Entity
@JsonInclude(NON_NULL)
public class Target extends AbstractEntity<Target> implements WithFarmID<Target> {

    private static final long serialVersionUID = 5596582746795373012L;

    @ManyToOne
    @JoinColumn(name = "targettype_id", nullable = false)
    private TargetType targetType;

    @ManyToOne
    @JoinColumn(name = "environment_id")
    private Environment environment;

    @JsonIgnore
    private long farmId;

    @ManyToOne
    private Target parent;

    @JsonIgnore
    @OneToMany(mappedBy = "target", fetch = FetchType.EAGER)
    private final Set<Rule> rules = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "balancepolicy_id")
    private BalancePolicy balancePolicy;

    @Override
    protected Set<String> readOnlyFields() {
        return AbstractEntity.defaultReadOnlyFields;
    }

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

    public BalancePolicy getBalancePolicy() {
        return balancePolicy;
    }

    public Target setBalancePolicy(BalancePolicy balancePolicy) {
        this.balancePolicy = balancePolicy;
        return this;
    }

}

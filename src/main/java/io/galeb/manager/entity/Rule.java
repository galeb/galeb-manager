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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.galeb.manager.repository.custom.RuleRepositoryImpl;

@NamedQuery(name="Rule.findAll", query = RuleRepositoryImpl.FIND_ALL)
@Entity
@JsonInclude(NON_NULL)
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_rule", columnNames = { "name" }) })
public class Rule extends AbstractEntity<Rule> implements WithFarmID<Rule>, WithParents<VirtualHost> {

    private static final long serialVersionUID = 5596582746795373020L;

    @ManyToOne
    @JoinColumn(name = "ruletype_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_ruletype"))
    @JsonProperty(required = true)
    private RuleType ruleType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns=@JoinColumn(name = "rule_id", nullable = true, foreignKey = @ForeignKey(name="FK_rule_virtualhost")),
    inverseJoinColumns=@JoinColumn(name = "parents_id", nullable = true, foreignKey = @ForeignKey(name="FK_parents_rule")))
    private Set<VirtualHost> parents = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false, foreignKey = @ForeignKey(name="FK_rule_target"))
    @JsonProperty(required = true)
    private Target target;

    @Column
    @JsonProperty("order")
    private int ruleOrder = 0;

    @Column
    @JsonProperty("default")
    private boolean ruleDefault = false;

    @Column
    private Boolean global = false;

    @JsonIgnore
    private long farmId;

    public Rule(String name, RuleType ruleType, Target target) {
        Assert.notNull(ruleType);
        Assert.notNull(target);
        setName(name);
        this.ruleType = ruleType;
        this.target = target;
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

    @Override
    public Set<VirtualHost> getParents() {
        return parents;
    }

    public Rule setParents(Set<VirtualHost> parents) {
        if (parents != null) {
            this.parents.clear();
            this.parents.addAll(parents);
        }
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Rule setTarget(Target target) {
        Assert.notNull(target);
        this.target = target;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public Rule setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    public int getRuleOrder() {
        return ruleOrder;
    }

    public Rule setRuleOrder(int ruleOrder) {
        this.ruleOrder = ruleOrder;
        return this;
    }

    public boolean isRuleDefault() {
        return ruleDefault;
    }

    public Rule setRuleDefault(boolean ruleDefault) {
        this.ruleDefault = ruleDefault;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public Rule setGlobal(Boolean global) {
        if (global != null) {
            this.global = global;
        }
        return this;
    }

}

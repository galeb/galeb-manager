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

import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@NamedQuery(name="Rule.findAll", query =
"SELECT r FROM Rule r "
        + "INNER JOIN r.target.project.teams t "
        + "INNER JOIN t.accounts a "
        + "WHERE 1 = :hasRoleAdmin OR "
             + "r.parent IS NULL OR "
             + "a.name = :principalName")
@Entity
@JsonInclude(NON_NULL)
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_rule", columnNames = { "name" }) })
public class Rule extends AbstractEntity<Rule> implements WithFarmID<Rule> {

    private static final long serialVersionUID = 5596582746795373020L;

    @ManyToOne
    @JoinColumn(name = "ruletype", nullable = false, foreignKey = @ForeignKey(name="FK_rule_ruletype"))
    private RuleType ruleType;

    @ManyToOne
    @JoinColumn(name = "parent", nullable = true, foreignKey = @ForeignKey(name="FK_rule_parent"))
    private VirtualHost parent;

    @ManyToOne
    @JoinColumn(name = "target", nullable = false, foreignKey = @ForeignKey(name="FK_rule_target"))
    private Target target;

    @Column
    private int ruleOrder = 0;

    @Column
    private boolean ruleDefault = false;

    @JsonIgnore
    private long farmId;

    @Override
    protected Set<String> readOnlyFields() {
        return AbstractEntity.defaultReadOnlyFields;
    }

    public Rule(String name, RuleType ruleType, VirtualHost parent, Target target) {
        Assert.notNull(ruleType);
        Assert.notNull(target);
        setName(name);
        this.ruleType = ruleType;
        this.parent = parent;
        this.target = target;
    }

    protected Rule() {
        //
    }

    @Override
    @JoinColumn(foreignKey=@ForeignKey(name="FK_rule_properties"))
    public Map<String, String> getProperties() {
        return super.getProperties();
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public Rule setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
        return this;
    }

    public VirtualHost getParent() {
        return parent;
    }

    public Rule setParent(VirtualHost parent) {
        this.parent = parent;
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

}

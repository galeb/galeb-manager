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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.stream.Collectors.toSet;

@Entity
@Table(name = "virtualhost", uniqueConstraints = { @UniqueConstraint(name = "UK_name_virtualhost", columnNames = { "name" }) })
public class VirtualHost extends AbstractEntity<VirtualHost> implements WithFarmID<VirtualHost>, WithAliases<VirtualHost> {

    private static final long serialVersionUID = 5596582746795373014L;

    @ManyToOne
    @JoinColumn(name = "environment_id",  nullable = false, foreignKey = @ForeignKey(name="FK_virtualhost_environment"))
    @JsonProperty(required = true)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name="FK_virtualhost_project"))
    @JsonProperty(required = true)
    private Project project;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> aliases = new HashSet<>();

    @JsonIgnore
    private long farmId;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = RuleOrder.class)
    private final Set<RuleOrder> rulesOrdered = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "rule_default_id", foreignKey = @ForeignKey(name="FK_virtualhost_rule_default_id"))
    private Rule ruleDefault;

    @ManyToMany(mappedBy = "parents")
    private final Set<Rule> rules = new HashSet<>();

    public VirtualHost(String name, Environment environment, Project project) {
        Assert.notNull(environment);
        Assert.notNull(project);
        setName(name);
        this.environment = environment;
        this.project = project;
    }

    protected VirtualHost() {
        //
    }

    public Environment getEnvironment() {
        return environment;
    }

    public VirtualHost setEnvironment(Environment environment) {
        updateHash();
        this.environment = environment;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public VirtualHost setProject(Project project) {
        updateHash();
        this.project = project;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public VirtualHost setFarmId(long farmId) {
        updateHash();
        this.farmId = farmId;
        return this;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public VirtualHost setAliases(Set<String> aliases) {
        if (aliases != null) {
            updateHash();
            this.aliases.clear();
            this.aliases.addAll(aliases);
        }
        return this;
    }

    public Set<RuleOrder> getRulesOrdered() {
        return rulesOrdered;
    }

    public VirtualHost setRulesOrdered(Set<RuleOrder> rulesOrdered) {
        if (rulesOrdered != null) {
            updateHash();
            this.rulesOrdered.clear();
            this.rulesOrdered.addAll(rulesOrdered);
        }
        return this;
    }

    public Rule getRuleDefault() {
        return ruleDefault;
    }

    public VirtualHost setRuleDefault(Rule ruleDefault) {
        updateHash();
        this.ruleDefault = ruleDefault;
        return this;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public VirtualHost setRules(Set<Rule> rules) {
        if (rules != null) {
            updateHash();
            this.rules.clear();
            this.rules.addAll(rules);
        }
        return this;
    }

    @Override
    public EntityStatus getStatus() {
        return getStatusFromMap();
    }
}

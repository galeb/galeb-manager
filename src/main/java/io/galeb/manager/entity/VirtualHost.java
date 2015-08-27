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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NamedQuery(name="VirtualHost.findAll", query=
"SELECT v FROM VirtualHost v "
        + "INNER JOIN v.project.teams t "
        + "INNER JOIN t.accounts a "
        + "WHERE 1 = :hasRoleAdmin OR "
        + "a.name = :principalName")
@Entity
public class VirtualHost extends AbstractEntity<VirtualHost> implements WithFarmID<VirtualHost> {

    private static final long serialVersionUID = 5596582746795373014L;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Environment environment;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Project project;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> aliases = new HashSet<>();

    @JsonIgnore
    private long farmId;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private final Set<Rule> rules = new HashSet<>();

    @Override
    protected Set<String> readOnlyFields() {
        return AbstractEntity.defaultReadOnlyFields;
    }

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
        this.environment = environment;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public VirtualHost setProject(Project project) {
        this.project = project;
        return this;
    }

    @Override
    public long getFarmId() {
        return farmId;
    }

    @Override
    public VirtualHost setFarmId(long farmId) {
        this.farmId = farmId;
        return this;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        if (aliases != null) {
            aliases.clear();
            aliases.addAll(aliases);
        }
        this.aliases = aliases;
    }

}

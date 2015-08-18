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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

@Entity
public class Team extends AbstractEntity<Team> {

    private static final long serialVersionUID = -4278444359290384175L;

    @ManyToMany(mappedBy = "teams", fetch = FetchType.EAGER)
    private final Set<Account> accounts = new HashSet<>();

    @ManyToMany(mappedBy = "teams", fetch = FetchType.EAGER)
    private final Set<Project> projects = new HashSet<>();

    public Set<Account> getAccounts() {
        return accounts;
    }

    public Team setAccounts(Set<Account> accounts) {
        if (accounts != null) {
            this.accounts.clear();
            this.accounts.addAll(accounts);
        }
        return this;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Team setProjects(Set<Project> projects) {
        if (projects != null) {
            this.projects.clear();
            this.projects.addAll(projects);
        }
        return this;
    }

}

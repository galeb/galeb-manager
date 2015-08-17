package io.galeb.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

@Entity
public class Team extends AbstractEntity<Team> {

    private static final long serialVersionUID = -4278444359290384175L;

    @ManyToMany(fetch = FetchType.EAGER)
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

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
    private Set<Account> accounts = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Project> projects = new HashSet<>();

    public Set<Account> getAccounts() {
        return accounts;
    }

    public Team setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
        return this;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Team setProjects(Set<Project> projects) {
        this.projects = projects;
        return this;
    }

}

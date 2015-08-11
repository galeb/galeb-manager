package io.galeb.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

@Entity
public class Account extends AbstractEntity<Account> {

    private static final long serialVersionUID = -2745836665462717899L;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Team> teams;

    @Column(nullable = false, unique = true)
    private String email;

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public Account setTeams(Set<Team> teams) {
        this.teams = teams;
        return this;
    }

}

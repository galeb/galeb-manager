package io.galeb.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

@Entity
public class Account extends AbstractEntity<Account> {

    private static final long serialVersionUID = -2745836665462717899L;

    public enum Role {
        USER,
        ADMIN
    }

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Team> teams;

    @Column(nullable = false)
    private String email;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

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

    public Role getRole() {
        return role;
    }

    public Account setRole(Role role) {
        this.role = role;
        return this;
    }

}

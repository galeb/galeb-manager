package io.galeb.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Project extends AbstractEntity<Project> {

    private static final long serialVersionUID = 5596582746795373018L;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    private final Set<VirtualHost> virtualhosts = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    private final Set<Target> targets = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns=@JoinColumn(name="team_id"),
               inverseJoinColumns=@JoinColumn(name="project_id"))
    private final Set<Team> teams = new HashSet<>();

    public Project(String name) {
        setName(name);
    }

    protected Project() {
        //
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public Project setTeams(Set<Team> teams) {
        if (teams != null) {
            this.teams.clear();
            this.teams.addAll(teams);
        }
        return this;
    }

}

package io.galeb.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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

    public Project(String name) {
        setName(name);
    }

    protected Project() {
        //
    }
}

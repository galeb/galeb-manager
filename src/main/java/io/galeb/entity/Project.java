package io.galeb.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Project extends AbstractEntity<Project> {

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private final Set<VirtualHost> virtualhosts = new HashSet<>();

    public Project(String name) {
        setName(name);
    }

    protected Project() {
        //
    }
}

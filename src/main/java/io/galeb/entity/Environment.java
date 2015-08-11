package io.galeb.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Environment extends AbstractEntity<Environment> {

    private static final long serialVersionUID = 5596582746795373016L;

    @JsonIgnore
    @OneToMany(mappedBy = "environment", fetch = FetchType.EAGER)
    private final Set<Farm> farms = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "environment", fetch = FetchType.EAGER)
    private final Set<VirtualHost> virtualhosts = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "environment", fetch = FetchType.EAGER)
    private final Set<Target> targets = new HashSet<>();

    public Environment(String name) {
        setName(name);
    }

    protected Environment() {
        //
    }

}

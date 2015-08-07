package io.galeb.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class TargetType extends AbstractEntity<TargetType> {

    private static final long serialVersionUID = 5596582746795373013L;

    @OneToMany()
    private Set<Target> targets;

    public TargetType(String name) {
        setName(name);
    }

    protected TargetType() {
        //
    }

    public Set<Target> getTargets() {
        return targets;
    }
}

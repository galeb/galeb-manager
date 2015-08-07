package io.galeb.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.util.Assert;

@MappedSuperclass
public abstract class EntityAffiliable<T extends AbstractEntity<?>> extends AbstractEntity<T> {

    private static final long serialVersionUID = 5596582746795373015L;

    @Column(nullable = false)
    private String parent;

    public String getParent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    public T setParent(String parent) {
        Assert.notNull(parent);
        this.parent = parent;
        return (T) this;
    }

}

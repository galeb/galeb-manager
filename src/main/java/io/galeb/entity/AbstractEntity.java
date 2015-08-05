package io.galeb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<?>> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Column(unique = true, nullable = false)
    private String name;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        Assert.hasText(name);
        this.name = name;
        return (T) this;
    }

}

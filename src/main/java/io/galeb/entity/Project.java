package io.galeb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Column(unique = true, nullable = false)
    private String name;

    public Project(String name) {
        Assert.hasText(name);
        this.name = name;
    }

    protected Project() {
        //
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Project setName(String name) {
        this.name = name;
        return this;
    }
}

package io.galeb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

@Entity
public class VirtualHost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Environment environment;

    public VirtualHost(String name, Environment environment) {
        Assert.hasText(name);
        Assert.notNull(environment);
        this.name = name;
        this.environment = environment;
    }

    protected VirtualHost() {
        //
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VirtualHost setName(String name) {
        this.name = name;
        return this;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public VirtualHost setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }
}

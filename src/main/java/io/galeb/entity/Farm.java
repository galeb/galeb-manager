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
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String api;

    public Farm(String name,String domain, String api) {
        Assert.hasText(name);
        Assert.hasText(domain);
        Assert.hasText(api);
        this.name = name;
        this.domain = domain;
        this.api = api;
    }

    protected Farm() {
        //
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getApi() {
        return api;
    }

    public Farm setName(String name) {
        this.name = name;
        return this;
    }

    public Farm setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public Farm setApi(String api) {
        this.api = api;
        return this;
    }
}

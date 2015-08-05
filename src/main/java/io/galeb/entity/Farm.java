package io.galeb.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.util.Assert;

@Entity
public class Farm extends AbstractEntity<Farm> {

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String api;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Environment environment;

    public Farm(String name,String domain, String api, Environment environment) {
        Assert.hasText(domain);
        Assert.hasText(api);
        Assert.notNull(environment);
        setName(name);
        this.domain = domain;
        this.api = api;
        this.environment = environment;
    }

    protected Farm() {
        //
    }

    public String getDomain() {
        return domain;
    }

    public Farm setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getApi() {
        return api;
    }

    public Farm setApi(String api) {
        this.api = api;
        return this;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Farm setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }
}

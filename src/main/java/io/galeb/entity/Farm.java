package io.galeb.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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

    @OneToOne
    @JoinColumn(nullable = false)
    private Provider provider;

    public Farm(String name,String domain, String api, Environment environment, Provider provider) {
        Assert.hasText(domain);
        Assert.hasText(api);
        Assert.notNull(environment);
        Assert.notNull(provider);
        setName(name);
        this.domain = domain;
        this.api = api;
        this.environment = environment;
        this.provider = provider;
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

    public Provider getProvider() {
        return provider;
    }

    public Farm setProvider(Provider provider) {
        this.provider = provider;
        return this;
    }
}

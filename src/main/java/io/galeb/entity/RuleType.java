package io.galeb.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

@Entity
public class RuleType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany
    private Set<Rule> rules;

    public RuleType(String name) {
        Assert.hasText(name);
        this.name = name;
    }

    protected RuleType() {
        //
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RuleType setName(String name) {
        this.name = name;
        return this;
    }

    public Set<Rule> getRules() {
        return rules;
    }
}

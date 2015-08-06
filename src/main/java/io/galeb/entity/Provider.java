package io.galeb.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Provider extends AbstractEntity<Provider> {

    @JsonIgnore
    @OneToMany(mappedBy = "provider")
    private final Set<Farm> farms = new HashSet<>();

    @Column
    private String driver;

    @Column
    private String provisioning;

    public Provider(String name) {
        setName(name);
    }

    protected Provider() {
        //
    }

    public String getDriver() {
        return driver;
    }

    public Provider setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public String getProvisioning() {
        return provisioning;
    }

    public Provider setProvisioning(String provisioning) {
        this.provisioning = provisioning;
        return this;
    }
}

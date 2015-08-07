package io.galeb.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<?>> implements Serializable {

    private static final long serialVersionUID = 4521414292400791447L;

    public enum EntityStatus {
        PENDING,
        OK,
        ERROR,
        UNKNOWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @CreatedDate
    public Date createdDate;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Column(unique = true, nullable = false)
    private String name;

    @ElementCollection
    private final Map<String, String> properties = new HashMap<>();

    @Column
    private EntityStatus status;

    public AbstractEntity() {
        this.status = EntityStatus.UNKNOWN;
    }

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

    public Map<String, String> getProperties() {
        return properties;
    }

    public EntityStatus getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    public T setStatus(EntityStatus status) {
        this.status = status;
        return (T) this;
    }

}

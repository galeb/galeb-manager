package io.galeb.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

import io.galeb.security.SpringSecurityAuditorAware;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<?>> implements Serializable {

    private static final long serialVersionUID = 4521414292400791447L;

    public enum EntityStatus {
        PENDING,
        OK,
        ERROR,
        UNKNOWN,
        DISABLED,
        ENABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    public Long version;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    public String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    public Date createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    public Date lastModifiedDate;

    @LastModifiedBy
    @Column(nullable = false)
    public String lastModifiedBy;

    @Column(unique = true, nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private final Map<String, String> properties = new HashMap<>();

    @Column(nullable = false)
    private EntityStatus status;

    @PrePersist
    private void onCreate() {
        createdDate = new Date();
        createdBy = getCurrentAuditor();
        lastModifiedDate = createdDate;
        lastModifiedBy = createdBy;
    }

    @PreUpdate
    private void onUpdate() {
        lastModifiedDate = new Date();
        lastModifiedBy = getCurrentAuditor();
    }

    private String getCurrentAuditor() {
        final SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();
        return auditorAware.getCurrentAuditor();
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

    @SuppressWarnings("unchecked")
    public T setProperties(Map<String, String> properties) {
        if (properties != null) {
            this.properties.clear();
            this.properties.putAll(properties);
        }
        return (T) this;
    }

    public EntityStatus getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    public T setStatus(EntityStatus aStatus) {
        status = Optional.ofNullable(aStatus).orElse(status);
        return (T) this;
    }

}

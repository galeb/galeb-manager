package io.galeb.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.util.Assert;

@Entity
public class VirtualHost extends AbstractEntity<VirtualHost> {

    private static final long serialVersionUID = 5596582746795373014L;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Environment environment;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Project project;

    public VirtualHost(String name, Environment environment, Project project) {
        Assert.notNull(environment);
        Assert.notNull(project);
        setName(name);
        this.environment = environment;
        this.project = project;
    }

    protected VirtualHost() {
        //
    }

    public Environment getEnvironment() {
        return environment;
    }

    public VirtualHost setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public VirtualHost setProject(Project project) {
        this.project = project;
        return this;
    }
}

package io.galeb.engine.farm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.engine.DriverBuilder;
import io.galeb.entity.Project;

@Component
public class ProjectEngine {

    public static final String QUEUE_CREATE = "queue-project-create";
    public static final String QUEUE_UPDATE = "queue-project-update";
    public static final String QUEUE_REMOVE = "queue-project-remove";

    private static final Log LOGGER = LogFactory.getLog(ProjectEngine.class);

    private Driver getDriver(Project project) {
        return DriverBuilder.build(Driver.DEFAULT_DRIVER_NAME);
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Project project) {
        LOGGER.info("Creating "+project.getClass().getSimpleName()+" "+project.getName());
        Driver driver = getDriver(project);
        driver.create(project);
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Project project) {
        LOGGER.info("Updating "+project.getClass().getSimpleName()+" "+project.getName());
        Driver driver = getDriver(project);
        driver.update(project);
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Project project) {
        LOGGER.info("Removing "+project.getClass().getSimpleName()+" "+project.getName());
        Driver driver = getDriver(project);
        driver.remove(project);
    }
}

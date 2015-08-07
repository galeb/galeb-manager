package io.galeb.engine.farm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.engine.DriverBuilder;
import io.galeb.entity.Target;

@Component
public class TargetEngine {

    public static final String QUEUE_CREATE = "queue-target-create";
    public static final String QUEUE_UPDATE = "queue-target-update";
    public static final String QUEUE_REMOVE = "queue-target-remove";

    private static final Log LOGGER = LogFactory.getLog(TargetEngine.class);

    private Driver getDriver(Target target) {
        return DriverBuilder.build(Driver.DEFAULT_DRIVER_NAME);
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Target target) {
        LOGGER.info("Creating "+target.getClass().getSimpleName()+" "+target.getName());
        Driver driver = getDriver(target);
        driver.create(target);
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Target target) {
        LOGGER.info("Updating "+target.getClass().getSimpleName()+" "+target.getName());
        Driver driver = getDriver(target);
        driver.update(target);
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Target target) {
        LOGGER.info("Removing "+target.getClass().getSimpleName()+" "+target.getName());
        Driver driver = getDriver(target);
        driver.remove(target);
    }
}

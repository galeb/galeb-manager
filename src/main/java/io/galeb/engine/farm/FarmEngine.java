package io.galeb.engine.farm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.engine.DriverBuilder;
import io.galeb.entity.Farm;

@Component
public class FarmEngine {

    public static final String QUEUE_CREATE = "queue-farm-create";
    public static final String QUEUE_UPDATE = "queue-farm-update";
    public static final String QUEUE_REMOVE = "queue-farm-remove";

    private static final Log LOGGER = LogFactory.getLog(FarmEngine.class);

    private Driver getDriver(Farm farm) {
        return DriverBuilder.build(Driver.DEFAULT_DRIVER_NAME);
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Farm farm) {
        LOGGER.info("Creating "+farm.getClass().getSimpleName()+" "+farm.getName());
        Driver driver = getDriver(farm);
        driver.create(farm);
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Farm farm) {
        LOGGER.info("Updating "+farm.getClass().getSimpleName()+" "+farm.getName());
        Driver driver = getDriver(farm);
        driver.update(farm);
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Farm farm) {
        LOGGER.info("Removing "+farm.getClass().getSimpleName()+" "+farm.getName());
        Driver driver = getDriver(farm);
        driver.remove(farm);
    }
}

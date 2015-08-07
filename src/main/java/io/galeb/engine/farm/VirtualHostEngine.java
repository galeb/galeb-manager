package io.galeb.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.engine.DriverBuilder;
import io.galeb.entity.Farm;
import io.galeb.entity.VirtualHost;
import io.galeb.repository.FarmRepository;

@Component
public class VirtualHostEngine {

    public static final String QUEUE_CREATE = "queue-virtualhost-create";
    public static final String QUEUE_UPDATE = "queue-virtualhost-update";
    public static final String QUEUE_REMOVE = "queue-virtualhost-remove";

    private static final Log LOGGER = LogFactory.getLog(VirtualHostEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    private Driver getDriver(VirtualHost virtualHost) {
        long farmId = virtualHost.getFarmId();
        String driverName = Driver.DEFAULT_DRIVER_NAME;
        if (farmId>-1) {
            Optional<Farm> farm = farmRepository.findById(farmId).stream().findFirst();
            if (farm.isPresent()) {
                driverName = farm.get().getProvider().getDriver();
            }
        }
        return DriverBuilder.build(driverName);
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(VirtualHost virtualHost) {
        LOGGER.info("Creating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.create(virtualHost);
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(VirtualHost virtualHost) {
        LOGGER.info("Updating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.update(virtualHost);
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(VirtualHost virtualHost) {
        LOGGER.info("Removing "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.remove(virtualHost);
    }
}

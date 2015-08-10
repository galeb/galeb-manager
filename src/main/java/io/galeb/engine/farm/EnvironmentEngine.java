package io.galeb.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Environment;
import io.galeb.entity.Farm;

@Component
public class EnvironmentEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-environment-create";
    public static final String QUEUE_UPDATE = "queue-environment-update";
    public static final String QUEUE_REMOVE = "queue-environment-remove";

    private static final Log LOGGER = LogFactory.getLog(EnvironmentEngine.class);

    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        return Optional.empty();
    }

    @Override
    protected String makeJson(AbstractEntity<?> entity) {
        return "{}";
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Environment environment) {
        LOGGER.info("Creating "+environment.getClass().getSimpleName()+" "+environment.getName());
        Driver driver = getDriver(environment);
        driver.create(fromEntity(environment));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Environment environment) {
        LOGGER.info("Updating "+environment.getClass().getSimpleName()+" "+environment.getName());
        Driver driver = getDriver(environment);
        driver.update(fromEntity(environment));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Environment environment) {
        LOGGER.info("Removing "+environment.getClass().getSimpleName()+" "+environment.getName());
        Driver driver = getDriver(environment);
        driver.remove(fromEntity(environment));
    }

}

package io.galeb.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Farm;
import io.galeb.entity.TargetType;

@Component
public class TargetTypeEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-targettype-create";
    public static final String QUEUE_UPDATE = "queue-targettype-update";
    public static final String QUEUE_REMOVE = "queue-targettype-remove";

    private static final Log LOGGER = LogFactory.getLog(TargetTypeEngine.class);

    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        return Optional.empty();
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(TargetType targetType) {
        LOGGER.info("Creating "+targetType.getClass().getSimpleName()+" "+targetType.getName());
        Driver driver = getDriver(targetType);
        driver.create(fromEntity(targetType));
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void update(TargetType targetType) {
        LOGGER.info("Updating "+targetType.getClass().getSimpleName()+" "+targetType.getName());
        Driver driver = getDriver(targetType);
        driver.update(fromEntity(targetType));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void remove(TargetType targetType) {
        LOGGER.info("Removing "+targetType.getClass().getSimpleName()+" "+targetType.getName());
        Driver driver = getDriver(targetType);
        driver.remove(fromEntity(targetType));
    }
}

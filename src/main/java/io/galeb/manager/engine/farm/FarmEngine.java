package io.galeb.manager.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.manager.engine.Provisioning;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;

@Component
public class FarmEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-farm-create";
    public static final String QUEUE_UPDATE = "queue-farm-update";
    public static final String QUEUE_REMOVE = "queue-farm-remove";

    private static final Log LOGGER = LogFactory.getLog(FarmEngine.class);

    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        return Optional.empty();
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Farm farm) {
        LOGGER.info("Creating "+farm.getClass().getSimpleName()+" "+farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        provisioning.create(fromEntity(farm));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Farm farm) {
        LOGGER.info("Removing "+farm.getClass().getSimpleName()+" "+farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        provisioning.create(fromEntity(farm));
    }
}

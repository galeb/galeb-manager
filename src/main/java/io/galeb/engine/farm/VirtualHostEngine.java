package io.galeb.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Farm;
import io.galeb.entity.VirtualHost;
import io.galeb.repository.FarmRepository;

@Component
public class VirtualHostEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-virtualhost-create";
    public static final String QUEUE_UPDATE = "queue-virtualhost-update";
    public static final String QUEUE_REMOVE = "queue-virtualhost-remove";

    private static final Log LOGGER = LogFactory.getLog(VirtualHostEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        long farmId = -1L;
        if (entity instanceof VirtualHost) {
            farmId = ((VirtualHost)entity).getFarmId();
        }
        return farmRepository.findById(farmId).stream().findFirst();
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(VirtualHost virtualHost) {
        LOGGER.info("Creating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.create(fromEntity(virtualHost));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(VirtualHost virtualHost) {
        LOGGER.info("Updating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.update(fromEntity(virtualHost));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(VirtualHost virtualHost) {
        LOGGER.info("Removing "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.remove(fromEntity(virtualHost));
    }
}

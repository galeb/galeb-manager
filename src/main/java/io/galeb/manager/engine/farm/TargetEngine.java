package io.galeb.manager.engine.farm;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Target;
import io.galeb.manager.repository.FarmRepository;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class TargetEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-target-create";
    public static final String QUEUE_UPDATE = "queue-target-update";
    public static final String QUEUE_REMOVE = "queue-target-remove";

    private static final Log LOGGER = LogFactory.getLog(TargetEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        long farmId = -1L;
        if (entity instanceof Target) {
            farmId = ((Target)entity).getFarmId();
        }
        return farmRepository.findById(farmId).stream().findFirst();
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Target target) {
        LOGGER.info("Creating "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = getDriver(target);
        driver.create(makeProperties(target));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Target target) {
        LOGGER.info("Updating "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = getDriver(target);
        driver.update(makeProperties(target));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Target target) {
        LOGGER.info("Removing "+target.getClass().getSimpleName()+" "+target.getName());
        final Driver driver = getDriver(target);
        driver.remove(makeProperties(target));
    }

    private Properties makeProperties(Target target) {
        String json = "{}";
        try {
            final JsonMapper jsonMapper = makeJson(target);
            if (target.getParent() != null) {
                jsonMapper.putString("parentId", target.getParent().getName());
            }
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.equals(e.getMessage());
        }
        final Properties properties = fromEntity(target);
        properties.put("json", json);
        properties.put("path", target.getTargetType().getName().toLowerCase());
        return properties;
    }
}

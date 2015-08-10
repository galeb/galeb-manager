package io.galeb.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.EntityAffiliable;
import io.galeb.entity.Farm;
import io.galeb.entity.Rule;
import io.galeb.entity.Target;
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.repository.FarmRepository;

@Component
public class TargetEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-target-create";
    public static final String QUEUE_UPDATE = "queue-target-update";
    public static final String QUEUE_REMOVE = "queue-target-remove";

    private static final Log LOGGER = LogFactory.getLog(TargetEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @SuppressWarnings("unused")
    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        long farmId = -1L;
        if (entity instanceof EntityAffiliable) {
            AbstractEntity<?> targetParent = null;// = ((EntityAffiliable<Target>) entity).getParent();
            if (targetParent instanceof Rule) {
                Rule rule = (Rule)targetParent;
                //farmId = rule.getFarmId();
            }
        }
        return farmRepository.findById(farmId).stream().findFirst();
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Target target) {
        LOGGER.info("Creating "+target.getClass().getSimpleName()+" "+target.getName());
        Driver driver = getDriver(target);
        driver.create(makeProperties(target));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Target target) {
        LOGGER.info("Updating "+target.getClass().getSimpleName()+" "+target.getName());
        Driver driver = getDriver(target);
        driver.update(makeProperties(target));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Target target) {
        LOGGER.info("Removing "+target.getClass().getSimpleName()+" "+target.getName());
        Driver driver = getDriver(target);
        driver.remove(makeProperties(target));
    }

    private Properties makeProperties(Target target) {
        String json = "{}";
        try {
            JsonMapper jsonMapper = makeJson(target);
            json = jsonMapper.toString();
        } catch (JsonProcessingException e) {
            LOGGER.equals(e.getMessage());
        }
        Properties properties = fromEntity(target);
        properties.put("json", json);
        properties.put("path", target.getTargetType().getName().toLowerCase());
        return properties;
    }
}

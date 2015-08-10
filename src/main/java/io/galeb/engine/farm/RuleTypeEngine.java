package io.galeb.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Farm;
import io.galeb.entity.RuleType;

@Component
public class RuleTypeEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-ruletype-create";
    public static final String QUEUE_UPDATE = "queue-ruletype-update";
    public static final String QUEUE_REMOVE = "queue-ruletype-remove";

    private static final Log LOGGER = LogFactory.getLog(RuleTypeEngine.class);

    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        return Optional.empty();
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(RuleType ruleType) {
        LOGGER.info("Creating "+ruleType.getClass().getSimpleName()+" "+ruleType.getName());
        Driver driver = getDriver(ruleType);
        driver.create(fromEntity(ruleType));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(RuleType ruleType) {
        LOGGER.info("Updating "+ruleType.getClass().getSimpleName()+" "+ruleType.getName());
        Driver driver = getDriver(ruleType);
        driver.update(fromEntity(ruleType));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(RuleType ruleType) {
        LOGGER.info("Removing "+ruleType.getClass().getSimpleName()+" "+ruleType.getName());
        Driver driver = getDriver(ruleType);
        driver.remove(fromEntity(ruleType));
    }
}

package io.galeb.engine.farm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.engine.Driver;
import io.galeb.engine.DriverBuilder;
import io.galeb.entity.Rule;

@Component
public class RuleEngine {

    public static final String QUEUE_CREATE = "queue-rule-create";
    public static final String QUEUE_UPDATE = "queue-rule-update";
    public static final String QUEUE_REMOVE = "queue-rule-remove";

    private static final Log LOGGER = LogFactory.getLog(RuleEngine.class);

    private Driver getDriver(Rule rule) {
        return DriverBuilder.build(Driver.DEFAULT_DRIVER_NAME);
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Rule rule) {
        LOGGER.info("Creating "+rule.getClass().getSimpleName()+" "+rule.getName());
        Driver driver = getDriver(rule);
        driver.create(rule);
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Rule rule) {
        LOGGER.info("Updating "+rule.getClass().getSimpleName()+" "+rule.getName());
        Driver driver = getDriver(rule);
        driver.update(rule);
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Rule rule) {
        LOGGER.info("Removing "+rule.getClass().getSimpleName()+" "+rule.getName());
        Driver driver = getDriver(rule);
        driver.remove(rule);
    }
}

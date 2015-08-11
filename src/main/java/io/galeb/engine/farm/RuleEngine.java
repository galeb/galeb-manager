package io.galeb.engine.farm;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Farm;
import io.galeb.entity.Rule;
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.repository.FarmRepository;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class RuleEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-rule-create";
    public static final String QUEUE_UPDATE = "queue-rule-update";
    public static final String QUEUE_REMOVE = "queue-rule-remove";

    private static final Log LOGGER = LogFactory.getLog(RuleEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @Override
    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        long farmId = -1L;
        if (entity instanceof Rule) {
            farmId = ((Rule)entity).getParent().getFarmId();
        }
        return farmRepository.findById(farmId).stream().findFirst();
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Rule rule) {
        LOGGER.info("Creating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = getDriver(rule);
        driver.create(makeProperties(rule));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(Rule rule) {
        LOGGER.info("Updating "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = getDriver(rule);
        driver.update(makeProperties(rule));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Rule rule) {
        LOGGER.info("Removing "+rule.getClass().getSimpleName()+" "+rule.getName());
        final Driver driver = getDriver(rule);
        driver.remove(makeProperties(rule));
    }

    private Properties makeProperties(Rule rule) {
        String json = "{}";
        try {
            final JsonMapper jsonMapper = makeJson(rule);
            jsonMapper.putString("parentId", rule.getParent().getName());
            jsonMapper.addToNode("properties", "ruleType", rule.getRuleType().getName());
            jsonMapper.addToNode("properties", "targetType", rule.getTarget().getTargetType().getName());
            jsonMapper.addToNode("properties", "targetId", rule.getTarget().getName());
            json = jsonMapper.toString();
        } catch (final JsonProcessingException e) {
            LOGGER.equals(e.getMessage());
        }
        final Properties properties = fromEntity(rule);
        properties.put("json", json);
        properties.put("path", "rule");
        return properties;
    }
}

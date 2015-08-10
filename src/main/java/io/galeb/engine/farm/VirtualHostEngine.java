package io.galeb.engine.farm;

import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Farm;
import io.galeb.entity.VirtualHost;
import io.galeb.manager.common.Properties;
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

    @Override
    protected String makeJson(AbstractEntity<?> entity) {
        String result = "{}";
        if(entity instanceof VirtualHost) {
            JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);
            ObjectNode node = jsonNodeFactory.objectNode();

            node.put("id", entity.getName());
            node.put("pk", entity.getId());
            node.put("version", entity.getId());
            ObjectNode properties = jsonNodeFactory.objectNode();
            entity.getProperties().entrySet().stream().forEach(entry -> {
                properties.put(entry.getKey(), entry.getValue());
            });
            node.set("properties", properties);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            try {
                result = mapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return result;
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(VirtualHost virtualHost) {
        LOGGER.info("Creating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        String json = makeJson(virtualHost);
        Properties properties = fromEntity(virtualHost);
        properties.put("json", json);
        properties.put("path", "virtualhost");
        Driver driver = getDriver(virtualHost);
        driver.create(properties);
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

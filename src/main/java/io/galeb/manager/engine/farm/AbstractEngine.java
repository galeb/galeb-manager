package io.galeb.manager.engine.farm;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.engine.Provisioning;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;

public abstract class AbstractEngine {

    protected abstract Optional<Farm> findFarm(AbstractEntity<?> entity);

    protected JsonMapper makeJson(AbstractEntity<?> entity) throws JsonProcessingException {

        JsonMapper json = new JsonMapper();
        json.putString("id", entity.getName());
        json.putLong("pk", entity.getId());
        json.putLong("version", entity.getId());
        entity.getProperties().entrySet().stream().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            json.addToNode("properties", key, value);
        });

        return json;
    }

    protected Properties fromEntity(AbstractEntity<?> entity) {
        Properties properties = new Properties();
        properties.put("api", findApi(entity));
        return properties;
    }

    protected String findApi(AbstractEntity<?> entity) {
        Optional<Farm> farm = findFarm(entity);
        return farm.isPresent() ? farm.get().getApi() : "UNDEF";
    }

    protected Driver getDriver(AbstractEntity<?> entity) {
        String driverName = Driver.DEFAULT_DRIVER_NAME;
        Optional<Farm> farm = findFarm(entity);
        if (farm.isPresent()) {
            driverName = farm.get().getProvider().getDriver();
        }
        return DriverBuilder.build(driverName);
    }

    protected Provisioning getProvisioning(AbstractEntity<?> entity) {
        return new Provisioning() {
            // NULL
        };
    }
}

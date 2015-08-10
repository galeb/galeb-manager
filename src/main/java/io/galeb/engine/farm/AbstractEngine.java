package io.galeb.engine.farm;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.engine.Driver;
import io.galeb.engine.DriverBuilder;
import io.galeb.engine.Provisioning;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Farm;
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;

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

package io.galeb.engine.farm;

import java.util.Optional;

import io.galeb.engine.Driver;
import io.galeb.engine.DriverBuilder;
import io.galeb.entity.AbstractEntity;
import io.galeb.entity.Farm;
import io.galeb.manager.common.Properties;

public abstract class AbstractEngine {

    protected abstract Optional<Farm> findFarm(AbstractEntity<?> entity);

    protected Properties fromEntity(AbstractEntity<?> entity) {
        Properties properties = new Properties();
        properties.put("entity", entity);
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
}

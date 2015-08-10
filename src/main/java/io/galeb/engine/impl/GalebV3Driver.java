package io.galeb.engine.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.galeb.engine.Driver;
import io.galeb.handler.VirtualHostHandler;
import io.galeb.manager.common.Properties;

public class GalebV3Driver implements Driver {

    public static final String DRIVER_NAME = GalebV3Driver.class.getSimpleName()
                                                                .replaceAll("Driver", "");

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

    @Override
    public String toString() {
        return DRIVER_NAME;
    }

    @Override
    public boolean create(Properties properties) {
        Class<?> entityClass = properties.getOrDefault("entity", new Object()).getClass();
        String api = properties.getOrDefault("api", "NULL").toString();
        LOGGER.info(entityClass.getName());
        LOGGER.info(">>>>>>>>>>> FARM API:" + api);
        return true;
    }

}

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
        String api = properties.getOrDefault("api", "NULL").toString();
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString();
        LOGGER.info(">>>>>>>>>>> POST http://" + api + "/" + path);
        LOGGER.info(json);
        return true;
    }

}

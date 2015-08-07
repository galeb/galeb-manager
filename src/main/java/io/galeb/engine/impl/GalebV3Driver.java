package io.galeb.engine.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.galeb.engine.Driver;
import io.galeb.entity.AbstractEntity;
import io.galeb.handler.VirtualHostHandler;

@Service
public class GalebV3Driver implements Driver {

    public static final String DRIVER_NAME = GalebV3Driver.class.getSimpleName()
                                                                .replaceAll("Driver", "");

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);
    private static final Gson jsonParser = new GsonBuilder().setPrettyPrinting()
                                                            .create();

    @Override
    public String toString() {
        return DRIVER_NAME;
    }

    @Override
    public boolean create(AbstractEntity<?> entity) {
        Class<?> entityClass = entity.getClass();
        LOGGER.info(entityClass.getName());
        LOGGER.info(jsonParser.toJson(entity));
        return true;
    }

}

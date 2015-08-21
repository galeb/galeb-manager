/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.engine.farm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.repository.FarmRepository;

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
        return findFarmById(farmRepository, farmId);
    }

    @JmsListener(destination = QUEUE_CREATE)
    public void create(VirtualHost virtualHost) {
        LOGGER.info("Creating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.create(makeProperties(virtualHost));
    }

    @JmsListener(destination = QUEUE_UPDATE)
    public void update(VirtualHost virtualHost) {
        LOGGER.info("Updating "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.update(makeProperties(virtualHost));
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(VirtualHost virtualHost) {
        LOGGER.info("Removing "+virtualHost.getClass().getSimpleName()+" "+virtualHost.getName());
        Driver driver = getDriver(virtualHost);
        driver.remove(makeProperties(virtualHost));
    }

    private Properties makeProperties(VirtualHost virtualHost) {
        String json = "{}";
        try {
            JsonMapper jsonMapper = makeJson(virtualHost);
            json = jsonMapper.toString();
        } catch (JsonProcessingException e) {
            LOGGER.equals(e.getMessage());
        }
        Properties properties = fromEntity(virtualHost);
        properties.put("json", json);
        properties.put("path", "virtualhost");
        return properties;
    }
}
